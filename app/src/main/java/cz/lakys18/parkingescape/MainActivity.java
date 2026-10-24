package cz.lakys18.parkingescape;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.webkit.WebChromeClient;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public class MainActivity extends Activity {

    private WebView webView;
    private RewardedAd rewardedAd;
    private InterstitialAd interstitialAd;
    private ConsentInformation consentInformation;
    private boolean adsInitialized = false;
    private static final String PROD_REWARDED_AD_UNIT = "ca-app-pub-4896675580672730/2624180321";
    private static final String PROD_INTERSTITIAL_AD_UNIT = "ca-app-pub-4896675580672730/5725072104";
    // Official Google test ad units are used in debug builds so development/testing
    // never generates traffic on the production Parking Escape ad units.
    private static final String TEST_REWARDED_AD_UNIT = "ca-app-pub-3940256099942544/5224354917";
    private static final String TEST_INTERSTITIAL_AD_UNIT = "ca-app-pub-3940256099942544/1033173712";

    private String rewardedAdUnit() {
        return BuildConfig.DEBUG ? TEST_REWARDED_AD_UNIT : PROD_REWARDED_AD_UNIT;
    }

    private String interstitialAdUnit() {
        return BuildConfig.DEBUG ? TEST_INTERSTITIAL_AD_UNIT : PROD_INTERSTITIAL_AD_UNIT;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureSystemBars();

        requestPrivacyConsent();

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new AdsBridge(), "AndroidAds");

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // Android 15/16 can draw the window underneath system bars. Keep the
        // WebView itself inside the safe area so CSS coordinates and touches
        // use the same unobscured viewport, including after rotation.
        FrameLayout content = new FrameLayout(this);
        content.setBackgroundColor(0xFF0C1720);
        content.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safeArea = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        setContentView(content);
        ViewCompat.requestApplyInsets(content);
        webView.loadUrl("file:///android_asset/index.html");
    }

    private void requestPrivacyConsent() {
        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();

        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        this,
                        formError -> initializeAdsIfAllowed()),
                requestConsentError -> initializeAdsIfAllowed());

        initializeAdsIfAllowed();
    }

    private void initializeAdsIfAllowed() {
        if (adsInitialized || consentInformation == null || !consentInformation.canRequestAds()) {
            return;
        }
        adsInitialized = true;
        MobileAds.initialize(this, initializationStatus -> {
            loadRewardedAd();
            loadInterstitialAd();
        });
    }

    private void loadRewardedAd() {
        RewardedAd.load(this, rewardedAdUnit(), new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        rewardedAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        rewardedAd = null;
                    }
                });
    }

    private void loadInterstitialAd() {
        InterstitialAd.load(this, interstitialAdUnit(), new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        interstitialAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        interstitialAd = null;
                    }
                });
    }

    private void showInterstitialAd() {
        runOnUiThread(() -> {
            if (interstitialAd == null) {
                loadInterstitialAd();
                return;
            }
            InterstitialAd ad = interstitialAd;
            interstitialAd = null;
            ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    loadInterstitialAd();
                }
            });
            ad.show(this);
        });
    }

    private void showRewardedAd() {
        runOnUiThread(() -> {
            if (rewardedAd == null) {
                loadRewardedAd();
                webView.evaluateJavascript("window.onRewardedAdUnavailable && window.onRewardedAdUnavailable()", null);
                return;
            }
            RewardedAd ad = rewardedAd;
            rewardedAd = null;
            ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    loadRewardedAd();
                }
            });
            ad.show(this, rewardItem ->
                    webView.evaluateJavascript("window.onRewardedAdEarned && window.onRewardedAdEarned()", null));
        });
    }

    private class AdsBridge {
        @JavascriptInterface
        public void showRewardedAd() {
            MainActivity.this.showRewardedAd();
        }

        @JavascriptInterface
        public void showInterstitialAd() {
            MainActivity.this.showInterstitialAd();
        }
    }

    private void configureSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        controller.show(WindowInsetsCompat.Type.systemBars());
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
