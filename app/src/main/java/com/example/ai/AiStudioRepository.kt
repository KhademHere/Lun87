package com.example.ai

import android.graphics.Bitmap
import com.example.model.HeadshotAspectRatio
import com.example.model.HeadshotResolution
import com.example.model.HeadshotStylePreset
import com.example.model.SelfieAnalysisResult
import com.example.model.StudioModelTier
import com.example.model.StudioPresets
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.json.JSONObject

class AiStudioRepository {

    private val selfieAnalysisSchema: Schema by lazy {
        Schema.obj(
            properties = mapOf(
                "overallScore" to Schema.integer(description = "Overall headshot readiness score from 0 to 100"),
                "lightingScore" to Schema.integer(description = "Facial lighting quality score from 0 to 100"),
                "framingScore" to Schema.integer(description = "Head and shoulders composition score from 0 to 100"),
                "sharpnessScore" to Schema.integer(description = "Focus and optical clarity score from 0 to 100"),
                "expressionScore" to Schema.integer(description = "Natural approachable expression score from 0 to 100"),
                "summary" to Schema.string(description = "Concise 2-sentence studio photographer critique of the selfie"),
                "tips" to Schema.array(
                    items = Schema.string(),
                    description = "3 to 4 concrete, actionable tips to improve the selfie or guide retouching"
                ),
                "recommendedPresetId" to Schema.enumeration(
                    values = StudioPresets.all.map { it.id },
                    description = "Best matching headshot style preset ID for this subject"
                ),
                "recommendedReason" to Schema.string(description = "One sentence explaining why this backdrop style suits the subject")
            )
        )
    }

