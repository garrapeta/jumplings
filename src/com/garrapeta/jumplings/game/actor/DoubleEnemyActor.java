package com.garrapeta.jumplings.game.actor;

import android.graphics.PointF;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.garrapeta.gameengine.BitmapManager;
import com.garrapeta.gameengine.Viewport;
import com.garrapeta.jumplings.R;
import com.garrapeta.jumplings.game.JumplingsGameWorld;

public class DoubleEnemyActor extends EnemyActor {

    // ---------------------------------------------------------------
    // Constantes

    public final static float DEFAULT_RADIUS = BASE_RADIUS * 1.1f;

    public final static float HEIGHT_RESTORATION_FACTOR = 1f / 3f;

    public final static short JUMPER_CODE_DOUBLE = 1;

    // ------------------------------------------------------ Variables
    // est�ticas

    // vivo
    protected final static int BMP_ORANGE_BODY_ID = R.drawable.orange_double_body;

    protected final static int BMP_ORANGE_FOOT_RIGHT_ID = R.drawable.orange_foot_right;
    protected final static int BMP_ORANGE_FOOT_LEFT_ID = R.drawable.orange_foot_left;

    protected final static int BMP_ORANGE_HAND_RIGHT_ID = R.drawable.orange_hand_right;
    protected final static int BMP_ORANGE_HAND_LEFT_ID = R.drawable.orange_hand_left;

    // debris
    protected final static int BMP_DEBRIS_ORANGE_BODY_ID = R.drawable.orange_debris_double_body;

    protected final static int BMP_DEBRIS_ORANGE_FOOT_RIGHT_ID = R.drawable.orange_debris_foot_right;
    protected final static int BMP_DEBRIS_ORANGE_FOOT_LEFT_ID = R.drawable.orange_debris_foot_left;

    protected final static int BMP_DEBRIS_ORANGE_HAND_RIGHT_ID = R.drawable.orange_debris_hand_right;
    protected final static int BMP_DEBRIS_ORANGE_HAND_LEFT_ID = R.drawable.orange_debris_hand_left;

    // ----------------------------------------------------------------
    // Variables

    // ------------------------------------------------------ M�todos est�ticos

    static double getDoubleEnemyBaseThread() {
        return 1.5;
    }

    // ---------------------------------------------------------------
    // Constructor

    /**
     * @param gameWorld
     */
    public DoubleEnemyActor(JumplingsGameWorld mWorld) {
        super(mWorld);
        mRadius = DoubleEnemyActor.DEFAULT_RADIUS;
        mCode = DoubleEnemyActor.JUMPER_CODE_DOUBLE;
        mRadius = DoubleEnemyActor.DEFAULT_RADIUS;
    }

    @Override
    protected void initBodies(PointF worldPos) {
        // Cuerpo
        {
            // Create Shape with Properties
            PolygonShape polygonShape = new PolygonShape();
            Vector2[] vertices = new Vector2[] {
                    new Vector2(0, mRadius),
                    new Vector2(-mRadius, 0),
                    new Vector2(0, -mRadius),
                    new Vector2(mRadius, 0) };
            polygonShape.set(vertices);

            mMainBody = getWorld().createBody(this, worldPos, true);
            mMainBody.setBullet(true);

            // Assign shape to Body
            Fixture f = mMainBody.createFixture(polygonShape, 1.0f);
            f.setFilterData(CONTACT_FILTER);
            polygonShape.dispose();

        }

        mAnthtopoDelegate.createAnthropomorphicLimbs(worldPos, mRadius);
    }

    @Override
    protected void initBitmaps() {
        // vivo
        mAnthtopoDelegate.initAnthropomorphicBitmaps(BMP_ORANGE_BODY_ID, BMP_ORANGE_FOOT_RIGHT_ID, BMP_ORANGE_FOOT_LEFT_ID, BMP_ORANGE_HAND_RIGHT_ID,
                BMP_ORANGE_HAND_LEFT_ID, BMP_EYE_0_RIGHT_OPENED_ID, BMP_EYE_0_LEFT_OPENED_ID, BMP_EYE_0_RIGHT_CLOSED_ID, BMP_EYE_0_LEFT_CLOSED_ID);

        // debris
        BitmapManager mb = getWorld().getBitmapManager();
        mBmpDebrisBody = mb.getBitmap(BMP_DEBRIS_ORANGE_BODY_ID);

        mBmpDebrisFootRight = mb.getBitmap(BMP_DEBRIS_ORANGE_FOOT_RIGHT_ID);
        mBmpDebrisFootLeft = mb.getBitmap(BMP_DEBRIS_ORANGE_FOOT_LEFT_ID);

        mBmpDebrisHandRight = mb.getBitmap(BMP_DEBRIS_ORANGE_HAND_RIGHT_ID);
        mBmpDebrisHandLeft = mb.getBitmap(BMP_DEBRIS_ORANGE_HAND_LEFT_ID);

        mBmpDebrisEyeRight = mb.getBitmap(BMP_DEBRIS_EYE_0_RIGHT_ID);
        mBmpDebrisEyeLeft = mb.getBitmap(BMP_DEBRIS_EYE_0_LEFT_ID);
    }

    // -------------------------------------------------------- M�todos propios

    private final float getRestorationInitVy(float posY) {
        float maxHeight = posY + HEIGHT_RESTORATION_FACTOR * (getWorld().mViewport.getWorldBoundaries().top - posY);
        return (float) getInitialYVelocity(maxHeight);
    }

    @Override
    public void onHitted() {

        EnemyActor son = null;
        float xVel = 0;
        Vector2 pos = null;

        pos = mMainBody.getWorldCenter();
        son = getWorld().getFactory()
                        .getDoubleSonEnemyActor(Viewport.vector2ToPointF(pos));
        xVel = mMainBody.getLinearVelocity().x;

        getWorld().addActor(son);

        float yVel = getRestorationInitVy(pos.y);
        son.setLinearVelocity(xVel / 2, yVel);

        super.onHitted();
    }

    @Override
    protected void free(JumplingsFactory factory) {
        getWorld().getFactory()
                  .free(this);
    }

}
