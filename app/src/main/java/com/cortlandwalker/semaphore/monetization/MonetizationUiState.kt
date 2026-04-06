package com.cortlandwalker.semaphore.monetization

data class MonetizationUiState(
    val adsRemoved: Boolean = false,
    val canShowBannerAds: Boolean = false,
    val canPurchaseRemoveAds: Boolean = false,
    val isBillingAvailable: Boolean = false,
    val isLoadingPricing: Boolean = true,
    val isPurchasePending: Boolean = false,
    val removeAdsPrice: String = "$1.29"
)

sealed interface PurchaseLaunchResult {
    data object Launched : PurchaseLaunchResult
    data object AlreadyOwned : PurchaseLaunchResult
    data object BillingUnavailable : PurchaseLaunchResult
    data object ProductUnavailable : PurchaseLaunchResult
    data class Failed(val debugMessage: String) : PurchaseLaunchResult
}
