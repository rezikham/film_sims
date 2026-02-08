package com.tqmane.filmsim.data

import android.content.Context
import com.tqmane.filmsim.R

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
    
    // Supported LUT file extensions
    private val lutExtensions = listOf(".cube", ".png", ".bin")
    
    // Category name to string resource ID mapping
    private fun getCategoryDisplayName(context: Context, categoryName: String): String {
        return when (categoryName) {
            // OnePlus categories
            "App Filters" -> context.getString(R.string.category_app_filters)
            "Artistic" -> context.getString(R.string.category_artistic)
            "Black & White" -> context.getString(R.string.category_black_white)
            "Cinematic Movie" -> context.getString(R.string.category_cinematic_movie)
            "Cool Tones" -> context.getString(R.string.category_cool_tones)
            "Food" -> context.getString(R.string.category_food)
            "Golden Touch" -> context.getString(R.string.category_golden_touch)
            "Instagram Filters" -> context.getString(R.string.category_instagram_filters)
            "Japanese Style" -> context.getString(R.string.category_japanese_style)
            "Landscape" -> context.getString(R.string.category_landscape)
            "Night" -> context.getString(R.string.category_night)
            "Portrait" -> context.getString(R.string.category_portrait)
            "Uncategorized" -> context.getString(R.string.category_uncategorized)
            "Vintage-Retro" -> context.getString(R.string.category_vintage_retro)
            "Warm Tones" -> context.getString(R.string.category_warm_tones)
            // Xiaomi categories
            "Cinematic" -> context.getString(R.string.category_cinematic)
            "Film Simulation" -> context.getString(R.string.category_film_simulation)
            "Monochrome" -> context.getString(R.string.category_monochrome)
            "Nature-Landscape" -> context.getString(R.string.category_nature_landscape)
            "Portrait-Soft" -> context.getString(R.string.category_portrait_soft)
            "Special Effects" -> context.getString(R.string.category_special_effects)
            "Vivid-Natural" -> context.getString(R.string.category_vivid_natural)
            "Warm-Vintage" -> context.getString(R.string.category_warm_vintage)
            // Leica_lux categories
            "Leica Looks" -> context.getString(R.string.category_leica_looks)
            "Artist Looks" -> context.getString(R.string.category_artist_looks)
            // Film category
            "Film" -> context.getString(R.string.category_film)
            // Vivo categories
            "newfilter" -> context.getString(R.string.category_camera_filters)
            "editor_filters" -> context.getString(R.string.category_editor_filters)
            "movielut" -> context.getString(R.string.category_movie)
            "portraitstylefilter" -> context.getString(R.string.category_portrait_style)
            "portraitstylefilter_multistyle" -> context.getString(R.string.category_portrait_multistyle)
            "collage_filters" -> context.getString(R.string.category_collage_filters)
            "nightstylefilter" -> context.getString(R.string.category_night_style)
            "superzoom" -> context.getString(R.string.category_superzoom)
            // Common/Nothing
            "_all" -> context.getString(R.string.category_all)
            // Fallback - keep original name for Fujifilm, Kodak Film, etc.
            else -> categoryName.replace("_", " ").replace("-", " - ")
        }
    }
    
    // Film folder LUT filename to localized display name
    private fun getFilmLutName(context: Context, fileName: String): String {
        return when {
            fileName.contains("field", ignoreCase = true) -> context.getString(R.string.lut_film_field)
            fileName.contains("seaside", ignoreCase = true) -> context.getString(R.string.lut_film_seaside)
            fileName.contains("city", ignoreCase = true) -> context.getString(R.string.lut_film_city)
            fileName.contains("neon", ignoreCase = true) -> context.getString(R.string.lut_film_neon)
            fileName.contains("cold_flash", ignoreCase = true) -> context.getString(R.string.lut_film_cold_flash)
            fileName.contains("warm_flash", ignoreCase = true) -> context.getString(R.string.lut_film_warm_flash)
            fileName.contains("vintage", ignoreCase = true) -> context.getString(R.string.lut_film_vintage)
            fileName.contains("clear", ignoreCase = true) -> context.getString(R.string.lut_film_clear)
            fileName.contains("800t", ignoreCase = true) -> context.getString(R.string.lut_film_800t)
            else -> fileName.replace("_", " ")
        }
    }
    
    // Brand name to display name mapping
    private fun getBrandDisplayName(context: Context, brandName: String): String {
        return when (brandName) {
            "Leica_lux" -> context.getString(R.string.brand_leica_lux)
            else -> brandName
        }
    }
    
    // Leica_lux filter filename to localized display name
    private fun getLeicaLuxFilterName(context: Context, fileName: String): String {
        return when {
            fileName.contains("Classic_sRGB") -> context.getString(R.string.lut_leica_classic)
            fileName.contains("Contemporary_sRGB") -> context.getString(R.string.lut_leica_contemporary)
            fileName.contains("Leica-Filter_Monochrome") -> context.getString(R.string.lut_leica_monochrome_natural)
            fileName.contains("Leica-Filter_Natural") -> context.getString(R.string.lut_leica_natural)
            fileName.contains("Leica-Looks_Blue") -> context.getString(R.string.lut_leica_blue)
            fileName.contains("Leica-Looks_Eternal") -> context.getString(R.string.lut_leica_eternal)
            fileName.contains("Leica-Looks_Selenium") -> context.getString(R.string.lut_leica_selenium)
            fileName.contains("Leica-Looks_Sepia") -> context.getString(R.string.lut_leica_sepia)
            fileName.contains("Leica-Looks_Silver") -> context.getString(R.string.lut_leica_silver)
            fileName.contains("Leica-Looks_Teal") -> context.getString(R.string.lut_leica_teal)
            fileName.contains("Leica_Bleach") -> context.getString(R.string.lut_leica_bleach)
            fileName.contains("Leica_Brass") -> context.getString(R.string.lut_leica_brass)
            fileName.contains("Leica_Monochrome_High_Contrast") -> context.getString(R.string.lut_leica_high_contrast)
            fileName.contains("Leica_Vivid") -> context.getString(R.string.lut_leica_vivid)
            fileName.contains("Tyson_100yearsMono") -> context.getString(R.string.lut_100_years_mono)
            fileName.contains("Tyson_GregWilliams_Sepia0") -> context.getString(R.string.lut_greg_williams_sepia_0)
            fileName.contains("Tyson_GregWilliams_Sepia100") -> context.getString(R.string.lut_greg_williams_sepia_100)
            fileName.contains("Tyson_Leica_Base_V3") -> context.getString(R.string.lut_leica_standard)
            fileName.contains("Tyson_Leica_Chrome") -> context.getString(R.string.lut_leica_chrome)
            else -> fileName.replace("_", " ")
        }
    }
    
    // Vivo filter filename to display name
    private fun getVivoFilterName(fileName: String): String {
        var name = fileName
        // Strip common prefixes (longer/more specific first)
        val prefixes = listOf(
            "special_new_filter_", "special_portrait_",
            "new_filter_back_photo_hdr_", "new_filter_",
            "effects_space_filter_",
            "filter_polaroid_", "filter_portrait_style_", "filter_portrait_",
            "filter_",
            "front_filter_portrait_style_",
            "portrait_back_", "portrait_",
            "zeiss_star_light_",
            "pack_film_",
            "polaroid_",
            // editor/collage category prefixes
            "film_", "food_", "fruity_", "human_", "japan_", "night_", "style_"
        )
        for (prefix in prefixes) {
            if (name.startsWith(prefix)) {
                name = name.removePrefix(prefix)
                break
            }
        }
        // Handle remaining front_/back_ prefixes
        if (name.startsWith("front_")) name = name.removePrefix("front_")
        if (name.startsWith("back_")) name = name.removePrefix("back_")
        // Strip redundant suffixes
        name = name.removeSuffix("_lut").removeSuffix("_filter")
        // Title case
        return name.replace("_", " ").split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
    
    // Nubia filter filename to localized display name
    private fun getNubiaFilterName(context: Context, fileName: String): String {
        return when {
            fileName.startsWith("fengjing") -> context.getString(R.string.lut_nubia_landscape)
            fileName.startsWith("meishi") -> context.getString(R.string.lut_nubia_food)
            fileName.startsWith("renxiang") && fileName.contains("bg") -> context.getString(R.string.lut_nubia_portrait_bg)
            fileName.startsWith("renxiang") && fileName.contains("skin 2") -> context.getString(R.string.lut_nubia_portrait_skin_2)
            fileName.startsWith("renxiang") && fileName.contains("skin") -> context.getString(R.string.lut_nubia_portrait_skin)
            fileName.startsWith("richang") -> context.getString(R.string.lut_nubia_daily)
            fileName.startsWith("shenghuo") && fileName.contains("bg") -> context.getString(R.string.lut_nubia_life_bg)
            fileName.startsWith("shenghuo") && fileName.contains("skin") -> context.getString(R.string.lut_nubia_life_skin)
            else -> fileName.replace("_", " ")
        }
    }
    
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
                val isLeicaLux = brandName == "Leica_lux"
                val isVivo = brandName == "Vivo"
                val isNubia = brandName == "Nubia"
                
                // Check if brand has flat structure (LUT files directly in brand folder)
                val directLutFiles = contents.filter { file -> 
                    lutExtensions.any { ext -> file.endsWith(ext, ignoreCase = true) }
                }
                
                if (directLutFiles.isNotEmpty()) {
                    // Flat structure (e.g., Nothing) - create a single "All" category
                    
                    // Group by basename to handle duplicates (prefer .bin > .cube > .png)
                    val groupedFiles = directLutFiles.groupBy { filename ->
                        lutExtensions.fold(filename) { acc, ext -> 
                            acc.removeSuffix(ext).removeSuffix(ext.uppercase())
                        }
                    }
                    
                    val lutItems = groupedFiles.map { (baseName, files) ->
                        // Select best file: .bin -> .cube -> .png
                        val selectedFile = files.find { it.endsWith(".bin", ignoreCase = true) }
                            ?: files.find { it.endsWith(".cube", ignoreCase = true) }
                            ?: files.first()
                            
                        val displayName = when {
                            isLeicaLux -> getLeicaLuxFilterName(context, baseName)
                            isNubia -> getNubiaFilterName(context, baseName)
                            isVivo -> getVivoFilterName(baseName)
                            else -> baseName.replace("_", " ")
                        }
                        LutItem(
                            name = displayName,
                            assetPath = "$brandPath/$selectedFile"
                        )
                    }.sortedBy { it.name.lowercase() }
                    
                    categories.add(
                        LutCategory(
                            name = "_all",
                            displayName = getCategoryDisplayName(context, "_all"),
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
                    
                    // Group by basename to handle duplicates (prefer .bin > .cube > .png)
                    val groupedFiles = files
                        .filter { file -> 
                            lutExtensions.any { ext -> file.endsWith(ext, ignoreCase = true) }
                        }
                        .groupBy { filename ->
                            lutExtensions.fold(filename) { acc, ext -> 
                                acc.removeSuffix(ext).removeSuffix(ext.uppercase())
                            }
                        }

                    val lutItems = groupedFiles.map { (baseName, variants) ->
                        // Select best file: .bin -> .cube -> .png
                        val selectedFile = variants.find { it.endsWith(".bin", ignoreCase = true) }
                            ?: variants.find { it.endsWith(".cube", ignoreCase = true) }
                            ?: variants.first()

                        val isFilmCategory = categoryName == "Film"
                        val displayName = when {
                            isLeicaLux -> getLeicaLuxFilterName(context, baseName)
                            isFilmCategory -> getFilmLutName(context, baseName)
                            isVivo -> getVivoFilterName(baseName)
                            else -> baseName.replace("_", " ")
                        }
                        LutItem(
                            name = displayName,
                            assetPath = "$categoryPath/$selectedFile"
                        )
                    }.sortedBy { it.name.lowercase() }
                    
                    if (lutItems.isNotEmpty()) {
                        categories.add(
                            LutCategory(
                                name = categoryName,
                                displayName = getCategoryDisplayName(context, categoryName),
                                items = lutItems
                            )
                        )
                    }
                }
                
                if (categories.isNotEmpty()) {
                    brands.add(
                        LutBrand(
                            name = brandName,
                            displayName = getBrandDisplayName(context, brandName),
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