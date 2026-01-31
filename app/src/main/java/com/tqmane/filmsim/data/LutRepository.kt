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
        "Nothing" to "Nothing",
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
        "Artistic" to "アーティスティック",
        "Cinematic" to "シネマティック",
        "Cool Tones" to "クールトーン",
        "Film Simulation" to "フィルムシミュレーション",
        "Japanese Style" to "日本風",
        "Leica" to "Leica",
        "Monochrome" to "モノクロ",
        "Nature-Landscape" to "自然・風景",
        "Portrait-Soft" to "ポートレート・ソフト",
        "Special Effects" to "特殊効果",
        "Vivid-Natural" to "ビビッド・ナチュラル",
        "Warm-Vintage" to "ウォーム・ヴィンテージ",
        // Nothing (flat structure - uses "All" as virtual category)
        "_all" to "すべて"
    )
    
    // Supported LUT file extensions
    private val lutExtensions = listOf(".cube", ".png", ".bin")
    
    fun getLutBrands(context: Context): List<LutBrand> {
        val assetManager = context.assets
        val brands = mutableListOf<LutBrand>()
        
        try {
            val rootPath = "luts"
            val brandFolders = assetManager.list(rootPath) ?: return emptyList()
            
            for (brandName in brandFolders) {
                val brandPath = "$rootPath/$brandName"
                val contents = assetManager.list(brandPath) ?: continue
                
                val categories = mutableListOf<LutCategory>()
                
                // Check if brand has flat structure (LUT files directly in brand folder)
                val directLutFiles = contents.filter { file -> 
                    lutExtensions.any { ext -> file.endsWith(ext, ignoreCase = true) }
                }
                
                if (directLutFiles.isNotEmpty()) {
                    // Flat structure (e.g., Nothing) - create a single "All" category
                    val lutItems = directLutFiles.map { filename ->
                        val name = lutExtensions.fold(filename) { acc, ext -> 
                            acc.removeSuffix(ext).removeSuffix(ext.uppercase())
                        }.replace("_", " ")
                        LutItem(
                            name = name,
                            assetPath = "$brandPath/$filename"
                        )
                    }.sortedBy { it.name.lowercase() }
                    
                    categories.add(
                        LutCategory(
                            name = "_all",
                            displayName = categoryDisplayNames["_all"] ?: "All",
                            items = lutItems
                        )
                    )
                }
                
                // Check for subdirectories (category folders)
                val categoryFolders = contents.filter { name ->
                    !lutExtensions.any { ext -> name.endsWith(ext, ignoreCase = true) }
                }
                
                for (categoryName in categoryFolders) {
                    val categoryPath = "$brandPath/$categoryName"
                    val files = assetManager.list(categoryPath) ?: continue
                    
                    val lutItems = files
                        .filter { file -> 
                            lutExtensions.any { ext -> file.endsWith(ext, ignoreCase = true) }
                        }
                        .map { filename ->
                            val name = lutExtensions.fold(filename) { acc, ext -> 
                                acc.removeSuffix(ext).removeSuffix(ext.uppercase())
                            }.replace("_", " ")
                            LutItem(
                                name = name,
                                assetPath = "$categoryPath/$filename"
                            )
                        }
                        .sortedBy { it.name.lowercase() }
                    
                    if (lutItems.isNotEmpty()) {
                        categories.add(
                            LutCategory(
                                name = categoryName,
                                displayName = categoryDisplayNames[categoryName] ?: categoryName.replace("_", " ").replace("-", " - "),
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