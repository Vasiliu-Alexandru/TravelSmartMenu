package com.example.travelsmartmenu

data class ScreenStrings(
    val headline: String,
    val subtitle: String,
    val literalTranslationLabel: String,
    val literalTranslationExample: String,
    val realMeaningLabel: String,
    val realMeaningExample: String,
    val whereEatingLabel: String,
    val countryName: String,
    val moreCountriesSoon: String,
    val tailorProvinceTitle: String,
    val tailorProvinceSubtitle: String,
    val foreignMenusLabel: String,
    val scanMenuButton: String
)

object AppStrings {
    val english = ScreenStrings(
        headline = "No guesswork.\nAll offline.",
        subtitle = "Scan a menu. Understand every dish. Order something you'll love.",
        literalTranslationLabel = "LITERAL TRANSLATION",
        literalTranslationExample = "蚂蚁上树  →  \"Ants Climbing a Tree\"",
        realMeaningLabel = "REAL MEANING",
        realMeaningExample = "Savory minced pork clinging to glassy vermicelli noodles in a spicy Sichuan sauce.",
        whereEatingLabel = "Where are you eating?",
        countryName = "China",
        moreCountriesSoon = "More countries coming soon",
        tailorProvinceTitle = "Tailor dish info to current province's style",
        tailorProvinceSubtitle = "Helps identify regional variations of dishes.",
        foreignMenusLabel = "Foreign menus, in your language",
        scanMenuButton = "Scan a menu"
    )

    val romana = ScreenStrings(
        headline = "Fără presupuneri.\nComplet offline.",
        subtitle = "Scanează un meniu. Înțelege fiecare preparat. Comandă ceva pe gustul tău.",
        literalTranslationLabel = "TRADUCERE LITERALĂ",
        literalTranslationExample = "蚂蚁上树  →  \"Furnici urcând într-un copac\"",
        realMeaningLabel = "SENSUL REAL",
        realMeaningExample = "Carne tocată de porc savuroasă cu tăiței transparenți de fasole în sos picant Sichuan.",
        whereEatingLabel = "Unde mănânci?",
        countryName = "China",
        moreCountriesSoon = "Mai multe țări în curând",
        tailorProvinceTitle = "Adaptează detaliile preparatelor la provincia curentă",
        tailorProvinceSubtitle = "Ajută la identificarea variantelor regionale ale preparatelor.",
        foreignMenusLabel = "Meniuri străine, în limba ta",
        scanMenuButton = "Scanează un meniu"
    )

    fun forLanguage(language: String): ScreenStrings {
        return if (language == "Română") romana else english
    }
}