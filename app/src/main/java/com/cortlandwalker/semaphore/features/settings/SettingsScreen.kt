package com.cortlandwalker.semaphore.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cortlandwalker.semaphore.features.settings.SettingsAction.*
import com.cortlandwalker.semaphore.ui.components.GridBackground

@Composable
fun SettingsScreen(
    state: SettingsState,
    reducer: SettingsReducer
) {
    val backgroundColor = Color(0xFFF8F8FA) // Light grey/white bg
    val cardColor = Color.White
    val textPrimary = Color.Black
    val textSecondary = Color.Gray
    val purplePrimary = Color(0xFF6A5ACD)

    Scaffold(
        containerColor = backgroundColor,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // Grid Header Area
            Box(modifier = Modifier.height(250.dp).fillMaxWidth()) {
                GridBackground(
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = Color(0xFFF2F2F7)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { reducer.postAction(TapBack) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF2D3142)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .offset(y = (-24).dp)
                    .padding(horizontal = 24.dp)
            ) {
                // Section: GENERAL
                SectionHeader("GENERAL")

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        SettingsItem(
                            icon = Icons.Default.Analytics,
                            iconTint = purplePrimary,
                            title = "Analytics",
                            subtitle = "View your workout stats",
                            onClick = { reducer.postAction(SettingsAction.TapAnalytics) }
                        )
                        HorizontalDivider(
                            color = backgroundColor,
                            thickness = 2.dp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        SettingsItem(
                            icon = Icons.Default.Email,
                            iconTint = purplePrimary,
                            title = "Send Feedback",
                            subtitle = "Report bugs or request features",
                            onClick = { reducer.postAction(TapFeedback) }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                SectionHeader("SUPPORT")

                RemoveAdsCard(
                    state = state,
                    reducer = reducer
                )

                Spacer(Modifier.height(24.dp))

                // Section: ABOUT
                SectionHeader("ABOUT")

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        SettingsItem(
                            icon = Icons.AutoMirrored.Filled.Help,
                            iconTint = purplePrimary,
                            title = "FAQ",
                            subtitle = "Questions & Answers",
                            onClick = { reducer.postAction(TapFAQ) }
                        )

                        HorizontalDivider(
                            color = backgroundColor,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        SettingsItem(
                            icon = Icons.Default.Star,
                            iconTint = Color(0xFFFFD700),
                            title = "Rate App",
                            subtitle = "Review on Google Play",
                            onClick = { reducer.postAction(TapRateApp) },
                            showExternalIcon = true
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))

                // Footer Version
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Semaphore v${state.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = textSecondary.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(50.dp))
            }
        }
    }
}

@Composable
private fun RemoveAdsCard(
    state: SettingsState,
    reducer: SettingsReducer
) {
    val monetization = state.monetization

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (monetization.adsRemoved) "Ads removed" else "Remove banner ads",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2D3142)
            )
            Text(
                text = when {
                    monetization.adsRemoved -> "Thanks for supporting Semaphore. Ads stay off on this Google Play account."
                    monetization.isPurchasePending -> "Your purchase is pending. Ads will be removed automatically when Google Play confirms payment."
                    monetization.isLoadingPricing -> "Connecting to Google Play to load pricing..."
                    else -> "Unlock an ad-free workout screen with a one-time purchase."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            if (!monetization.adsRemoved) {
                Button(
                    onClick = { reducer.postAction(TapRemoveAds) },
                    enabled = monetization.canPurchaseRemoveAds,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Remove ads ${monetization.removeAdsPrice}")
                }
            } else {
                Surface(
                    color = Color(0xFFEBE9F8),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Purchase active",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF6A5ACD)
                    )
                }
            }

            TextButton(
                onClick = { reducer.postAction(TapRestorePurchases) },
                enabled = monetization.isBillingAvailable || monetization.adsRemoved
            ) {
                Text("Restore purchase")
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.Gray,
        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showExternalIcon: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Surface(
            shape = CircleShape,
            color = iconTint.copy(alpha = 0.1f), // Subtle tint background
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // Texts
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF2D3142) // Dark text
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Chevron or External Icon
        Icon(
            if (showExternalIcon) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    val reducer = SettingsReducer()
    SettingsScreen(SettingsState(), reducer)
}
