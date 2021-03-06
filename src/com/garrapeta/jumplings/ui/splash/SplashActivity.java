package com.garrapeta.jumplings.ui.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.garrapeta.jumplings.R;
import com.garrapeta.jumplings.ui.menu.MenuActivity;
import com.garrapeta.jumplings.util.AdsHelper;
import com.garrapeta.jumplings.util.FlurryHelper;

/**
 * Activity implementing the Splash screen
 */
public class SplashActivity extends Activity implements OnClickListener {

    private View mTitleView;
    private View mSubtitleView;
    private boolean mQuickExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        View root = findViewById(R.id.splash_root);
        root.setOnClickListener(this);

        mTitleView = findViewById(R.id.splash_title);
        mSubtitleView = findViewById(R.id.splash_subtitle);

        mTitleView.setVisibility(View.INVISIBLE);
        mSubtitleView.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onAnimationPhaseOne();
            }
        }, 300);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryHelper.onStartSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryHelper.onEndSession(this);
    }

    private void onAnimationPhaseOne() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!mQuickExit) {
                    onAnimationPhaseTwo();
                }
            }
        });

        // doing this here instead of onAnimationStart because of problems of
        // the animation not starting in old devices
        mTitleView.setVisibility(View.VISIBLE);
        mTitleView.startAnimation(animation);
    }

    private void onAnimationPhaseTwo() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!mQuickExit) {
                    onAnimationPhaseThree();
                }
            }
        });
        // doing this here instead of onAnimationStart because of problems of
        // the animation not starting in old devices
        mSubtitleView.setVisibility(View.VISIBLE);
        mSubtitleView.startAnimation(animation);
    }

    private void onAnimationPhaseThree() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTitleView.setVisibility(View.INVISIBLE);
                mSubtitleView.setVisibility(View.INVISIBLE);
                openMenuActivity();
            }
        });
        // doing this here instead of onAnimationStart because of problems of
        // the animation not starting in old devices
        mTitleView.startAnimation(animation);
        mSubtitleView.startAnimation(animation);
    }

    private void openMenuActivity() {
        finish();
        overridePendingTransition(0, 0);
        Intent intent = new Intent(SplashActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (AdsHelper.shoulShowAds(SplashActivity.this)) {
            // if the premium state is not known ignore the click
            return;
        }
        mQuickExit = true;
        openMenuActivity();
    }

}
