package com.example.travelsmartmenu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDishesScreen(
    dishes: List<TranslatedMenuItem>,
    selectedLanguage: String,
    onBackClick: () -> Unit
) {
    val bgCream = Color(0xFFF7F7F2)
    val darkGreen = Color(0xFF193E29)
    val cardSage = Color(0xFFE7EDE8)
    val textMuted = Color(0xFF5A655E)
    val isRomanian = selectedLanguage == "Română"

    var selectedDish by remember { mutableStateOf<TranslatedMenuItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRomanian) "Toate preparatele (${dishes.size})"
                        else "All Dishes (${dishes.size})",
                        fontWeight = FontWeight.Bold,
                        color = darkGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = darkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgCream)
            )
        },
        containerColor = bgCream
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dishes) { dish ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFDCDFD9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDish = dish }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dish.translatedName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkGreen
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = dish.originalName,
                                fontSize = 14.sp,
                                color = textMuted
                            )
                        }

                        dish.extractedPrice?.let { price ->
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                color = cardSage,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = price,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = darkGreen
                                    )
                                    dish.convertedPriceText?.let { converted ->
                                        Text(
                                            text = converted,
                                            fontSize = 12.sp,
                                            color = textMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedDish?.let { dish ->
        DishDetailDialog(
            dish = dish,
            selectedLanguage = selectedLanguage,
            onDismiss = { selectedDish = null }
        )
    }
}