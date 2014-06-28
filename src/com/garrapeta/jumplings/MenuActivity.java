package com.garrapeta.jumplings;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

import com.garrapeta.gameengine.GameView;
import com.garrapeta.gameengine.utils.L;
import com.garrapeta.jumplings.flurry.FlurryHelper;
import com.garrapeta.jumplings.ui.PurchaseDialogFactory;
import com.garrapeta.jumplings.ui.PurchaseDialogFactory.PurchaseDialogFragment.PurchaseDialogListener;
import com.garrapeta.jumplings.util.AdsHelper;
import com.garrapeta.jumplings.util.InAppPurchaseHelper;
import com.garrapeta.jumplings.util.InAppPurchaseHelper.PurchaseCallback;
import com.garrapeta.jumplings.util.InAppPurchaseHelper.PurchaseStateQueryCallback;
import com.garrapeta.jumplings.util.Utils;
import com.garrapeta.jumplings.wave.CampaignWave;
import com.garrapeta.jumplings.wave.MenuWave;
import com.garrapeta.jumplings.wave.TestWave;
import com.google.android.gms.ads.AdView;

/**
 * Activity implementing the menu screen
 */
public class MenuActivity extends FragmentActivity implements PurchaseDialogListener {

    private final static String TAG = MenuActivity.class.getSimpleName();

    /**
     * Tag used to refer to the dialog fragment
     */
    static final String DIALOG_FRAGMENT_TAG = "dialog_fragment_tag";

    private View mTitle;

    private Button mStartBtn;
    private ImageButton mPreferencesBtn;
    private ImageButton mHighScoresBtn;
    private ImageButton mAboutBtn;
    private ImageButton mShareButton;
    private ImageButton mPremiumBtn;

    private AdView mAdView;
    private View mDebugGroup;

    // used to resolve the state of the in app billing purchases
    private InAppPurchaseHelper mInAppPurchaseHelper;

    /** World */
    JumplingsWorld mWorld;

    private boolean mAnimationShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI Setup
        setContentView(R.layout.activity_menu);

        mTitle = findViewById(R.id.menu_title);

