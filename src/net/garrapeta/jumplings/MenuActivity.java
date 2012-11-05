package net.garrapeta.jumplings;

import net.garrapeta.gameengine.GameView;
import net.garrapeta.jumplings.wave.CampaignSurvivalWave;
import net.garrapeta.jumplings.wave.MenuWave;
import net.garrapeta.jumplings.wave.TestWave;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.openfeint.api.OpenFeint;
import com.openfeint.api.OpenFeintDelegate;
import com.openfeint.api.resource.CurrentUser;
import com.openfeint.api.resource.User;
import com.openfeint.api.ui.Dashboard;

public class MenuActivity extends Activity {

    // -----------------------------------------------------------------
    // Constantes

    // ----------------------------------------------------- Variables est�ticas

    // ----------------------------------------------------- Variables de
    // instancia

    ImageButton feintLeaderBoardBtn;

    /** World */
    JumplingsWorld mWorld;

    // ------------------------------------------- Variables de configuraci�n

    // ---------------------------------------------------- M�todos est�ticos

    // -------------------------------------------------- M�todos de Activity

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Preparaci�n de la UI
        setContentView(R.layout.menu);

        // OPEN FEINT
        if (JumplingsApplication.FEINT_ENABLED) {
            OpenFeint.initialize(this, JumplingsApplication.feintSettings, new OpenFeintDelegate() {
                @Override
                public void userLoggedIn(CurrentUser user) {
                    super.userLoggedIn(user);
                    enableFeintLeaderboardButton();
                }

                @Override
                public void userLoggedOut(User user) {
                    super.userLoggedOut(user);
                    disableFeintLeaderboardButton();
                }

                @Override
                public void onDashboardAppear() {
                    super.onDashboardAppear();
                }

                @Override
                public void onDashboardDisappear() {
                    super.onDashboardDisappear();
                }

                @Override
                public boolean showCustomApprovalFlow(Context ctx) {
                    return super.showCustomApprovalFlow(ctx);
                }
            });
        }

        Button startBtn = (Button) findViewById(R.id.menu_playBtn);
        startBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });

        if (JumplingsApplication.DEBUG_ENABLED) {
            Button testBtn = (Button) findViewById(R.id.menu_testBtn);
            testBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTest();
                }
            });
            testBtn.setVisibility(View.VISIBLE);

            Button exitBtn = (Button) findViewById(R.id.menu_exitBtn);
            exitBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            exitBtn.setVisibility(View.VISIBLE);
        }

        ImageButton highScoresBtn = (ImageButton) findViewById(R.id.menu_highScoresBtn);
        highScoresBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighScores();
            }
        });

        if (JumplingsApplication.FEINT_ENABLED) {
            feintLeaderBoardBtn = (ImageButton) findViewById(R.id.menu_feintLeaderBoardBtn);
            feintLeaderBoardBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFeintLeaderboard();
                }
            });
        }

        ImageButton preferencesBtn = (ImageButton) findViewById(R.id.menu_preferencesBtn);
        preferencesBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreferences();
            }
        });

        ImageButton aboutBtn = (ImageButton) findViewById(R.id.menu_aboutBtn);
        aboutBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbout();
            }
        });
        

        // Ads
        if (JumplingsApplication.MOBCLIX_ENABLED) {
            findViewById(R.id.menu_advertising_banner_view).setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(JumplingsApplication.LOG_SRC, "onStart " + this);

        // Si ahora est� logeado se activa el bot�n de Feint
        if (JumplingsApplication.FEINT_ENABLED && OpenFeint.isUserLoggedIn()) {
            enableFeintLeaderboardButton();
        }

        mWorld = new JumplingsWorld(this, (GameView) findViewById(R.id.menu_gamesurface));
        mWorld.setDrawDebugInfo(JumplingsApplication.DEBUG_ENABLED);

        // Preparaci�n de la wave

        mWorld.mWave = new MenuWave(mWorld, null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(JumplingsApplication.LOG_SRC, "onRestart " + this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(JumplingsApplication.LOG_SRC, "onPause " + this);
        mWorld.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(JumplingsApplication.LOG_SRC, "onResume " + this);
        mWorld.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(JumplingsApplication.LOG_SRC, "onStop " + this);
        mWorld.finish();
        mWorld = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(JumplingsApplication.LOG_SRC, "onDestroy " + this);
    }

    // ---------------------------------------------------- M�todos propios
    
    // ---------------------------- M�todos de componentes de interacci�n

    private void startNewGame() {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.WAVE_BUNDLE_KEY, CampaignSurvivalWave.WAVE_KEY);
        startActivity(i);
    }

    private void startTest() {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.WAVE_BUNDLE_KEY, TestWave.WAVE_KEY);
        startActivity(i);
        // Intent i = new Intent(this, GameOverActivity.class);
        // startActivity(i);
    }

    private void showHighScores() {
        Intent i = new Intent(this, HighScoreListingActivity.class);
        startActivity(i);
    }

    private void showPreferences() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
    }

    private void showAbout() {
        Toast.makeText(this, "TODO: about/info Activity", Toast.LENGTH_LONG).show();
    }

    private void enableFeintLeaderboardButton() {
        feintLeaderBoardBtn.setVisibility(View.VISIBLE);
    }

    private void disableFeintLeaderboardButton() {
        feintLeaderBoardBtn.setVisibility(View.GONE);
    }

    private void showFeintLeaderboard() {
        Dashboard.openLeaderboard(GameOverActivity.feintLeaderboardId);
    }

}