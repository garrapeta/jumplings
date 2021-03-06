package com.garrapeta.jumplings.game.wave;

import android.graphics.PointF;
import android.graphics.RectF;

import com.badlogic.gdx.math.Vector2;
import com.garrapeta.gameengine.GameWorld;
import com.garrapeta.gameengine.SyncGameMessage;
import com.garrapeta.gameengine.utils.LogX;
import com.garrapeta.gameengine.utils.PhysicsUtils;
import com.garrapeta.jumplings.game.JumplingsGameWorld;
import com.garrapeta.jumplings.game.Player;
import com.garrapeta.jumplings.game.actor.BombActor;
import com.garrapeta.jumplings.game.actor.DoubleEnemyActor;
import com.garrapeta.jumplings.game.actor.EnemyActor;
import com.garrapeta.jumplings.game.actor.MainActor;
import com.garrapeta.jumplings.game.actor.PowerUpActor;
import com.garrapeta.jumplings.game.actor.RoundEnemyActor;
import com.garrapeta.jumplings.game.actor.SplitterEnemyActor;

public class GameWave extends AllowanceWave<JumplingsGameWorld> {

    // --------------------------------------------------- Constantes

    private static float POWERUP_BASE_LAPSE = 45000;

    public static short JUMPER_CODE_NULL = Short.MIN_VALUE;

    // ---------------------------------------- Variables de instancia

    // owner of the wave
    private final ICampaignWave mParent;

    private short mNextJumperCode;

    private float mBombProbability;
    private float mSpecialEnemyProbability;
    private float mDoubleEnemyProbability;
    private float mTripleSplitterEnemyProbability;

    private int mMaxBombs;

    /** N�mero de enemigos que hay que matar */
    private int mTotalKills;
    /** N�mero de enemigos muertos */
    private int mKills;

    // -------------------------------------------------------- Constructor

    public GameWave(JumplingsGameWorld world, ICampaignWave parent, int level, boolean schedulePowerUpAtStart) {
        super(world, level);
        mParent = parent;

        mNextJumperCode = RoundEnemyActor.JUMPER_CODE_SIMPLE;

        // Inicializaci�n de probabilidades y riesgos
        setMaxThreat(-0.25 + (level * 1.25));

        mBombProbability = Math.min(0.30f, level * 0.025f);

        mSpecialEnemyProbability = Math.min(0.65f, level * 0.07f);

        mDoubleEnemyProbability = 0.4f;

        mTripleSplitterEnemyProbability = Math.min(0.5f, 0.0f + (level * 0.05f));

        mMaxBombs = (int) (level * 0.334);

        mTotalKills = 0 + level * 7;

        if (schedulePowerUpAtStart) {
            scheduleGeneratePowerUp(getPowerUpCreationLapse());
        }
    }

    // ------------------------------------------------- M�todos heredados

    @Override
    protected float getCurrentThreat() {
        return getWorld().getThread();
    }

    @Override
    protected double generateThreat(double threatNeeded) {
        PointF initPos = new PointF();
        Vector2 initVel = new Vector2();

        createInitPosAndVel(initPos, initVel);

        if (mNextJumperCode == JUMPER_CODE_NULL) {
            mNextJumperCode = generateJumperCode();
        }
        double threat = tryToCreateJumper(threatNeeded, initPos, initVel);

        return threat;
    }

    @Override
    public void onProcessFrame(float stepTime) {
        // se comprueba que la wave no ha terminado por tiempo
        if (isCompleted()) {
            // no se comunica el fin de la wave hasta que todos los enemigos
            // est�n muertos
            if (getWorld().mJumplingActors.size() == 0 && mParent != null) {
                mParent.onChildWaveEnded();
            }
        } else {
            super.onProcessFrame(stepTime);
        }
    }

    @Override
    public boolean onEnemyKilled(EnemyActor enemy) {
        mKills++;
        return false;
    }

    /**
     * @return progreso, del 0 - 100
     */
    public float getProgress() {
        return mKills * 100 / mTotalKills;
    }

