package com.example.travelsmartmenu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                OnboardingScreen()
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel()
) {
    val bgCream = Color(0xFFF7F7F2)
    val darkGreen = Color(0xFF193E29)
    val cardSage = Color(0xFFE7EDE8)
    val textMuted = Color(0xFF5A655E)
    val borderLight = Color(0xFFDCDFD9)

    // Automatically switches whenever viewModel.selectedLanguage changes!
    val strings = AppStrings.forLanguage(viewModel.selectedLanguage)

    Scaffold(
        containerColor = bgCream
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // 1. Headline & Subtitle
            Text(
                text = strings.headline,
                fontSize = 34.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = darkGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = strings.subtitle,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = textMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Translation Example Card
            Surface(
                color = cardSage,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = strings.literalTranslationLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.literalTranslationExample,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E2420)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color(0xFFD2DBD4)
                    )

                    Text(
                        text = strings.realMeaningLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.realMeaningExample,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = Color(0xFF1E2420)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Country Selector
            Text(
                text = strings.whereEatingLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = darkGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.countryName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E2420)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select country",
                        tint = textMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.moreCountriesSoon,
                fontSize = 12.sp,
                color = Color(0xFF8A948E)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Province Tailoring Checkbox Card
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, borderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.toggleTailorToProvince(!viewModel.tailorToProvince)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = viewModel.tailorToProvince,
                        onCheckedChange = { viewModel.toggleTailorToProvince(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = darkGreen,
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.tailorProvinceTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E2420)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.tailorProvinceSubtitle,
                            fontSize = 12.sp,
                            color = textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Language Toggle
            Text(
                text = strings.foreignMenusLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = darkGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardSage)
                    .padding(4.dp)
            ) {
                listOf("English", "Română").forEach { lang ->
                    val isSelected = viewModel.selectedLanguage == lang
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { viewModel.updateLanguage(lang) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = darkGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = lang,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF1E2420) else textMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Primary CTA Button
            Button(
                onClick = { /* Navigate to scanner screen next */ },
                colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.scanMenuButton,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}