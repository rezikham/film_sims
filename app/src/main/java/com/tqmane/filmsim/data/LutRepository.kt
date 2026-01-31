package com.tqmane.filmsim.data

import android.content.Context

data class LutItem(
    val name: String,
    val assetPath: String
)

data class LutCategory(
    val name: String,
    val displayName: String,
    val items: List<LutItem>
)

data class LutBrand(
    val name: String,
    val displayName: String,
    val categories: List<LutCategory>
)

object LutRepository {
    
    private val brandDisplayNames = mapOf(
        "OnePlus" to "OnePlus",
        "Xiaomi" to "Xiaomi"
    )
    
    private val categoryDisplayNames = mapOf(
        // OnePlus
        "App Filters" to "アプリフィルター",
        "Artistic" to "アーティスティック",
        "Black & White" to "モノクロ",
        "Cinematic Movie" to "シネマティック",
        "Cool Tones" to "クールトーン",
        "Food" to "フード",
        "Fujifilm" to "Fujifilm",
        "Golden Touch" to "ゴールデンタッチ",
        "Hasselblad Master" to "Hasselblad Master",
        "Instagram Filters" to "Instagram風",
        "Japanese Style" to "日本風",
        "Kodak Film" to "Kodak Film",
        "Landscape" to "風景",
        "Night" to "夜景",
        "OPPO Original" to "OPPO Original",
        "Portrait" to "ポートレート",
        "Ricoh GR" to "Ricoh GR",
        "Uncategorized" to "未分類",
        "Vintage-Retro" to "ヴィンテージ・レトロ",
        "Warm Tones" to "ウォームトーン",
        // Xiaomi
        "adjust" to "調整",
        "color_highlight" to "ハイライト",
        "color_shadow" to "シャドウ",
        "dolby" to "Dolby",
        "enhance" to "強調",
        "film" to "フィルム",
        "leica" to "Leica",
        "onekey" to "ワンキー",
        "popular" to "人気"
    )
    
    fun getLutBrands(context: Context): List<LutBrand> {
        val assetManager = context.assets
        val brands = mutableListOf<LutBrand>()
        
        try {
            val rootPath = "luts"
            val brandFolders = assetManager.list(rootPath) ?: return emptyList()
            
            for (brandName in brandFolders) {
                val brandPath = "$rootPath/$brandName"
                val categoryFolders = assetManager.list(brandPath) ?: continue
                
                val categories = mutableListOf<LutCategory>()
                
                for (categoryName in categoryFolders) {
                    val categoryPath = "$brandPath/$categoryName"
                    val files = assetManager.list(categoryPath) ?: continue
                    
                    val lutItems = files
                        .filter { it.endsWith(".cube", ignoreCase = true) }
                        .map { filename ->
                            LutItem(
                                name = filename.removeSuffix(".cube").removeSuffix(".CUBE").replace("_", " "),
                                assetPath = "$categoryPath/$filename"
                            )
                        }
                        .sortedBy { it.name.lowercase() }
                    
                    if (lutItems.isNotEmpty()) {
                        categories.add(
                            LutCategory(
                                name = categoryName,
                                displayName = categoryDisplayNames[categoryName] ?: categoryName.replace("_", " "),
                                items = lutItems
                            )
                        )
                    }
                }
                
                if (categories.isNotEmpty()) {
                    brands.add(
                        LutBrand(
                            name = brandName,
                            displayName = brandDisplayNames[brandName] ?: brandName,
                            categories = categories.sortedBy { it.displayName }
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return brands.sortedBy { it.displayName }
    }
    
    // Legacy support
    fun getLutGenres(context: Context): List<LutGenre> {
        val brands = getLutBrands(context)
        return brands.flatMap { brand ->
            brand.categories.map { category ->
                LutGenre(
                    name = "${brand.displayName} - ${category.displayName}",
                    items = category.items
                )
            }
        }
    }
}

// Legacy data class for compatibility
data class LutGenre(
    val name: String,
    val items: List<LutItem>
)