    public boolean isCompleted() {
        return getProgress() >= 100;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    // -------------------------------------------------- M�todos propios

    @Override
    public void start() {
        super.start();
        if (mParent != null) {
            mParent.onChildWaveStarted();
        }
    }

    private short generateJumperCode() {
        short code;

        do {
            while (true) {
                if (Math.random() < mBombProbability && getWorld().mBombActors.size() < mMaxBombs) {
                    code = BombActor.JUMPER_CODE_BOMB;
                    break;
                }

                if (Math.random() > mSpecialEnemyProbability) {
                    code = RoundEnemyActor.JUMPER_CODE_SIMPLE;
                    break;
                }

                if (Math.random() < mDoubleEnemyProbability) {
                    code = DoubleEnemyActor.JUMPER_CODE_DOUBLE;
                    break;
                }

                if (Math.random() < mTripleSplitterEnemyProbability) {
                    code = SplitterEnemyActor.JUMPER_CODE_SPLITTER_TRIPLE;
                    break;
                }

                code = SplitterEnemyActor.JUMPER_CODE_SPLITTER_DOUBLE;
                break;
            }
        } while (MainActor.getBaseThread(code) > getMaxThreat());

        LogX.i(TAG, "Next enemy code: " + code);

        return code;
    }

    private double tryToCreateJumper(double threatNeeded, PointF initPos, Vector2 initVel) {
        double threat = MainActor.getBaseThread(mNextJumperCode);
        MainActor mainActor = null;

        if (threat <= threatNeeded) {

            switch (mNextJumperCode) {
            case RoundEnemyActor.JUMPER_CODE_SIMPLE:
                // simple
                mainActor = getWorld().getFactory()
                                      .getRoundEnemyActor(initPos);
                break;
            case DoubleEnemyActor.JUMPER_CODE_DOUBLE:
                // double
                mainActor = getWorld().getFactory()
                                      .getDoubleEnemyActor(initPos);
                break;
            case SplitterEnemyActor.JUMPER_CODE_SPLITTER_DOUBLE:
                // splitter small
                mainActor = getWorld().getFactory()
                                      .getSplitterEnemyActor(initPos, 1);
                break;
            case SplitterEnemyActor.JUMPER_CODE_SPLITTER_TRIPLE:
                // splitter big
                mainActor = getWorld().getFactory()
                                      .getSplitterEnemyActor(initPos, 2);
                break;
            case BombActor.JUMPER_CODE_BOMB:
                // bomb
                mainActor = getWorld().getFactory()
                                      .getBombActor(initPos);
                break;
            }
        }

        if (mainActor != null) {
            mainActor.setLinearVelocity(initVel.x, initVel.y);
            getWorld().addActor(mainActor);
            LogX.i(TAG, "Added mainActor: " + mNextJumperCode);
            mNextJumperCode = JUMPER_CODE_NULL;
            return threat;

        } else {
            return 0;
        }
    }

    private void createInitPosAndVel(PointF pos, Vector2 vel) {
        int rand = (int) (Math.random() * 4);

        switch (rand) {
        case 0:
            // up
            createInitPosAndVelTop(pos, vel);
            break;
        case 1:
            // down
            createInitPosAndVelBottom(pos, vel);
            break;
        case 2:
            // left
            createInitPosAndVelLeft(pos, vel);
            break;
        case 3:
            // right
            createInitPosAndVelRight(pos, vel);
            break;
        }

    }

    private void createInitPosAndVelBottom(PointF pos, Vector2 vel) {
        // posX
        pos.x = getRandomPosX();

        // posY
        pos.y = getBottomPos();

        // vel
        createInitVel(pos, vel);
    }

    private void createInitPosAndVelTop(PointF pos, Vector2 vel) {
        // posX
        pos.x = getRandomPosX();

        // posY
        pos.y = getTopPos();

        // vel
        vel.x = 0;
        vel.y = 0;
    }

    private void createInitPosAndVelLeft(PointF pos, Vector2 vel) {
        // posX
        pos.x = getLeftPos();

        // posY
        pos.y = getRandomPosY();

        // vel
        createInitVel(pos, vel);
    }

    private void createInitPosAndVelRight(PointF pos, Vector2 vel) {
        // posX
        pos.x = getRightPos();

        // posY
        pos.y = getRandomPosY();

        // vel
        createInitVel(pos, vel);
    }

    private void createInitVel(PointF pos, Vector2 vel) {
        // vel
        float g = getWorld().getGravityY();

        // Factor de aletoriedad (0 - 1)
        float XFACTOR = 0.9f;
        float YFACTOR = 0.75f;

        // Distancia m�xima que pueda viajar verticalmente
        RectF bounds = getWorld().mViewport.getWorldBoundaries();
        float boundsHeight = bounds.top - bounds.bottom;

        // Distancia vertical que va a alcanzar. Se le hace un poco aleatoria.
        float maxYDistance = (float) (YFACTOR + ((1 - YFACTOR) * Math.random())) * boundsHeight;

        float targetY = bounds.bottom + maxYDistance;

        // Distancia vertical que a subir
        float yDistance = targetY - pos.y;

        // velocidad vertical inicial
        float vy = (float) PhysicsUtils.getInitialVelocity(yDistance, 0, g);

        // Tiempo que va a estar subiendo
        float tup = (float) PhysicsUtils.getTime(vy, 0, g);
        float tdown = (float) PhysicsUtils.getTime(maxYDistance, -g);

        // Tiempo que va a estar viajando (arriba + abajo)
        float t = tup + tdown;

        float worldWidth = bounds.right - bounds.left;

        // Distancia m�xima que pueda viajar horizontalmente
        float maxXDistance;
        // Dependiendo de la posici�n se le tira a la izquierda o derecha
        if (pos.x > bounds.left + (worldWidth / 2)) {
            maxXDistance = bounds.left - pos.x;
        } else {
            maxXDistance = bounds.right - pos.x;
        }

        // Distancia que va a viajer horizontalmente. Se le hace un poco
        // aleatoria.
        float xDistance = (float) (XFACTOR + ((1 - XFACTOR) * Math.random())) * maxXDistance;

        float vx = xDistance / t;

        vel.x = vx;
        vel.y = vy;

    }

    private float getPowerUpCreationLapse() {
        int wounds = Math.max(0, Player.DEFAUL_INIT_LIFES - getWorld().getPlayer()
                                                                      .getLifes());

        float lapse = POWERUP_BASE_LAPSE - (wounds * (POWERUP_BASE_LAPSE / Player.DEFAUL_INIT_LIFES));
        LogX.i(TAG, "Next power up in: " + lapse + " ms (" + getWorld().getPlayer()
                                                                       .getLifes() + " lives)");
        return lapse;
    }

    private void scheduleGeneratePowerUp(float delay) {
        getWorld().post(new PowerUpGenerationSynMessage(this), delay);
    }

    private void generatePowerUp() {
        PointF initPos = new PointF();
        Vector2 initVel = new Vector2();

        createInitPosAndVelTop(initPos, initVel);

        PowerUpActor powerUp;

        // la probabilidad de que salga una vida crece conforme se ha perdido
        // vida
        float lifeUpBaseProbability = 0.4f;
        float woundedFactor = 1 - (getWorld().getPlayer()
                                             .getLifes() / getWorld().getPlayer()
                                                                     .getMaxLifes());
        float lifeUpProbability = lifeUpBaseProbability * woundedFactor;

        if (Math.random() < lifeUpProbability) {
            powerUp = getWorld().getFactory()
                                .getLifePowerUpActor(initPos);
        } else {
            powerUp = getWorld().getFactory()
                                .getSwordPowerUpActor(initPos);
        }
        powerUp.setLinearVelocity(initVel.x, initVel.y);
        getWorld().addActor(powerUp);
    }

    /**
     * Message to generate power ups
     * 
     * @author garrapeta
     */
    private static class PowerUpGenerationSynMessage extends SyncGameMessage {

        private final GameWave mGameWave;

        private PowerUpGenerationSynMessage(GameWave gameWave) {
            mGameWave = gameWave;
        }

        @Override
        public final void doInGameLoop(GameWorld world) {
            if (!mGameWave.getWorld()
                          .isGameOver()) {
                final float nextPowerUp;
                if (!mGameWave.mParent.isInBetweenWaves()) {
                    mGameWave.generatePowerUp();
                    nextPowerUp = mGameWave.getPowerUpCreationLapse();
                } else {
                    nextPowerUp = mGameWave.getPowerUpCreationLapse() / 2;
                }
                mGameWave.scheduleGeneratePowerUp(nextPowerUp);
            }
        }
    }

}