        mStartBtn = (Button) findViewById(R.id.menu_playBtn);
        mStartBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });

        mDebugGroup = findViewById(R.id.menu_debug_view_group);

        Button testBtn = (Button) findViewById(R.id.menu_testBtn);
        testBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest();
            }
        });

        Button exitBtn = (Button) findViewById(R.id.menu_exitBtn);
        exitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mHighScoresBtn = (ImageButton) findViewById(R.id.menu_highScoresBtn);
        mHighScoresBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighScores();
            }
        });

        mShareButton = (ImageButton) findViewById(R.id.menu_shareBtn);
        mShareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryHelper.logShareButtonClicked();
                Utils.share(MenuActivity.this, getString(R.string.menu_share));
            }
        });

        mPremiumBtn = (ImageButton) findViewById(R.id.menu_premiumBtn);
        mPremiumBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = PurchaseDialogFactory.create();
                dialog.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        });

        mPreferencesBtn = (ImageButton) findViewById(R.id.menu_preferencesBtn);
        mPreferencesBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreferences();
            }
        });

        mAboutBtn = (ImageButton) findViewById(R.id.menu_aboutBtn);
        mAboutBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbout();
            }
        });

        // Ads
        mAdView = (AdView) findViewById(R.id.menu_advertising_banner_view);

        // The UI starts invisible and becomes visible with an animation
        mTitle.setVisibility(View.INVISIBLE);
        mStartBtn.setVisibility(View.INVISIBLE);
        mPreferencesBtn.setVisibility(View.INVISIBLE);
        mHighScoresBtn.setVisibility(View.INVISIBLE);
        mAboutBtn.setVisibility(View.INVISIBLE);
        mDebugGroup.setVisibility(PermData.areDebugFeaturesEnabled(this) ? View.INVISIBLE : View.GONE);
        mShareButton.setVisibility(View.INVISIBLE);
        mAdView.setVisibility(View.INVISIBLE);
        mPremiumBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (L.sEnabled)
            Log.i(JumplingsApplication.TAG, "onStart " + this);

        // Query the state of the purchase
        mInAppPurchaseHelper = new InAppPurchaseHelper(this);
        if (PermData.isPremiumPurchaseStateKnown(this)) {
            if (L.sEnabled)
                Log.d(TAG, "Premium purchase state known. No need to query.");
            startAnimation(PermData.isPremiumPurchased(this));
        } else {
            if (L.sEnabled)
                Log.d(TAG, "Premium purchase state unknown. Querying for it.");
            mInAppPurchaseHelper.queryIsPremiumPurchasedAsync(this, new PurchaseStateQueryCallback() {
                @Override
                public void onPurchaseStateQueryFinished(boolean purchased) {
                    startAnimation(purchased);
                }

                @Override
                public void onPurchaseStateQueryError(String message) {
                    if (L.sEnabled)
                        Log.i(TAG, "Error querying purchase state " + message);
                    // we assume it is purchased
                    startAnimation(true);
                }
            });
        }

        FlurryHelper.onStartSession(this);

        mWorld = new JumplingsWorld(this, (GameView) findViewById(R.id.menu_gamesurface), this);
        mWorld.setDrawDebugInfo(PermData.areDebugFeaturesEnabled(this));

        // Wave setup
        mWorld.mWave = new MenuWave(mWorld);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (L.sEnabled)
            Log.i(JumplingsApplication.TAG, "onPause " + this);
        if (mWorld.isRunning()) {
            mWorld.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (L.sEnabled)
            Log.i(JumplingsApplication.TAG, "onResume " + this);
        mWorld.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (L.sEnabled)
            Log.i(JumplingsApplication.TAG, "onStop " + this);
        FlurryHelper.onEndSession(this);

        if (mWorld.isRunning()) {
            mWorld.finish();
            mWorld = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (L.sEnabled)
            Log.i(JumplingsApplication.TAG, "onDestroy " + this);
        super.onDestroy();
        if (mInAppPurchaseHelper != null) {
            mInAppPurchaseHelper.dispose();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (L.sEnabled)
            Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mInAppPurchaseHelper != null && mInAppPurchaseHelper.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startAnimation(boolean purchased) {
        onPremiumStateUpdate(purchased);
        if (!mAnimationShown) {
            mAnimationShown = true;
            onStartAnimationPhaseOne();
        }
    }

    private void onStartAnimationPhaseOne() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.menu_screen_scale_in);
        fadeInAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onStartAnimationPhaseTwo();
            }
        });

        // doing this here instead of onAnimationStart because of problems of
        // the animation not starting in old devices
        mTitle.setVisibility(View.VISIBLE);
        mTitle.startAnimation(fadeInAnimation);
    }

    private void onStartAnimationPhaseTwo() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeInAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });

        // doing this here instead of onAnimationStart because of problems of
        // the animation not starting in old devices
        mStartBtn.setVisibility(View.VISIBLE);
        mStartBtn.setVisibility(View.VISIBLE);
        mPreferencesBtn.setVisibility(View.VISIBLE);
        mHighScoresBtn.setVisibility(View.VISIBLE);
        mAboutBtn.setVisibility(View.VISIBLE);
        mDebugGroup.setVisibility(PermData.areDebugFeaturesEnabled(this) ? View.VISIBLE : View.GONE);
        mShareButton.setVisibility(View.VISIBLE);
        if (AdsHelper.shoulShowAds(this)) {
            mAdView.setVisibility(View.VISIBLE);
            mPremiumBtn.setVisibility(View.VISIBLE);
            AdsHelper.requestAd(mAdView);
        } else {
            mAdView.setVisibility(View.GONE);
            mPremiumBtn.setVisibility(View.GONE);
        }

        mStartBtn.startAnimation(fadeInAnimation);
        mPreferencesBtn.startAnimation(fadeInAnimation);
        mHighScoresBtn.startAnimation(fadeInAnimation);
        mAboutBtn.startAnimation(fadeInAnimation);
        mDebugGroup.startAnimation(fadeInAnimation);
        mAdView.startAnimation(fadeInAnimation);
        mShareButton.startAnimation(fadeInAnimation);
        mPremiumBtn.startAnimation(fadeInAnimation);
    }

    private void startNewGame() {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.WAVE_BUNDLE_KEY, CampaignWave.WAVE_KEY);
        startActivity(i);
    }

    private void startTest() {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.WAVE_BUNDLE_KEY, TestWave.WAVE_KEY);
        startActivity(i);

    }

    private void showHighScores() {
        Intent intent = new Intent(this, HighScoreListingActivity.class);
        RectF worldBoundaries = mWorld.mViewport.getWorldBoundaries();
        HighScoreListingActivity.putScreenSizeExtras(intent, worldBoundaries.width(), worldBoundaries.height());
        startActivity(intent);
    }

    private void showPreferences() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
    }

    private void showAbout() {
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    private void onPremiumStateUpdate(boolean purchased) {
        if (L.sEnabled)
            Log.i(TAG, "Premium upgrade purchased: " + purchased);
        if (!AdsHelper.shoulShowAds(this)) {
            // this will prevent the animations to start, and the views will
            // never become visible
            mAdView.setVisibility(View.GONE);
            mPremiumBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPurchaseBtnClicked() {
        FlurryHelper.logBuyBtnClickedFromHome();
        mInAppPurchaseHelper.purchasePremiumAsync(this, new PurchaseCallback() {
            @Override
            public void onPurchaseFinished(boolean purchased) {
                FlurryHelper.logPurchasedFromHome();
                onPremiumStateUpdate(purchased);
            }

            @Override
            public void onPurchaseError(String message) {
                if (L.sEnabled)
                    Log.i(TAG, "Error querying purchase state " + message);
            }
        });
    }

}