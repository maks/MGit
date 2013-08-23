package me.sheimi.sgit.utils;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

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
    private Activity mActivity;
    private IabHelper mHelper;
    private static final int INIT = 0;
    private static final int NOT_PAID = 1;
    private static final int PAID = 2;
    private int mPayStatus = INIT;
    static final int RC_REQUEST = 10001;

    private AdUtils(Activity activity) {
        mActivity = activity;
        mHelper = new IabHelper(activity, Constants.BASE64_PUBLIC_KEY);
        if (CommonUtils.isDebug(activity)) {
            mHelper.enableDebugLogging(true);
        }

    }

    public static AdUtils getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new AdUtils(activity);
        }
        return mInstance;
    }

    public void setupIabHelper(final AdView adView) {
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(AdUtils.class.getName(), "Problem setting up In-app Billing: " +
                            result);
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
                        if (inv.hasPurchase(Constants.INAPP_BILLING_ADS)) {
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

    public IabHelper getIabHelper() {
        return mHelper;
    }

    public void loadAds(final AdView adView) {
        switch (mPayStatus) {
            case NOT_PAID:
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
