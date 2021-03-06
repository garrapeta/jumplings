package com.garrapeta.jumplings.game.actor;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.garrapeta.gameengine.BitmapManager;
import com.garrapeta.gameengine.Viewport;
import com.garrapeta.jumplings.R;
import com.garrapeta.jumplings.game.JumplingsGameWorld;

public abstract class PowerUpActor extends MainActor {

    // ----------------------------------------------------------- Constantes
    public final static float DEFAULT_RADIUS = BASE_RADIUS * 1.05f;

    // ------------------------------------------------- Variables est�ticas

    // vivo
    protected final static int BMP_POWERUP_BG = R.drawable.powerup_bg;

    // debris
    protected final static int BMP_DEBRIS_POWERUP_BG = R.drawable.powerup_debris_bg;

    // ---------------------------------------------- Variables de instancia

    protected Body mIconBody;

    // Bitmaps del actor vivo
    protected Bitmap mBmpBg;
    protected Bitmap mBmpIcon;

    // Bitmaps del actor muerto (debris)
    protected Bitmap mBmpDebrisBg;
    protected Bitmap mBmpDebrisIcon;

    // --------------------------------------------------- Constructor

    public PowerUpActor(JumplingsGameWorld world) {
        super(world, Z_INDEX);
        mRadius = PowerUpActor.DEFAULT_RADIUS;
        // vivo
        BitmapManager mb = world.getBitmapManager();
        mBmpBg = mb.getBitmap(BMP_POWERUP_BG);
        // debris
        mBmpDebrisBg = mb.getBitmap(BMP_DEBRIS_POWERUP_BG);
    }

    // --------------------------------------------- M�todos heredados

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

        // Icon
        {
            // Create Shape with Properties
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.setAsBox(mRadius, mRadius);
            PointF pos = new PointF(worldPos.x, worldPos.y);
            mIconBody = getWorld().createBody(this, pos, true);
            mIconBody.setBullet(false);

            // Assign shape to Body
            Fixture f = mIconBody.createFixture(polygonShape, 1.0f);
            f.setRestitution(AUX_BODIES_RESTITUTION);
            f.setFilterData(NO_CONTACT_FILTER);
            polygonShape.dispose();

            // Uni�n
            WeldJointDef jointDef = new WeldJointDef();

            jointDef.initialize(mMainBody, mIconBody, Viewport.pointFToVector2(pos));

            getWorld().createJoint(this, jointDef);
        }
    }

    @Override
    protected ArrayList<JumplingActor<?>> getDebrisBodies() {
        ArrayList<JumplingActor<?>> debrisActors = new ArrayList<JumplingActor<?>>();

        // Main Body
        {
            Body body = mMainBody;
            DebrisActor debrisActor = getWorld().getFactory()
                                                .getDebrisActor(body, mBmpDebrisBg);

            getWorld().addActor(debrisActor);
            debrisActors.add(debrisActor);
        }

        // Icon
        {
            Body body = mIconBody;
            DebrisActor debrisActor = getWorld().getFactory()
                                                .getDebrisActor(body, mBmpDebrisIcon);

            getWorld().addActor(debrisActor);
            debrisActors.add(debrisActor);
        }

        return debrisActors;
    }

    @Override
    protected void drawBitmaps(Canvas canvas) {
        getWorld().drawBitmap(canvas, this.mMainBody, mBmpBg);
        getWorld().drawBitmap(canvas, this.mIconBody, mBmpIcon);
    }

    @Override
    public void onBumpChange(boolean bumped) {
        // nothing
    }

    @Override
    protected void dispose() {
        super.dispose();
        mIconBody = null;
        mBmpBg = null;
        mBmpIcon = null;
        mBmpDebrisBg = null;
        mBmpDebrisIcon = null;
    }
}
