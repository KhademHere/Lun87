package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

enum class StudioModelTier(
    val id: String,
    val label: String,
    val hudBadge: String,
    val modelName: String,
    val subtitle: String
) {
    FAST_STUDIO(
        id = "fast",
        label = "Studio Fast",
        hudBadge = "FAST",
        modelName = "gemini-nano-banana-2.1",
        subtitle = "Rapid synthesis & retouching"
    ),
    PRO_STUDIO(
        id = "pro",
        label = "Studio Pro",
        hudBadge = "PRO",
        modelName = "gemini-3-pro-image-preview",
        subtitle = "Flagship 85mm studio fidelity"
    )
}

enum class HeadshotAspectRatio(
    val label: String,
    val ratioValue: Float,
    val hint: String
) {
    RATIO_1_1("1:1", 1f, "LinkedIn / Avatar"),
    RATIO_2_3("2:3", 2f / 3f, "Classic 35mm Portrait"),
    RATIO_3_2("3:2", 3f / 2f, "Editorial Press"),
    RATIO_3_4("3:4", 3f / 4f, "Studio Headshot"),
    RATIO_4_3("4:3", 4f / 3f, "Speaker Bio"),
    RATIO_9_16("9:16", 9f / 16f, "Vertical Story"),
    RATIO_16_9("16:9", 16f / 9f, "Banner / Keynote"),
    RATIO_21_9("21:9", 21f / 9f, "Cinema Widescreen")
}

enum class HeadshotResolution(
    val label: String,
    val detail: String
) {
    RES_1K("1K", "1024px"),
    RES_2K("2K", "2048px"),
    RES_4K("4K", "4096px")
}

data class HeadshotStylePreset(
    val id: String,
    val title: String,
    val shortTitle: String,
    val lightingSpec: String,
    val lensSpec: String,
    val wardrobeCue: String,
    val promptRecipe: String,
    @DrawableRes val thumbnailRes: Int?,
    val fallbackGradientHexStart: Long = 0xFF28221D,
    val fallbackGradientHexEnd: Long = 0xFF141316
)

object StudioPresets {
    val all: List<HeadshotStylePreset> = listOf(
        HeadshotStylePreset(
            id = "corporate_grey",
            title = "Corporate Grey Backdrop",
            shortTitle = "Corporate Grey",
            lightingSpec = "3-POINT SOFTBOX",
            lensSpec = "85MM F/2.0",
            wardrobeCue = "Tailored charcoal blazer & crisp shirt",
            promptRecipe = "Seamless matte medium-slate corporate studio backdrop with soft radial falloff, classic three-point softbox Rembrandt key lighting with gentle hair rim light, crisp tailored business attire, natural confident expression, authentic skin texture with zero plastic airbrushing, shot on medium-format 85mm f/2.0 portrait lens.",
            thumbnailRes = R.drawable.img_style_corporate
        ),
        HeadshotStylePreset(
            id = "tech_office",
            title = "Modern Tech Office",
            shortTitle = "Tech Office",
            lightingSpec = "WINDOW KEY + BOKEH",
            lensSpec = "85MM F/1.4",
            wardrobeCue = "Smart casual knit or structured overshirt",
            promptRecipe = "Contemporary architectural tech headquarters interior softly blurred at f/1.4 with warm oak slats, glass partitions, and creamy pendant light bokeh, natural directional window key light, modern smart-casual leadership wardrobe, approachable authentic smile, editorial startup founder portrait.",
            thumbnailRes = R.drawable.img_style_tech_office
        ),
        HeadshotStylePreset(
            id = "outdoor_natural",
            title = "Outdoor Natural Light",
            shortTitle = "Outdoor Light",
            lightingSpec = "GOLDEN HOUR RIM",
            lensSpec = "85MM F/1.4",
            wardrobeCue = "Refined neutral casual jacket or linen",
            promptRecipe = "Golden-hour outdoor urban architectural avenue with sunlit limestone and lush soft-focus greenery, warm natural backlit rim light around hair and shoulders, soft open shade on face with bright catchlights in eyes, relaxed authentic expression, creamy 85mm f/1.4 bokeh.",
            thumbnailRes = R.drawable.img_style_outdoor
        ),
        HeadshotStylePreset(
            id = "editorial_mono",
            title = "Editorial Monochrome",
            shortTitle = "Editorial B&W",
            lightingSpec = "CHIAROSCURO",
            lensSpec = "50MM F/1.8",
            wardrobeCue = "Minimalist black turtleneck or dark jacket",
            promptRecipe = "Timeless fine-art black-and-white silver-halide studio portrait against dark textured charcoal canvas, sculpted directional key light with rich midtones and crisp highlights, subtle organic film grain, poised magazine editorial presence, minimalist dark wardrobe.",
            thumbnailRes = R.drawable.img_style_editorial
        ),
        HeadshotStylePreset(
            id = "executive_warm",
            title = "Executive Warm Studio",
            shortTitle = "Warm Executive",
            lightingSpec = "TUNGSTEN OCTABOX",
            lensSpec = "105MM F/2.0",
            wardrobeCue = "Structured navy or espresso tailored suit",
            promptRecipe = "Warm obsidian-and-espresso painted studio backdrop illuminated by a large tungsten octabox and subtle amber separation light, authoritative yet approachable C-suite executive posture, finely tailored dark suit, razor-sharp eyes, magazine-grade tonal depth.",
            thumbnailRes = null,
            fallbackGradientHexStart = 0xFF3A281A,
            fallbackGradientHexEnd = 0xFF141112
        ),
        HeadshotStylePreset(
            id = "creative_loft",
            title = "Creative Loft Daylight",
            shortTitle = "Creative Loft",
            lightingSpec = "NORTH DAYLIGHT",
            lensSpec = "50MM F/1.4",
            wardrobeCue = "Architectural minimal layers",
            promptRecipe = "Airy industrial-modern creative studio loft with tall steel-framed daylight windows, soft cool-warm architectural plaster walls gently out of focus, luminous diffused northern daylight sculpting cheekbones naturally, confident creative director energy.",
            thumbnailRes = null,
            fallbackGradientHexStart = 0xFF28323D,
            fallbackGradientHexEnd = 0xFF13161B
        )
    )