    private val analysisModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-4-neon",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = selfieAnalysisSchema
            }
        )
    }

    // High-thinking art direction model: intentionally omits maxOutputTokens.
    private val deepArtDirectionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-4-carbon",
            generationConfig = generationConfig {
                thinkingConfig = thinkingConfig {
                    thinkingLevel = ThinkingLevel.HIGH
                }
            }
        )
    }

    suspend fun generateHeadshot(
        selfieBitmap: Bitmap,
        preset: HeadshotStylePreset,
        modelTier: StudioModelTier,
        aspectRatio: HeadshotAspectRatio,
        resolution: HeadshotResolution,
        customNotes: String
    ): Bitmap {
        val imageConfig = ImageConfig.builder()
            .setAspectRatio(aspectRatio.toSdkAspectRatio())
            .setImageSize(resolution.toSdkImageSize())
            .build()

        val model = Firebase.ai.generativeModel(
            modelName = modelTier.modelName,
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
                this.imageConfig = imageConfig
            }
        )

        val customDirection = customNotes.trim().takeIf { it.isNotEmpty() }
            ?.let { " Additional art direction from client: $it." }
            .orEmpty()

        val prompt = buildString {
            append("Transform the person in this casual selfie into an authentic, magazine-grade professional studio headshot. ")
            append("Preserve the subject's exact facial structure, skin tone, eye shape, age, hair characteristics, and unmistakable identity. ")
            append("Style direction (${preset.title}): ${preset.promptRecipe} ")
            append("Wardrobe cue: ${preset.wardrobeCue}. ")
            append("Compose cleanly for a ${aspectRatio.label} frame with natural shoulder posture, crisp eye focus, lifelike pores and hair detail, and zero artificial plastic smoothing.")
            append(customDirection)
        }

        val response = model.generateContent(
            content {
                image(selfieBitmap)
                text(prompt)
            }
        )

        val imagePart = response.candidates.firstOrNull()
            ?.content?.parts?.filterIsInstance<ImagePart>()
            ?.firstOrNull()

        return imagePart?.image
            ?: throw IllegalStateException("The studio model did not return an image. Please try again.")
    }

    suspend fun retouchHeadshot(
        currentHeadshot: Bitmap,
        originalSelfie: Bitmap?,
        instruction: String,
        aspectRatio: HeadshotAspectRatio,
        resolution: HeadshotResolution
    ): Bitmap {
        val imageConfig = ImageConfig.builder()
            .setAspectRatio(aspectRatio.toSdkAspectRatio())
            .setImageSize(resolution.toSdkImageSize())
            .build()

        val model = Firebase.ai.generativeModel(
            modelName = StudioModelTier.FAST_STUDIO.modelName,
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
                this.imageConfig = imageConfig
            }
        )

        val prompt = buildString {
            append("Apply this precise darkroom retouching instruction to the professional headshot: ")
            append(instruction.trim())
            append(". Keep the subject's exact facial identity, proportions, and photorealistic natural skin texture faithful to the reference while executing the requested edit seamlessly at ${aspectRatio.label} framing.")
        }

        val response = model.generateContent(
            content {
                image(currentHeadshot)
                if (originalSelfie != null) {
                    image(originalSelfie)
                }
                text(prompt)
            }
        )

        val imagePart = response.candidates.firstOrNull()
            ?.content?.parts?.filterIsInstance<ImagePart>()
            ?.firstOrNull()

        return imagePart?.image
            ?: throw IllegalStateException("Retouch model did not return an edited image. Please try again.")
    }

    suspend fun analyzeSelfie(selfieBitmap: Bitmap): SelfieAnalysisResult {
        val presetCatalog = StudioPresets.all.joinToString("\n") { "- ${it.id}: ${it.title} (${it.lightingSpec})" }
        val prompt = buildString {
            append("You are a veteran commercial portrait and headshot photographer inspecting a client's casual selfie before a studio shoot. ")
            append("Evaluate lighting direction, head-and-shoulders framing, optical sharpness, and natural expression. ")
            append("Choose the single best matching backdrop preset from this list:\n")
            append(presetCatalog)
            append("\nProvide practical, encouraging tips tailored specifically to what you observe in this image.")
        }

        val response = analysisModel.generateContent(
            content {
                image(selfieBitmap)
                text(prompt)
            }
        )

        val jsonText = response.text
            ?: throw IllegalStateException("Selfie analysis returned an empty response.")

        val json = JSONObject(jsonText)
        val tipsArray = json.optJSONArray("tips")
        val tips = buildList {
            if (tipsArray != null) {
                for (i in 0 until tipsArray.length()) {
                    val tip = tipsArray.optString(i).trim()
                    if (tip.isNotEmpty()) add(tip)
                }
            }
        }

        return SelfieAnalysisResult(
            overallScore = json.optInt("overallScore", 75).coerceIn(0, 100),
            lightingScore = json.optInt("lightingScore", 75).coerceIn(0, 100),
            framingScore = json.optInt("framingScore", 75).coerceIn(0, 100),
            sharpnessScore = json.optInt("sharpnessScore", 75).coerceIn(0, 100),
            expressionScore = json.optInt("expressionScore", 75).coerceIn(0, 100),
            summary = json.optString("summary", "Ready for studio development."),
            tips = tips,
            recommendedPresetId = json.optString("recommendedPresetId", StudioPresets.all.first().id),
            recommendedReason = json.optString("recommendedReason", "Balances your natural contrast and lighting.")
        )
    }

    fun streamDeepArtDirection(
        selfieBitmap: Bitmap,
        targetRole: String,
        selectedPreset: HeadshotStylePreset
    ): Flow<String> {
        val roleClause = targetRole.trim().takeIf { it.isNotEmpty() }
            ?: "Senior Professional / Executive LinkedIn Profile"

        val prompt = buildString {
            append("You are a Creative Director & Master Headshot Photographer preparing a bespoke art direction brief. ")
            append("Examine this client's selfie carefully for skin undertone, hair contrast, face shape, and current lighting. ")
            append("Client goal / role: $roleClause. Currently selected backdrop: ${selectedPreset.title} (${selectedPreset.lightingSpec}).\n\n")
            append("Deliver a concise, high-signal studio brief organized with these exact plain-text headings:\n")
            append("1. LIGHTING & LENS STRATEGY\n")
            append("2. WARDROBE & COLOR PALETTE\n")
            append("3. POSE & MICRO-EXPRESSION\n")
            append("4. CUSTOM RETOUCH PROMPTS (provide 3 short copy-ready prompt sentences starting with '- ')\n")
            append("Keep each section crisp, insightful, and directly grounded in what you see in the selfie.")
        }

        return deepArtDirectionModel.generateContentStream(
            content {
                image(selfieBitmap)
                text(prompt)
            }
        ).mapNotNull { chunk -> chunk.text }
    }

    private fun HeadshotAspectRatio.toSdkAspectRatio(): AspectRatio = when (this) {
        HeadshotAspectRatio.RATIO_1_1 -> AspectRatio.SQUARE_1x1
        HeadshotAspectRatio.RATIO_2_3 -> AspectRatio.PORTRAIT_2x3
        HeadshotAspectRatio.RATIO_3_2 -> AspectRatio.LANDSCAPE_3x2
        HeadshotAspectRatio.RATIO_3_4 -> AspectRatio.PORTRAIT_3x4
        HeadshotAspectRatio.RATIO_4_3 -> AspectRatio.LANDSCAPE_4x3
        HeadshotAspectRatio.RATIO_9_16 -> AspectRatio.PORTRAIT_9x16
        HeadshotAspectRatio.RATIO_16_9 -> AspectRatio.LANDSCAPE_16x9
        HeadshotAspectRatio.RATIO_21_9 -> AspectRatio.LANDSCAPE_21x9
    }

    private fun HeadshotResolution.toSdkImageSize(): ImageSize = when (this) {
        HeadshotResolution.RES_1K -> ImageSize.SIZE_1K
        HeadshotResolution.RES_2K -> ImageSize.SIZE_2K
        HeadshotResolution.RES_4K -> ImageSize.SIZE_4K
    }
}
