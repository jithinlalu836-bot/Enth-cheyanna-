package com.example.service

import com.example.data.CountryRepository
import com.example.model.CountryItem
import java.util.Locale

object CountryDetector {

    // Extended alias mappings for natural chat comments
    private val aliases = mapOf(
        "TR" to listOf("turkey", "turkiye", "türkiye", "turk", "tr"),
        "IN" to listOf("india", "bharat", "hindustan", "ind"),
        "ID" to listOf("indonesia", "indo", "id"),
        "US" to listOf("usa", "united states", "america", "us", "american"),
        "VN" to listOf("vietnam", "viet nam", "việt nam", "vn"),
        "CN" to listOf("china", "prc", "cn"),
        "RU" to listOf("russia", "rossiya", "ru"),
        "PK" to listOf("pakistan", "pak", "pk"),
        "BD" to listOf("bangladesh", "bangla", "bd"),
        "BR" to listOf("brazil", "brasil", "br"),
        "JP" to listOf("japan", "nippon", "nihon", "jp"),
        "DE" to listOf("germany", "deutschland", "de"),
        "FR" to listOf("france", "français", "fr"),
        "GB" to listOf("uk", "united kingdom", "britain", "england", "gb"),
        "PH" to listOf("philippines", "pilipinas", "pinoy", "ph"),
        "TH" to listOf("thailand", "thai", "th"),
        "CA" to listOf("canada", "ca"),
        "AU" to listOf("australia", "aussie", "au"),
        "MX" to listOf("mexico", "méxico", "mx"),
        "IT" to listOf("italy", "italia", "it"),
        "ES" to listOf("spain", "españa", "es"),
        "KR" to listOf("korea", "south korea", "kr"),
        "AR" to listOf("argentina", "arg", "ar"),
        "SA" to listOf("saudi", "saudi arabia", "ksa", "sa"),
        "EG" to listOf("egypt", "misr", "eg"),
        "NG" to listOf("nigeria", "naija", "ng"),
        "ZA" to listOf("south africa", "mzansi", "za"),
        "NL" to listOf("netherlands", "holland", "nl"),
        "CO" to listOf("colombia", "co"),
        "PL" to listOf("poland", "polska", "pl"),
        "IR" to listOf("iran", "persia", "ir"),
        "MY" to listOf("malaysia", "my"),
        "IQ" to listOf("iraq", "iq"),
        "MA" to listOf("morocco", "maroc", "ma"),
        "UA" to listOf("ukraine", "ua"),
        "DZ" to listOf("algeria", "dz"),
        "PE" to listOf("peru", "pe"),
        "CL" to listOf("chile", "cl"),
        "SE" to listOf("sweden", "sverige", "se"),
        "CH" to listOf("switzerland", "swiss", "ch"),
        "BE" to listOf("belgium", "be"),
        "PT" to listOf("portugal", "pt"),
        "GR" to listOf("greece", "hellas", "gr"),
        "AT" to listOf("austria", "at"),
        "AE" to listOf("uae", "emirates", "dubai", "ae"),
        "SG" to listOf("singapore", "sg"),
        "NZ" to listOf("new zealand", "nz"),
        "NO" to listOf("norway", "norge", "no"),
        "DK" to listOf("denmark", "danmark", "dk"),
        "FI" to listOf("finland", "suomi", "fi"),
        "IE" to listOf("ireland", "ie"),
        "RO" to listOf("romania", "ro"),
        "CZ" to listOf("czech", "czech republic", "cz"),
        "HU" to listOf("hungary", "magyar", "hu"),
        "KZ" to listOf("kazakhstan", "kz"),
        "UZ" to listOf("uzbekistan", "uz"),
        "AZ" to listOf("azerbaijan", "az"),
        "KE" to listOf("kenya", "ke"),
        "GH" to listOf("ghana", "gh"),
        "ET" to listOf("ethiopia", "et"),
        "TZ" to listOf("tanzania", "tz"),
        "VE" to listOf("venezuela", "ve"),
        "EC" to listOf("ecuador", "ec"),
        "GT" to listOf("guatemala", "gt"),
        "CU" to listOf("cuba", "cu"),
        "DO" to listOf("dominican", "dom rep", "do"),
        "NP" to listOf("nepal", "np"),
        "LK" to listOf("sri lanka", "lk"),
        "MM" to listOf("myanmar", "burma", "mm"),
        "KH" to listOf("cambodia", "kh"),
        "QA" to listOf("qatar", "qa"),
        "KW" to listOf("kuwait", "kw"),
        "OM" to listOf("oman", "om"),
        "JO" to listOf("jordan", "jo"),
        "LB" to listOf("lebanon", "lb"),
        "HR" to listOf("croatia", "hrvatska", "hr"),
        "RS" to listOf("serbia", "srbija", "rs"),
        "BG" to listOf("bulgaria", "bg"),
        "SK" to listOf("slovakia", "sk"),
        "UY" to listOf("uruguay", "uy"),
        "PY" to listOf("paraguay", "py")
    )

    /**
     * Inspects a comment text and returns the matching country, if any.
     * Checks for:
     * 1. Exact flag emoji (e.g. 🇹🇷, 🇮🇳, 🇺🇸)
     * 2. Full country name or known aliases (e.g. "Turkey", "turk", "India", "Bharat", "USA")
     */
    fun detectCountry(
        commentText: String,
        allCountries: List<CountryItem>
    ): CountryItem? {
        val trimmed = commentText.trim()
        if (trimmed.isEmpty()) return null

        // 1. Direct Flag emoji match
        for (country in allCountries) {
            if (trimmed.contains(country.flag)) {
                return country
            }
        }

        val normalized = trimmed.lowercase(Locale.ROOT)
        val words = normalized.split(Regex("[\\s,;:.!?/+\\-#*~()\\[\\]{}|'\"`]+"))

        // 2. Exact word / Alias match
        for (country in allCountries) {
            val countryAliases = aliases[country.id] ?: listOf(country.name.lowercase(Locale.ROOT))
            for (alias in countryAliases) {
                // If single word (like "turkey", "india", "us")
                if (words.contains(alias)) {
                    return country
                }
                // If multi-word alias (like "united states", "saudi arabia")
                if (alias.contains(" ") && normalized.contains(alias)) {
                    return country
                }
            }

            // Also check full country name lowercase
            val lowerName = country.name.lowercase(Locale.ROOT)
            if (words.contains(lowerName) || (lowerName.contains(" ") && normalized.contains(lowerName))) {
                return country
            }
        }

        // 3. Fallback: Check if comment starts with or contains any country name
        for (country in allCountries) {
            val lowerName = country.name.lowercase(Locale.ROOT)
            if (lowerName.length >= 4 && normalized.contains(lowerName)) {
                return country
            }
        }

        return null
    }

    /**
     * Checks if the comment represents a Like & Subscribe event
     */
    fun isLikeAndSubscribe(commentText: String): Boolean {
        val lower = commentText.lowercase(Locale.ROOT)
        return (lower.contains("like") && lower.contains("sub")) ||
                lower.contains("liked & subbed") ||
                lower.contains("liked and subscribed") ||
                lower.contains("subscribe +400") ||
                lower.contains("sub +400")
    }
}
