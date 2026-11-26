package com.example.travelsmartmenu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DishDetailDialog(
    dish: TranslatedMenuItem,
    selectedLanguage: String,
    onDismiss: () -> Unit
) {
    val darkGreen = Color(0xFF193E29)
    val bgCream = Color(0xFFF7F7F2)
    val textMuted = Color(0xFF5A655E)
    val isRomanian = selectedLanguage == "Română"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bgCream,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = if (isRomanian) "TITLU ORIGINAL" else "ORIGINAL TITLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = textMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dish.originalName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = darkGreen
                )
            }
        },
        text = {
            Column {
                HorizontalDivider(color = Color(0xFFD2DBD4))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isRomanian) "TRADUCERE" else "TRANSLATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = textMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dish.translatedName,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E2420)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isRomanian) "Închide" else "Close",
                    color = Color.White
                )
            }
        }
    )
}