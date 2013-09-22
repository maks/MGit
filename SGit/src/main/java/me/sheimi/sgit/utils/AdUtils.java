package me.sheimi.sgit.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import me.sheimi.sgit.R;
import me.sheimi.sgit.RepoListActivity;
import me.sheimi.sgit.utils.inapputil.IabHelper;
import me.sheimi.sgit.utils.inapputil.IabResult;
import me.sheimi.sgit.utils.inapputil.Inventory;
import me.sheimi.sgit.utils.inapputil.Purchase;

/**
 * Created by sheimi on 8/23/13.
 */
public class AdUtils {

    private static AdUtils mInstance;
    private ViewUtils mViewUtils;
    private Activity mActivity;
    private IabHelper mHelper;
    private static final int INIT = 0;
    private static final int NOT_PAID = 1;
    private static final int PAID = 2;
    private static final int NOT_AVAILABLE = 3;
    private int mPayStatus = INIT;
    static final int RC_REQUEST = 10001;

    private AdUtils(Activity activity) {
        mActivity = activity;
        mViewUtils = ViewUtils.getInstance(activity);
    }

    public static AdUtils getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new AdUtils(activity);
        }
        return mInstance;
    }

    public void setupIabHelper(final AdView adView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mPayStatus = NOT_AVAILABLE;
            showAds(adView);
            return;
        }
        Log.d(getClass().getName(), "init Helper");
        mHelper = new IabHelper(mActivity, Constants.BASE64_PUBLIC_KEY);
        if (CommonUtils.isDebug(mActivity)) {
            mHelper.enableDebugLogging(true);
        }
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(AdUtils.class.getName(), "Problem setting up In-app Billing: " +
                            result);
                    mPayStatus = NOT_AVAILABLE;
                    showAds(adView);
                    return;
                }
                Log.d(AdUtils.class.getName(), "In-app Setup Success");
                // Hooray, IAB is fully set up!
                mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                    @Override
                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                        if (result.isFailure()) {
                            Log.d(AdUtils.class.getName(), "Query Fail: " + result);
                            return;
                        }
                        Purchase purchase = inv.getPurchase(Constants.INAPP_BILLING_ADS);
                        // if purchase is not null & purchase is not canceled or refunded
                        if (purchase != null && purchase.getPurchaseState() == 0) {
                            mPayStatus = PAID;
                            hideAds(adView);
                            return;
                        }
                        mPayStatus = NOT_PAID;
                        showAds(adView);
                    }
                });
            }
        });
    }

    public void payToDisableAds(final AdView adView) {
        if (mPayStatus == NOT_AVAILABLE) {
            mViewUtils.showToastMessage(R.string.alert_in_app_billing_not_available);
            return;
        }
        if (mPayStatus == PAID) {
            mViewUtils.showToastMessage(R.string.alert_have_paid);
            return;
        }
        if (mPayStatus == INIT) {
            mViewUtils.showToastMessage(R.string.alert_in_app_billing_not_inited);
            return;
        }
        mHelper.launchPurchaseFlow(mActivity, Constants.INAPP_BILLING_ADS, RC_REQUEST,
                new IabHelper.OnIabPurchaseFinishedListener() {
                    @Override
                    public void onIabPurchaseFinished(IabResult result, Purchase info) {
                        if (result.isFailure()) {
                            Log.d(RepoListActivity.class.getName(), "Pay Fail: " + result);
                            return;
                        }
                        if (info.getSku().equals(Constants.INAPP_BILLING_ADS)) {
                            mPayStatus = PAID;
                            hideAds(adView);
                            return;
                        }
                        Log.d(RepoListActivity.class.getName(), "Pay Fail: " + result);
                    }
                }, Constants.INAPP_BILLING_PAYLOAD);
    }


    public void disposeHelper() {
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPayStatus == NOT_AVAILABLE || mPayStatus == INIT || mPayStatus == PAID)
            return false;
        return mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    public void loadAds(final AdView adView) {
        switch (mPayStatus) {
            case NOT_PAID:
            case NOT_AVAILABLE:
                showAds(adView);
                break;
            case PAID:
            case INIT:
                hideAds(adView);
                break;
        }
    }

    private void showAds(final AdView adView) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adView.loadAd(new AdRequest());
            }
        });
    }

    private void hideAds(final AdView adView) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adView.setVisibility(View.GONE);
            }
        });
    }

}
