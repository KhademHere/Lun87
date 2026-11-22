package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "headshots")
data class HeadshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val stylePresetId: String,
    val styleName: String,
    val modelTierId: String,
    val modelName: String,
    val aspectRatioLabel: String,
    val resolutionLabel: String,
    val retouchPrompt: String? = null,
    val sourceImagePath: String,
    val generatedImagePath: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
