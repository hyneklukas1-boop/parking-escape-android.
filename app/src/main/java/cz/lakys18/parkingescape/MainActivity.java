package cz.lakys18.parkingescape;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
    private static final String TEST_REWARDED_AD_UNIT = "ca-app-pub-3940256099942544/5224354917";
    private static final String TEST_INTERSTITIAL_AD_UNIT = "ca-app-pub-3940256099942544/1033173712";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableFullscreen();

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

        setContentView(webView);
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
        RewardedAd.load(this, TEST_REWARDED_AD_UNIT, new AdRequest.Builder().build(),
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
        InterstitialAd.load(this, TEST_INTERSTITIAL_AD_UNIT, new AdRequest.Builder().build(),
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

    private void enableFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableFullscreen();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableFullscreen();
        }
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
