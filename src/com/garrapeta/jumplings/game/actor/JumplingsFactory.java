package com.garrapeta.jumplings.game.actor;

import android.graphics.Bitmap;
import android.graphics.PointF;

import com.badlogic.gdx.physics.box2d.Body;
import com.garrapeta.gameengine.utils.LogX;
import com.garrapeta.gameengine.utils.Pool;
import com.garrapeta.jumplings.JumplingsApplication;
import com.garrapeta.jumplings.game.JumplingsGameWorld;
import com.garrapeta.jumplings.game.JumplingsWorld;

/**
 * Class to control the creation of the Jumplings, so pools can be used and
 * allocation reduced
 */
public class JumplingsFactory {

    public final static String TAG = JumplingsApplication.TAG_JUMPLINGS + ".pool";
    private final static int MAX_INSTANCES = 300;

    private final JumplingsWorld mJumplingsWorld;

    private final Pool<DebrisActor> mDebrisActorPool;
    private final Pool<SparksActor> mSparksActor;
    private final Pool<IntroActor> mIntroActor;

    // TODO: make the pools final
    private Pool<RoundEnemyActor> mRoundEnemyActorPool;
    private Pool<DoubleEnemyActor> mDoubleEnemyActorPool;
    private Pool<DoubleSonEnemyActor> mDoubleSonEnemyActorPool;
    private Pool<SplitterEnemyActor> mSplitterEnemyActorPool;
    private Pool<BombActor> mBombActorPool;
    private Pool<SwordPowerUpActor> mSwordPowerUpActorPool;
    private Pool<LifePowerUpActor> mLifePowerUpActorPool;

    public JumplingsFactory(JumplingsWorld jumplingsWorld) {
        mJumplingsWorld = jumplingsWorld;

        mDebrisActorPool = new Pool<DebrisActor>(128, MAX_INSTANCES) {
            @Override
            protected DebrisActor newObject() {
                return new DebrisActor(mJumplingsWorld);
            }
        };

        mSparksActor = new Pool<SparksActor>(128, MAX_INSTANCES) {
            @Override
            protected SparksActor newObject() {
                return new SparksActor(mJumplingsWorld);
            }
        };

        mIntroActor = new Pool<IntroActor>(4, MAX_INSTANCES) {
            @Override
            protected IntroActor newObject() {
                return new IntroActor(mJumplingsWorld);
            }
        };

        // FIXME: fix this dynamic cast
        if (mJumplingsWorld instanceof JumplingsGameWorld) {
            final JumplingsGameWorld jumplingsGameWorld = (JumplingsGameWorld) mJumplingsWorld;
            mRoundEnemyActorPool = new Pool<RoundEnemyActor>(8, MAX_INSTANCES) {
                @Override
                protected RoundEnemyActor newObject() {
                    return new RoundEnemyActor(jumplingsGameWorld);
                }
            };
            mDoubleEnemyActorPool = new Pool<DoubleEnemyActor>(8, MAX_INSTANCES) {
                @Override
                protected DoubleEnemyActor newObject() {
                    return new DoubleEnemyActor(jumplingsGameWorld);
                }
            };
            mDoubleSonEnemyActorPool = new Pool<DoubleSonEnemyActor>(8, MAX_INSTANCES) {
                @Override
                protected DoubleSonEnemyActor newObject() {
                    return new DoubleSonEnemyActor(jumplingsGameWorld);
                }
            };
            mSplitterEnemyActorPool = new Pool<SplitterEnemyActor>(16, MAX_INSTANCES) {
                @Override
                protected SplitterEnemyActor newObject() {
                    return new SplitterEnemyActor(jumplingsGameWorld);
                }
            };
            mBombActorPool = new Pool<BombActor>(4, MAX_INSTANCES) {
                @Override
                protected BombActor newObject() {
                    return new BombActor(jumplingsGameWorld);
                }
            };
            mSwordPowerUpActorPool = new Pool<SwordPowerUpActor>(2, MAX_INSTANCES) {
                @Override
                protected SwordPowerUpActor newObject() {
                    return new SwordPowerUpActor(jumplingsGameWorld);
                }
            };
            mLifePowerUpActorPool = new Pool<LifePowerUpActor>(2, MAX_INSTANCES) {
                @Override
                protected LifePowerUpActor newObject() {
                    return new LifePowerUpActor(jumplingsGameWorld);
                }
            };
        }
    }

    // Get

    public DebrisActor getDebrisActor(Body body, Bitmap bitmap) {
        DebrisActor actor = mDebrisActorPool.obtain();
        LogX.i(TAG, "DebrisActor: " + mDebrisActorPool.getDebugString());
        actor.init(body, bitmap);
        return actor;
    }

