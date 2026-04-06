package com.cortlandwalker.semaphore.monetization

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.cortlandwalker.semaphore.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonetizationManager @Inject constructor(
    @ApplicationContext context: Context
) : PurchasesUpdatedListener {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _uiState = MutableStateFlow(
        MonetizationUiState(
            adsRemoved = prefs.getBoolean(KEY_ADS_REMOVED, false),
            canShowBannerAds = !prefs.getBoolean(KEY_ADS_REMOVED, false) && BuildConfig.ADMOB_BANNER_AD_UNIT_ID.isNotBlank()
        )
    )
    val uiState: StateFlow<MonetizationUiState> = _uiState.asStateFlow()

    private var billingClient: BillingClient? = null
    private var removeAdsProductDetails: ProductDetails? = null
    private var isConnecting = false

    fun start() {
        val client = billingClient ?: buildBillingClient().also { billingClient = it }
        if (client.isReady) {
            refreshCatalogAndEntitlements(client)
            return
        }

        if (isConnecting) return
        isConnecting = true
        updateState { it.copy(isLoadingPricing = true) }

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    updateState { current ->
                        current.copy(
                            isBillingAvailable = true,
                            isLoadingPricing = true
                        )
                    }
                    refreshCatalogAndEntitlements(client)
                } else {
                    updateState { current ->
                        current.copy(
                            isBillingAvailable = false,
                            isLoadingPricing = false
                        )
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnecting = false
                updateState { current -> current.copy(isBillingAvailable = false) }
            }
        })
    }

    fun restorePurchases() {
        start()
        billingClient?.takeIf { it.isReady }?.let { client ->
            scope.launch {
                queryOwnedPurchases(client)
            }
        }
    }

    fun launchRemoveAdsPurchase(activity: Activity): PurchaseLaunchResult {
        start()
        val client = billingClient ?: return PurchaseLaunchResult.BillingUnavailable
        if (_uiState.value.adsRemoved) return PurchaseLaunchResult.AlreadyOwned
        if (!client.isReady) return PurchaseLaunchResult.BillingUnavailable

        val productDetails = removeAdsProductDetails ?: return PurchaseLaunchResult.ProductUnavailable
        val detailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        productDetails.oneTimePurchaseOfferDetailsList
            ?.firstOrNull()
            ?.offerToken
            ?.let(detailsParamsBuilder::setOfferToken)

        val billingResult = client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(detailsParamsBuilder.build()))
                .build()
        )

        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> PurchaseLaunchResult.Launched
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                restorePurchases()
                PurchaseLaunchResult.AlreadyOwned
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> PurchaseLaunchResult.BillingUnavailable
            else -> PurchaseLaunchResult.Failed(billingResult.debugMessage)
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                scope.launch {
                    handlePurchases(purchases.orEmpty())
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> restorePurchases()
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                updateState { current -> current.copy(isPurchasePending = false) }
            }
            else -> {
                updateState { current -> current.copy(isPurchasePending = false) }
            }
        }
    }

    private fun buildBillingClient(): BillingClient {
        return BillingClient.newBuilder(appContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()
    }

    private fun refreshCatalogAndEntitlements(client: BillingClient) {
        queryProductDetails(client)
        queryOwnedPurchases(client)
    }

    private fun queryProductDetails(client: BillingClient) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BuildConfig.REMOVE_ADS_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            removeAdsProductDetails = productDetailsResult.productDetailsList.firstOrNull {
                it.productId == BuildConfig.REMOVE_ADS_PRODUCT_ID
            }

            updateState { current ->
                current.copy(
                    isBillingAvailable = billingResult.responseCode == BillingClient.BillingResponseCode.OK,
                    isLoadingPricing = false,
                    removeAdsPrice = removeAdsProductDetails
                        ?.oneTimePurchaseOfferDetailsList
                        ?.firstOrNull()
                        ?.formattedPrice
                        ?: current.removeAdsPrice
                )
            }
        }
    }

    private fun queryOwnedPurchases(client: BillingClient) {
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                scope.launch {
                    handlePurchases(purchases)
                }
            } else {
                updateState { current ->
                    current.copy(
                        isBillingAvailable = false,
                        isLoadingPricing = false
                    )
                }
            }
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        val removeAdsPurchase = purchases.firstOrNull { purchase ->
            purchase.products.contains(BuildConfig.REMOVE_ADS_PRODUCT_ID)
        }

        when (removeAdsPurchase?.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (!removeAdsPurchase.isAcknowledged) {
                    acknowledgePurchase(removeAdsPurchase)
                }
                persistAdsRemoved(true)
                updateState { current ->
                    current.copy(
                        adsRemoved = true,
                        isPurchasePending = false
                    )
                }
            }
            Purchase.PurchaseState.PENDING -> {
                updateState { current ->
                    current.copy(
                        adsRemoved = false,
                        isPurchasePending = true
                    )
                }
            }
            else -> {
                persistAdsRemoved(false)
                updateState { current ->
                    current.copy(
                        adsRemoved = false,
                        isPurchasePending = false
                    )
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        billingClient?.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        ) { }
    }

    private fun persistAdsRemoved(adsRemoved: Boolean) {
        prefs.edit { putBoolean(KEY_ADS_REMOVED, adsRemoved) }
    }

    private fun updateState(
        transform: (MonetizationUiState) -> MonetizationUiState
    ) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(
                canShowBannerAds = !updated.adsRemoved && BuildConfig.ADMOB_BANNER_AD_UNIT_ID.isNotBlank(),
                canPurchaseRemoveAds = !updated.adsRemoved &&
                    updated.isBillingAvailable &&
                    removeAdsProductDetails != null
            )
        }
    }

    private companion object {
        const val PREFS_NAME = "semaphore_monetization"
        const val KEY_ADS_REMOVED = "ads_removed"
    }
}
