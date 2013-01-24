package net.garrapeta.jumplings.wave;

import net.garrapeta.gameengine.GameWorld;
import net.garrapeta.gameengine.SyncGameMessage;
import net.garrapeta.jumplings.GameActivity;
import net.garrapeta.jumplings.JumplingsApplication;
import net.garrapeta.jumplings.JumplingsGameWorld;
import net.garrapeta.jumplings.Player;
import net.garrapeta.jumplings.Wave;
import net.garrapeta.jumplings.actor.EnemyActor;
import net.garrapeta.jumplings.scenario.IScenario;
import net.garrapeta.jumplings.scenario.RollingScenario;
import net.garrapeta.jumplings.scenario.ScenarioFactory;
import android.util.Log;
import android.widget.Toast;

public class CampaignSurvivalWave extends Wave implements IWaveEndListener {

    // ----------------------------------------------------- Constantes

    // Clave para referirse a esta wave
    public final static String WAVE_KEY = CampaignSurvivalWave.class.getCanonicalName();

    /** Nivel inicial de la wave hija */
    private final static int INIT_LEVEL = 1;

    /**
     * Ms que tarda en aparecer la primerta wave
     */
    public static final int FIRST_WAVE_DELAY = 0;

    /**
     * Ms que hay desde que termina la wave hasta que se realiza la siguiente
     * acci�n
     */
    public static final int INTER_WAVE_DELAY = 500;

    private static final int NEXT_SCENARIO_DELAY = 1500;

    private static final int RESUME_DELAY = NEXT_SCENARIO_DELAY + RollingScenario.FADE_IN_TIME;

    /**
     * Tiempo m�nimo entre di�logos de anuncios. Se mostrar�n al acabar la wave.
     * En ms.
     */
    private int ADS_MIN_TIME_LAPSE = 60 * 2 * 1000;

    /** Vidas que se ganan al pasar de nivel */
    public static final int NEW_LEVEL_EXTRA_LIFES = 0;

    // ----------------------------------------- Variables de instancia

    JumplingsGameWorld mWorld;
    IScenario mScenario;

    /**
     * Wave Actual
     */
    private AllowanceGameWave mCurrentWave;

    /** Timestamp de cuando se mostr� el �ltimo anuncio */
    private long lastAdTimeStamp = 0;

    // --------------------------------------------------- Constructor

    /**
     * @param jgWorld
     */
    public CampaignSurvivalWave(JumplingsGameWorld jgWorld, IWaveEndListener listener) {
        super(jgWorld, listener, INIT_LEVEL);
        mWorld = jgWorld;
        mScenario = ScenarioFactory.getScenario(jgWorld, ScenarioFactory.ScenariosIds.ROLLING);
        jgWorld.setScenario(mScenario);
    }

    // ------------------------------------------- M�todos Heredados

    @Override
    public void start() {
        super.start();
        Log.i(LOG_SRC, "Starting Wave Campaign");
        mScenario.init();
        scheduleNextWave(FIRST_WAVE_DELAY);
    }

    @Override
    public void onProcessFrame(float realTimeStep) {
        if (mCurrentWave != null) {
            mCurrentWave.processFrame(realTimeStep);
        }
    }

    @Override
    public boolean onEnemyScaped(EnemyActor e) {
        if (mCurrentWave != null) {
            return mCurrentWave.onEnemyScaped(e);
        }
        return false;
    }

    @Override
    public boolean onEnemyKilled(EnemyActor enemy) {
        mScenario.setProgress(mCurrentWave.getProgress());
        if (mCurrentWave != null) {
            return mCurrentWave.onEnemyKilled(enemy);
        }
        return false;
    }

    @Override
    public void dispose() {
        mJWorld = null;
        mScenario.dispose();
        mScenario = null;
        mCurrentWave.dispose();
        mCurrentWave = null;
    }

    @Override
    public boolean onGameOver() {
        return mCurrentWave.onGameOver();
    }

    // ---------------------------------- M�todos de IWaveEventListener

    @Override
    public void onWaveStarted() {
        Log.i(LOG_SRC, "Wave started");
    }

    @Override
    public void onWaveEnded() {
        Log.i(LOG_SRC, "Wave ended");
        mCurrentWave.pause();
        level++;
        scheduleNextWave(INTER_WAVE_DELAY);
    }

    // ------------------------------------------------ M�todos propios

    private void switchWave() {
        if (JumplingsApplication.MOBCLIX_ENABLED && mWorld.currentGameMillis() - lastAdTimeStamp > ADS_MIN_TIME_LAPSE) {
            // Se muestra anuncio
            mWorld.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWorld.mActivity.showDialog(GameActivity.DIALOG_AD_ID);
                }
            });

            lastAdTimeStamp = mWorld.currentGameMillis();
        }

        Player player = mWorld.getPlayer();
        player.addLifes(NEW_LEVEL_EXTRA_LIFES);
        mCurrentWave = new AllowanceGameWave(mWorld, this, level);
    }

    private void showLevel() {
        mJWorld.mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final String message = "Level " + level;
                Log.i(LOG_SRC, message);

                Toast toast = Toast.makeText(CampaignSurvivalWave.this.mWorld.mActivity, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }

    private void scheduleNextWave(float delay) {
        mJWorld.post(new SyncGameMessage() {
            @Override
            public void doInGameLoop(GameWorld world) {
                switchWave();
                showLevel();
                scheduleNextScenario(NEXT_SCENARIO_DELAY);
                scheduleResume(RESUME_DELAY);
            }
        }, delay);

    }

    private void scheduleResume(float delay) {
        mJWorld.post(new SyncGameMessage() {
            @Override
            public void doInGameLoop(GameWorld world) {
                mCurrentWave.play();
            }
        }, delay);
    }

    private void scheduleNextScenario(float delay) {
        mJWorld.post(new SyncGameMessage() {
            @Override
            public void doInGameLoop(GameWorld world) {
                mScenario.end();
            }
        }, delay);

    }

}
