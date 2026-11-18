package com.example.travelsmartmenu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class OnboardingViewModel : ViewModel() {
    var selectedCountry by mutableStateOf("China")
        private set

    var tailorToProvince by mutableStateOf(true)
        private set

    var selectedLanguage by mutableStateOf("English")
        private set

    fun toggleTailorToProvince(checked: Boolean) {
        tailorToProvince = checked
    }

    fun updateLanguage(language: String) {
        selectedLanguage = language
    }
}