    public IntroActor getIntroActor(PointF worldPos) {
        IntroActor actor = mIntroActor.obtain();
        LogX.i(TAG, "IntroActor: " + mIntroActor.getDebugString());
        actor.init(worldPos);
        return actor;
    }

    public SparksActor getSparksActor(PointF worldPos, int longevity) {
        SparksActor actor = mSparksActor.obtain();
        LogX.i(TAG, "SparksActor: " + mSparksActor.getDebugString());
        actor.init(worldPos, longevity);
        return actor;
    }

    public RoundEnemyActor getRoundEnemyActor(PointF worldPos) {
        RoundEnemyActor actor = mRoundEnemyActorPool.obtain();
        LogX.i(TAG, "RoundEnemyActor: " + mRoundEnemyActorPool.getDebugString());
        actor.init(worldPos);
        return actor;
    }

    public DoubleEnemyActor getDoubleEnemyActor(PointF worldPos) {
        DoubleEnemyActor actor = mDoubleEnemyActorPool.obtain();
        LogX.i(TAG, "DoubleEnemyActor: " + mDoubleEnemyActorPool.getDebugString());
        actor.init(worldPos);
        return actor;
    }

    public DoubleSonEnemyActor getDoubleSonEnemyActor(PointF worldPos) {
        DoubleSonEnemyActor actor = mDoubleSonEnemyActorPool.obtain();
        LogX.i(TAG, "DoubleSonEnemyActor: " + mDoubleSonEnemyActorPool.getDebugString());
        actor.init(worldPos);
        return actor;
    }

    public SplitterEnemyActor getSplitterEnemyActor(PointF worldPos, int level) {
        SplitterEnemyActor actor = mSplitterEnemyActorPool.obtain();
        LogX.i(TAG, "SplitterEnemyActor: " + mSplitterEnemyActorPool.getDebugString());
        actor.init(worldPos, level);
        return actor;
    }

    public BombActor getBombActor(PointF worldPos) {
        BombActor actor = mBombActorPool.obtain();
        LogX.i(TAG, "BombActor: " + mBombActorPool.getDebugString());
        actor.init(worldPos);
        return actor;
    }

    public SwordPowerUpActor getSwordPowerUpActor(PointF worldPos) {
        SwordPowerUpActor actor = mSwordPowerUpActorPool.obtain();
        LogX.i(TAG, "SwordPowerUpActor: " + mSwordPowerUpActorPool.getDebugString());
        actor.init(worldPos);
        return actor;
    }

    public LifePowerUpActor getLifePowerUpActor(PointF worldPos) {
        LifePowerUpActor actor = mLifePowerUpActorPool.obtain();
        LogX.i(TAG, "LifePowerUpActor: " + mLifePowerUpActorPool.getDebugString());
        actor.init(worldPos);
        return actor;
    }

    // Free

    public void free(DebrisActor actor) {
        mDebrisActorPool.free(actor);
    }

    public void free(SparksActor actor) {
        mSparksActor.free(actor);
    }

    public void free(IntroActor actor) {
        mIntroActor.free(actor);
    }

    public void free(RoundEnemyActor actor) {
        mRoundEnemyActorPool.free(actor);
    }

    public void free(DoubleEnemyActor actor) {
        mDoubleEnemyActorPool.free(actor);
    }

    public void free(DoubleSonEnemyActor actor) {
        mDoubleSonEnemyActorPool.free(actor);
    }

    public void free(SplitterEnemyActor actor) {
        mSplitterEnemyActorPool.free(actor);
    }

    public void free(BombActor actor) {
        mBombActorPool.free(actor);
    }

    public void free(SwordPowerUpActor actor) {
        mSwordPowerUpActorPool.free(actor);
    }

    public void free(LifePowerUpActor actor) {
        mLifePowerUpActorPool.free(actor);
    }

    /**
     * Frees all the resources
     */
    public void clear() {
        mDebrisActorPool.clear();
        mSparksActor.clear();
        mIntroActor.clear();
        // FIXME: avoid this instanceof
        if (mJumplingsWorld instanceof JumplingsGameWorld) {
            mRoundEnemyActorPool.clear();
            mDoubleEnemyActorPool.clear();
            mDoubleSonEnemyActorPool.clear();
            mSplitterEnemyActorPool.clear();
            mBombActorPool.clear();
            mSwordPowerUpActorPool.clear();
            mLifePowerUpActorPool.clear();
        }
    }
}
