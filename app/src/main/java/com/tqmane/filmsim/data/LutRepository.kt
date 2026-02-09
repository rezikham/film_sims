package com.tqmane.filmsim.data

import android.content.Context
import android.content.res.AssetManager
import com.tqmane.filmsim.R
import org.json.JSONObject
import java.io.InputStream

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
    private val lutExtensions = listOf(".cube", ".png", ".bin", ".webp", ".jpg", ".jpeg")

    // Cached mapping data
    private var categoryMappings: Map<String, String>? = null
    private var honorMappings: Map<String, String>? = null
    private var meizuMappings: JSONObject? = null
    private var leicaLuxMappings: Map<String, String>? = null
    private var nubiaMappings: Map<String, String>? = null
    private var filmMappings: Map<String, String>? = null

    // Load JSON from raw resources
    private fun loadJsonFromRaw(context: Context, resourceId: Int): String {
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        return inputStream.bufferedReader().use { it.readText() }
    }

    // Get resource ID for raw file
    private fun getRawResourceId(context: Context, fileName: String): Int {
        return context.resources.getIdentifier(fileName, "raw", context.packageName)
    }

    // Initialize and cache all mappings
    private fun initializeMappings(context: Context) {
        if (categoryMappings == null) {
            try {
                val categoryJson = loadJsonFromRaw(context, getRawResourceId(context, "lut_category_mappings"))
                categoryMappings = JSONObject(categoryJson).getJSONObject("categories").keys().asSequence()
                    .associateWith { key ->
                        JSONObject(categoryJson).getJSONObject("categories").getString(key)
                    }
            } catch (e: Exception) {
                categoryMappings = emptyMap()
            }
        }

        if (honorMappings == null) {
            try {
                val honorJson = loadJsonFromRaw(context, getRawResourceId(context, "lut_honor_mappings"))
                honorMappings = JSONObject(honorJson).getJSONObject("filters").keys().asSequence()
                    .associateWith { key ->
                        JSONObject(honorJson).getJSONObject("filters").getString(key)
                    }
            } catch (e: Exception) {
                honorMappings = emptyMap()
            }
        }

        if (meizuMappings == null) {
            try {
                val meizuJson = loadJsonFromRaw(context, getRawResourceId(context, "lut_meizu_mappings"))
                meizuMappings = JSONObject(meizuJson)
            } catch (e: Exception) {
                meizuMappings = JSONObject()
            }
        }

        if (leicaLuxMappings == null) {
            try {
                val leicaLuxJson = loadJsonFromRaw(context, getRawResourceId(context, "lut_leica_lux_mappings"))
                leicaLuxMappings = JSONObject(leicaLuxJson).getJSONObject("filters").keys().asSequence()
                    .associateWith { key ->
                        JSONObject(leicaLuxJson).getJSONObject("filters").getString(key)
                    }
            } catch (e: Exception) {
                leicaLuxMappings = emptyMap()
            }
        }

        if (nubiaMappings == null) {
            try {
                val nubiaJson = loadJsonFromRaw(context, getRawResourceId(context, "lut_nubia_mappings"))
                nubiaMappings = JSONObject(nubiaJson).getJSONObject("filters").keys().asSequence()
                    .associateWith { key ->
                        JSONObject(nubiaJson).getJSONObject("filters").getString(key)
                    }
            } catch (e: Exception) {
                nubiaMappings = emptyMap()
            }
        }

        if (filmMappings == null) {
            try {
                val filmJson = loadJsonFromRaw(context, getRawResourceId(context, "lut_film_mappings"))
                filmMappings = JSONObject(filmJson).getJSONObject("filters").keys().asSequence()
                    .associateWith { key ->
                        JSONObject(filmJson).getJSONObject("filters").getString(key)
                    }
            } catch (e: Exception) {
                filmMappings = emptyMap()
            }
        }
    }

    // Get string resource ID from name
    private fun getStringResourceId(context: Context, resourceName: String): Int {
        return context.resources.getIdentifier(resourceName, "string", context.packageName)
    }

    private fun isAssetDirectory(assetManager: AssetManager, assetPath: String): Boolean {
        return try {
            val children = assetManager.list(assetPath)
            !children.isNullOrEmpty()
        } catch (_: Exception) {
            false
        }
    }

    private fun isLutAssetFile(assetManager: AssetManager, parentAssetPath: String, name: String): Boolean {
        val leaf = name.substringAfterLast('/')
        val fullPath = "$parentAssetPath/$name"

        // Never treat directories as LUT files.
        if (isAssetDirectory(assetManager, fullPath)) return false

        // Some vendors ship raw LUT binaries without an extension (e.g. OnePlus/Uncategorized/default).
        // Treat extensionless *files* as LUT candidates; non-LUT metadata (xml) still has an extension.
        if (!leaf.contains('.')) return true

        return lutExtensions.any { ext -> leaf.endsWith(ext, ignoreCase = true) }
    }

    private fun stripKnownExtension(fileName: String): String {
        val leaf = fileName.substringAfterLast('/')
        if (!leaf.contains('.')) return leaf
        return lutExtensions.fold(leaf) { acc, ext ->
            acc.removeSuffix(ext).removeSuffix(ext.uppercase())
        }
    }

    private fun selectBestVariant(variants: List<String>): String {
        fun priority(name: String): Int {
            val leaf = name.substringAfterLast('/')
            if (!leaf.contains('.')) return 0 // extensionless raw bin
            return when {
                leaf.endsWith(".bin", ignoreCase = true) -> 1
                leaf.endsWith(".cube", ignoreCase = true) -> 2
                leaf.endsWith(".png", ignoreCase = true) -> 3
                leaf.endsWith(".webp", ignoreCase = true) -> 4
                leaf.endsWith(".jpg", ignoreCase = true) || leaf.endsWith(".jpeg", ignoreCase = true) -> 5
                else -> 9
            }
        }
        return variants.minBy { priority(it) }
    }
    
    // Category name to string resource ID mapping
    private fun getCategoryDisplayName(context: Context, categoryName: String): String {
        initializeMappings(context)

        val resourceName = categoryMappings?.get(categoryName)
        if (resourceName != null) {
            val resourceId = getStringResourceId(context, resourceName)
            if (resourceId != 0) {
                return try {
                    context.getString(resourceId)
                } catch (e: Exception) {
                    categoryName.replace("_", " ").replace("-", " - ")
                }
            }
        }

        // Fallback - keep original name for Fujifilm, Kodak Film, etc.
        return categoryName.replace("_", " ").replace("-", " - ")
    }
    
    // Film folder LUT filename to localized display name
    private fun getFilmLutName(context: Context, fileName: String): String {
        initializeMappings(context)

        // Check for partial matches in film mappings
        val mappings = filmMappings ?: emptyMap()
        for ((key: String, resourceName: String) in mappings.entries) {
            if (fileName.contains(key, ignoreCase = true)) {
                val resourceId = getStringResourceId(context, resourceName)
                if (resourceId != 0) {
                    return try {
                        context.getString(resourceId)
                    } catch (e: Exception) {
                        fileName.replace("_", " ")
                    }
                }
            }
        }

        return fileName.replace("_", " ")
    }
    
    // Honor filter filename to localized display name
    private fun getHonorFilterName(context: Context, fileName: String): String {
        initializeMappings(context)

        // Handle hn_ prefix recursively
        if (fileName.startsWith("hn_", ignoreCase = true)) {
            val name = fileName.removePrefix("hn_")
            return getHonorFilterName(context, name)
        }

        // Check for exact match in mappings
        val resourceName = honorMappings?.get(fileName.lowercase())
        if (resourceName != null) {
            val resourceId = getStringResourceId(context, resourceName)
            if (resourceId != 0) {
                return try {
                    context.getString(resourceId)
                } catch (e: Exception) {
                    fileName.replace("_", " ")
                }
            }
        }

        return fileName.replace("_", " ")
    }

    // Meizu filter filename to localized display name
    private fun getMeizuFilterName(context: Context, fileName: String, categoryName: String): String {
        initializeMappings(context)

        // classicFilter camera_* prefix handling
        if (categoryName == "classicFilter") {
            // Check for filtertable files
            if (fileName.contains("filtertable")) {
                val suffix = fileName.removePrefix("filtertable_rgb_second_")
                return suffix.replaceFirstChar { it.titlecase() }
            }

            // Check classicFilter mappings
            val classicFilterJson = meizuMappings?.optJSONObject("classicFilter")
            if (classicFilterJson != null) {
                for (key in classicFilterJson.keys()) {
                    if (fileName.contains(key, ignoreCase = true)) {
                        val filterInfo = classicFilterJson.getJSONObject(key)
                        val resourceName = if (fileName.contains("front", ignoreCase = true)) {
                            filterInfo.optString("front", filterInfo.optString("default"))
                        } else {
                            filterInfo.optString("default")
                        }

                        if (resourceName.isNotEmpty()) {
                            val resourceId = getStringResourceId(context, resourceName)
                            if (resourceId != 0) {
                                return try {
                                    context.getString(resourceId)
                                } catch (e: Exception) {
                                    fileName.replace("_", " ")
                                }
                            }
                        }
                    }
                }
            }
            return fileName.replace("_", " ")
        }

        // General folder
        if (categoryName == "General") {
            val generalJson = meizuMappings?.optJSONObject("General")
            if (generalJson != null) {
                val resourceName = generalJson.optString(fileName.lowercase(), "")
                if (resourceName.isNotEmpty()) {
                    val resourceId = getStringResourceId(context, resourceName)
                    if (resourceId != 0) {
                        return try {
                            context.getString(resourceId)
                        } catch (e: Exception) {
                            fileName.replaceFirstChar { it.titlecase() }.replace("_", " ")
                        }
                    }
                }
            }
            return fileName.replaceFirstChar { it.titlecase() }.replace("_", " ")
        }

        // aiFilters and filterManager: already have nice names (Bright, Gentle, etc.)
        return fileName.replace("_", " ")
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
        initializeMappings(context)

        // Check for partial matches in Leica Lux mappings
        val mappings = leicaLuxMappings ?: emptyMap()
        for ((key: String, resourceName: String) in mappings.entries) {
            if (fileName.contains(key, ignoreCase = true)) {
                val resourceId = getStringResourceId(context, resourceName)
                if (resourceId != 0) {
                    return try {
                        context.getString(resourceId)
                    } catch (e: Exception) {
                        fileName.replace("_", " ")
                    }
                }
            }
        }

        return fileName.replace("_", " ")
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
        initializeMappings(context)

        // Check for matches in Nubia mappings
        // Handle special cases with conditions
        if (fileName.startsWith("renxiang", ignoreCase = true)) {
            return when {
                fileName.contains("skin 2", ignoreCase = true) -> {
                    val resourceName = nubiaMappings?.get("renxiang_skin_2")
                    getResourceString(context, resourceName, fileName)
                }
                fileName.contains("skin", ignoreCase = true) -> {
                    val resourceName = nubiaMappings?.get("renxiang_skin")
                    getResourceString(context, resourceName, fileName)
                }
                fileName.contains("bg", ignoreCase = true) -> {
                    val resourceName = nubiaMappings?.get("renxiang_bg")
                    getResourceString(context, resourceName, fileName)
                }
                else -> fileName.replace("_", " ")
            }
        }

        if (fileName.startsWith("shenghuo", ignoreCase = true)) {
            return when {
                fileName.contains("skin", ignoreCase = true) -> {
                    val resourceName = nubiaMappings?.get("shenghuo_skin")
                    getResourceString(context, resourceName, fileName)
                }
                fileName.contains("bg", ignoreCase = true) -> {
                    val resourceName = nubiaMappings?.get("shenghuo_bg")
                    getResourceString(context, resourceName, fileName)
                }
                else -> fileName.replace("_", " ")
            }
        }

        // Check for prefix matches
        val mappings = nubiaMappings ?: emptyMap()
        for ((key: String, resourceName: String) in mappings.entries) {
            if (fileName.startsWith(key, ignoreCase = true)) {
                val resourceId = getStringResourceId(context, resourceName)
                if (resourceId != 0) {
                    return try {
                        context.getString(resourceId)
                    } catch (e: Exception) {
                        fileName.replace("_", " ")
                    }
                }
            }
        }

        return fileName.replace("_", " ")
    }

    // Helper function to get resource string with fallback
    private fun getResourceString(context: Context, resourceName: String?, fallback: String): String {
        if (resourceName != null) {
            val resourceId = getStringResourceId(context, resourceName)
            if (resourceId != 0) {
                return try {
                    context.getString(resourceId)
                } catch (e: Exception) {
                    fallback.replace("_", " ")
                }
            }
        }
        return fallback.replace("_", " ")
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
                val isHonor = brandName == "Honor"
                val isMeizu = brandName == "Meizu"
                
                // Check if brand has flat structure (LUT files directly in brand folder)
                val directLutFiles = contents.filter { file -> isLutAssetFile(assetManager, brandPath, file) }
                
                if (directLutFiles.isNotEmpty()) {
                    // Flat structure (e.g., Nothing) - create a single "All" category
                    
                    // Group by basename to handle duplicates (prefer .bin > .cube > .png)
                    val groupedFiles = directLutFiles.groupBy { filename -> stripKnownExtension(filename) }
                    
                    val lutItems = groupedFiles.map { (baseName, files) ->
                        val selectedFile = selectBestVariant(files)
                            
                        val displayName = when {
                            isLeicaLux -> getLeicaLuxFilterName(context, baseName)
                            isNubia -> getNubiaFilterName(context, baseName)
                            isVivo -> getVivoFilterName(baseName)
                            isHonor -> getHonorFilterName(context, baseName)
                            isMeizu -> getMeizuFilterName(context, baseName, "_all")
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
                val categoryFolders = contents.filter { name -> isAssetDirectory(assetManager, "$brandPath/$name") }
                
                for (categoryName in categoryFolders) {
                    val categoryPath = "$brandPath/$categoryName"
                    val files = assetManager.list(categoryPath) ?: continue
                    
                    // Group by basename to handle duplicates (prefer .bin > .cube > .png)
                    val groupedFiles = files
                        .filter { file -> isLutAssetFile(assetManager, categoryPath, file) }
                        .groupBy { filename -> stripKnownExtension(filename) }

                    val lutItems = groupedFiles.map { (baseName, variants) ->
                        val selectedFile = selectBestVariant(variants)

                        val isFilmCategory = categoryName == "Film"
                        val displayName = when {
                            isLeicaLux -> getLeicaLuxFilterName(context, baseName)
                            isFilmCategory -> getFilmLutName(context, baseName)
                            isVivo -> getVivoFilterName(baseName)
                            isHonor -> getHonorFilterName(context, baseName)
                            isMeizu -> getMeizuFilterName(context, baseName, categoryName)
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