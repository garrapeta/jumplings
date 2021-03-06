package com.garrapeta.jumplings.game.actor;

import android.graphics.PointF;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.garrapeta.gameengine.BitmapManager;
import com.garrapeta.jumplings.R;
import com.garrapeta.jumplings.game.JumplingsGameWorld;

public class RoundEnemyActor extends EnemyActor {

    // ---------------------------------------------------- Constantes

    // FIXME: remove this
    public static int sCount = 0;

    public final static float DEFAULT_RADIUS = BASE_RADIUS * 1f;

    public final static short JUMPER_CODE_SIMPLE = 0;

    // ------------------------------------------------------ Variables
    // est�ticas

    // vivo
    protected final static int BMP_RED_BODY_ID = R.drawable.red_body;

    protected final static int BMP_RED_FOOT_RIGHT_ID = R.drawable.red_foot_right;
    protected final static int BMP_RED_FOOT_LEFT_ID = R.drawable.red_foot_left;

    protected final static int BMP_RED_HAND_RIGHT_ID = R.drawable.red_hand_right;
    protected final static int BMP_RED_HAND_LEFT_ID = R.drawable.red_hand_left;

    // debris
    protected final static int BMP_DEBRIS_RED_BODY_ID = R.drawable.red_debris_body;

    protected final static int BMP_DEBRIS_RED_FOOT_RIGHT_ID = R.drawable.red_debris_foot_right;
    protected final static int BMP_DEBRIS_RED_FOOT_LEFT_ID = R.drawable.red_debris_foot_left;

    protected final static int BMP_DEBRIS_RED_HAND_RIGHT_ID = R.drawable.red_debris_hand_right;
    protected final static int BMP_DEBRIS_RED_HAND_LEFT_ID = R.drawable.red_debris_hand_left;

    // ---------------------------------------------------- Variables

    // ----------------------------------------------------------------
    // Constructor

    public RoundEnemyActor(JumplingsGameWorld world) {
        super(world);
        mRadius = RoundEnemyActor.DEFAULT_RADIUS;
        mCode = RoundEnemyActor.JUMPER_CODE_SIMPLE;
        sCount++;
    }

    // ------------------------------------------------------ M�todos heredados

    // -------------------------------------------------------- M�todos Propios

    @Override
    protected void initBodies(PointF worldPos) {

        // Cuerpo
        {
            // Create Shape with Properties
            CircleShape circleShape = new CircleShape();
            circleShape.setRadius(mRadius);
            mMainBody = getWorld().createBody(this, worldPos, true);
            mMainBody.setBullet(true);

            // Assign shape to Body
            Fixture f = mMainBody.createFixture(circleShape, 1.0f);
            f.setFilterData(CONTACT_FILTER);
            circleShape.dispose();

        }

        mAnthtopoDelegate.createAnthropomorphicLimbs(worldPos, mRadius);
    }

    @Override
    protected void initBitmaps() {
        // vivo
        mAnthtopoDelegate.initAnthropomorphicBitmaps(BMP_RED_BODY_ID, BMP_RED_FOOT_RIGHT_ID, BMP_RED_FOOT_LEFT_ID, BMP_RED_HAND_RIGHT_ID, BMP_RED_HAND_LEFT_ID,
                BMP_EYE_0_RIGHT_OPENED_ID, BMP_EYE_0_LEFT_OPENED_ID, BMP_EYE_0_RIGHT_CLOSED_ID, BMP_EYE_0_LEFT_CLOSED_ID);

        // debris
        BitmapManager mb = getWorld().getBitmapManager();
        mBmpDebrisBody = mb.getBitmap(BMP_DEBRIS_RED_BODY_ID);

        mBmpDebrisFootRight = mb.getBitmap(BMP_DEBRIS_RED_FOOT_RIGHT_ID);
        mBmpDebrisFootLeft = mb.getBitmap(BMP_DEBRIS_RED_FOOT_LEFT_ID);

        mBmpDebrisHandRight = mb.getBitmap(BMP_DEBRIS_RED_HAND_RIGHT_ID);
        mBmpDebrisHandLeft = mb.getBitmap(BMP_DEBRIS_RED_HAND_LEFT_ID);

        mBmpDebrisEyeRight = mb.getBitmap(BMP_DEBRIS_EYE_0_RIGHT_ID);
        mBmpDebrisEyeLeft = mb.getBitmap(BMP_DEBRIS_EYE_0_LEFT_ID);
    }

    @Override
    protected void free(JumplingsFactory factory) {
        getWorld().getFactory()
                  .free(this);
    }

}
