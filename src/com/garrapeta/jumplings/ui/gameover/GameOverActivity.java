package com.garrapeta.jumplings.ui.gameover;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.garrapeta.gameengine.utils.LogX;
import com.garrapeta.jumplings.JumplingsApplication;
import com.garrapeta.jumplings.R;
import com.garrapeta.jumplings.game.Score;
import com.garrapeta.jumplings.ui.game.GameActivity;
import com.garrapeta.jumplings.ui.menu.MenuActivity;
import com.garrapeta.jumplings.ui.scores.ScoresActivity;
import com.garrapeta.jumplings.util.AdsHelper;
import com.garrapeta.jumplings.util.FlurryHelper;
import com.garrapeta.jumplings.util.GooglePlayGamesLeaderboardHelper;
import com.garrapeta.jumplings.util.PermData;
import com.garrapeta.jumplings.util.Utils;
import com.garrapeta.jumplings.view.JumplingsToast;
import com.google.android.gms.ads.AdView;
import com.google.example.games.basegameutils.BaseGameActivity;

/**
 * Actividad para introducir un nuevo High Score
 * 
 * @author GaRRaPeTa
 */
public class GameOverActivity extends BaseGameActivity {

    // -----------------------------------------------------------------
    // Constantes

    /** Clave para pasar highScore entre actividades */
    public static final String NEW_HIGHSCORE_KEY = Score.class.getCanonicalName();

    /** Minimum length of the username */
    private static final int MINIMUM_NAME_LENGTH = 5;

    // ------------------------------------------------------------------
    // Variables

    private Score mPlayerScore;

    private Button mSaveScoreButton;

    private EditText mPlayerNameEditText;

    private View mScoreIntroductionView;
    private View mNextActionView;

    private String mWaveKey;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogX.i(JumplingsApplication.TAG, "onCreate " + this);

        mWaveKey = null;
        mWaveKey = getIntent().getStringExtra(GameActivity.WAVE_BUNDLE_KEY);
        mPlayerScore = (Score) getIntent().getParcelableExtra(NEW_HIGHSCORE_KEY);

        initGui();
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