    fun byId(id: String): HeadshotStylePreset =
        all.firstOrNull { it.id == id } ?: all.first()
}

data class RetouchQuickChip(
    val id: String,
    val label: String,
    val instruction: String
)

object RetouchPresets {
    val quickChips: List<RetouchQuickChip> = listOf(
        RetouchQuickChip(
            id = "charcoal_blazer",
            label = "Charcoal Blazer",
            instruction = "Update the wardrobe to a finely tailored charcoal wool blazer over a crisp clean crew neck or collar while keeping face, hair, and lighting identical."
        ),
        RetouchQuickChip(
            id = "warm_key",
            label = "Warmer Key Light",
            instruction = "Shift the studio lighting subtly warmer with a golden tungsten key light and gentle rim separation while preserving natural skin tones."
        ),
        RetouchQuickChip(
            id = "creamy_bokeh",
            label = "Deeper Bokeh",
            instruction = "Increase the optical background blur to a creamy 85mm f/1.2 shallow depth of field so the subject pops crisply from the backdrop."
        ),
        RetouchQuickChip(
            id = "natural_smile",
            label = "Warm Natural Smile",
            instruction = "Refine the expression into a slightly warmer, relaxed, confident natural smile with bright eye catchlights while preserving exact facial identity."
        ),
        RetouchQuickChip(
            id = "tidy_hair",
            label = "Clean Flyaways",
            instruction = "Gently tame stray flyaway hairs and soften harsh specular shine on forehead and nose while maintaining completely natural skin texture."
        ),
        RetouchQuickChip(
            id = "twilight_skyline",
            label = "Twilight Skyline",
            instruction = "Replace the background with a softly blurred architectural twilight city skyline viewed through floor-to-ceiling office glass with warm interior bokeh."
        )
    )
}

data class SelfieAnalysisResult(
    val overallScore: Int,
    val lightingScore: Int,
    val framingScore: Int,
    val sharpnessScore: Int,
    val expressionScore: Int,
    val summary: String,
    val tips: List<String>,
    val recommendedPresetId: String,
    val recommendedReason: String
)

sealed interface AsyncOpState<out T> {
    data object Idle : AsyncOpState<Nothing>
    data object Loading : AsyncOpState<Nothing>
    data class Streaming(val partialText: String) : AsyncOpState<Nothing>
    data class Success<T>(val data: T) : AsyncOpState<T>
    data class Error(val message: String) : AsyncOpState<Nothing>
}