    private void initGui() {
        setContentView(R.layout.activity_gameover);

        Button shareButton = (Button) findViewById(R.id.gameover_shareBtn);
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryHelper.logShareButtonClicked();
                Utils.share(GameOverActivity.this, getShareScoreMessage());
            }
        });

        final boolean newHighScore = mPlayerScore.mScore > 0 && Score.getLocalHighScoresPosition(this, mPlayerScore.mScore) < Score.MAX_LOCAL_HIGHSCORE_COUNT;

        TextView scoreTextView = (TextView) findViewById(R.id.gameover_scoreTextView);
        final String yourScoreStr = getString(R.string.gameover_your_score, mPlayerScore.mScore);
        scoreTextView.setText(yourScoreStr);

        Score highest = PermData.getLocalHighestScore(this);
        if (highest != null) {
            TextView messageTextView = (TextView) findViewById(R.id.gameover_messageTextView);
            messageTextView.setVisibility(View.VISIBLE);
            long prevHighScore = PermData.getLocalHighestScore(this).mScore;
            if (mPlayerScore.mScore > prevHighScore) {
                messageTextView.setText(R.string.gameover_beaten);
            } else {
                final String highscoreStr = getString(R.string.gameover_highscore_is, prevHighScore);
                messageTextView.setText(highscoreStr);
            }
        }

        mScoreIntroductionView = findViewById(R.id.gameover_nameIntroductionLayout);
        mNextActionView = findViewById(R.id.gameover_nextActionView);

        if (newHighScore) {
            mScoreIntroductionView.setVisibility(View.VISIBLE);
            mNextActionView.setVisibility(View.INVISIBLE);

            mPlayerNameEditText = (EditText) findViewById(R.id.gameover_playerNameEditText);
            mPlayerNameEditText.setText(PermData.getLastPlayerName(this));
            int textLength = mPlayerNameEditText.getText()
                                                .length();
            mPlayerNameEditText.setSelection(textLength, textLength);

            mPlayerNameEditText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onUsernameEditTextChanged(s);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            mPlayerNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
                    // If the event is a key-down event on the "enter"
                    // button
                    // TODO: Event is sometimes null:
                    // http://stackoverflow.com/questions/11301061/null-keyevent-and-actionid-0-in-oneditoraction-jelly-bean-nexus-7
                    if (event != null && (event.getAction() == KeyEvent.ACTION_DOWN) && (actionId == EditorInfo.IME_NULL)) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(GameOverActivity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);
                        return true;
                    }
                    return false;

                }
            });
        } else {
            mScoreIntroductionView.setVisibility(View.INVISIBLE);
            mNextActionView.setVisibility(View.VISIBLE);
        }

        mSaveScoreButton = (Button) findViewById(R.id.gameover_saveScoreBtn);
        mSaveScoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUsernameValid(mPlayerNameEditText.getText())) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mPlayerNameEditText.getWindowToken(), 0);
                    saveHighScore();
                } else {
                    final String message = getResources().getString(R.string.gameover_invalid_username, MINIMUM_NAME_LENGTH);
                    JumplingsToast.show(GameOverActivity.this, message, JumplingsToast.LENGTH_LONG);
                }
            }
        });

        if (mWaveKey != null) {
            Button replayButton = (Button) findViewById(R.id.gameover_replayBtn);
            replayButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    Intent i = new Intent(GameOverActivity.this, GameActivity.class);
                    i.putExtra(GameActivity.WAVE_BUNDLE_KEY, mWaveKey);

                    startActivity(i);
                }
            });
        }

        Button menuButton = (Button) findViewById(R.id.gameover_menuBtn);
        menuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent i = new Intent(GameOverActivity.this, MenuActivity.class);
                startActivity(i);
            }
        });

        Button viewHighScoresButton = (Button) findViewById(R.id.gameover_viewHighScoresBtn);
        viewHighScoresButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameOverActivity.this, ScoresActivity.class);
                startActivity(intent);
            }
        });

        final AdView adView = (AdView) findViewById(R.id.gameover_advertising_banner_view);
        if (AdsHelper.shoulShowAds(this)) {
            AdsHelper.requestAd(adView);
            adView.setVisibility(View.VISIBLE);
        } else {
            adView.setVisibility(View.GONE);

        }
    }

    /**
     * Salva el score
     */
    private void saveHighScore() {
        mPlayerScore.mPlayerName = mPlayerNameEditText.getText()
                                                      .toString();
        PermData.saveLastPlayerName(this, mPlayerScore.mPlayerName);
        PermData.addNewLocalScore(this, mPlayerScore);

        mScoreIntroductionView.setVisibility(View.INVISIBLE);
        mNextActionView.setVisibility(View.VISIBLE);
    }

    private String getShareScoreMessage() {
        return getString(R.string.gameover_share, mPlayerScore.mScore);
    }

    private void onUsernameEditTextChanged(CharSequence text) {
        // NOTE: instead of disabling the text we just change the background.
        // we want to capture the click event even if the name is not valid to
        // prompt a message
        if (isUsernameValid(text)) {
            mSaveScoreButton.setBackgroundResource(R.drawable.colorful_button_enabled);
        } else {
            mSaveScoreButton.setBackgroundResource(R.drawable.colorful_button_disabled);
        }
    }

    private boolean isUsernameValid(CharSequence text) {
        text = text.toString()
                   .trim();
        return (text != null && text.length() >= MINIMUM_NAME_LENGTH);
    }

    @Override
    public void onSignInFailed() {
        Toast.makeText(this, "failed", Toast.LENGTH_SHORT)
             .show();

    }

    @Override
    public void onSignInSucceeded() {
        Toast.makeText(this, "oK!!", Toast.LENGTH_SHORT)
             .show();

        GooglePlayGamesLeaderboardHelper.submitHighestScoreIfNeeded(this, getApiClient());
    }

}
