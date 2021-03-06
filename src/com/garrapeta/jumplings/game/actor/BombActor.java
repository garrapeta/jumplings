package com.garrapeta.jumplings.game.actor;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.garrapeta.gameengine.BitmapManager;
import com.garrapeta.gameengine.Viewport;
import com.garrapeta.jumplings.R;
import com.garrapeta.jumplings.game.JumplingsGameWorld;

public class BombActor extends MainActor {

    // ----------------------------------------------------------- Constantes

    public final static short JUMPER_CODE_BOMB = 6;

    public final static float DEFAULT_RADIUS = BASE_RADIUS * 1.25f;

    private final static int SPARKS_LAPSE = 300;

    private final static int SPARKS_PER_LAPSE = 2;

    /** Tiempo que permanecen las chispas en pantalla, en ms. Estela de la mecha */
    private final static int SPARKLE_LONGEVITY_FUSE = 1500;

    /** Tiempo que permanecen las chispas, en ms. Al explotar las bombas */
    private final static int SPARKLE_LONGEVITY_EXPLOSION = 700;

    /** N�mero de chispas que salen al explotar la bomba */
    private final static int SPARKS_AT_EXPLOSION = 25;

    /** Fuerza de las chispas al explotar la bomba */
    private final static int EXPLOSION_SPARKLE_FORCE = 500;

    /** Fuerza de las onda expansiva */
    private float BLAST_FORCE = 80;

    /** Radio de las onda expansiva */
    private float BLAST_RADIUS = 6;

    // ------------------------------------------------- Variables de instancia

    // Vivo
    protected Bitmap mBmpBody;
    protected Bitmap mBmpBodyFuse;

    // Debris
    protected Bitmap mBmpDebrisBody;
    protected Bitmap mBmpDebrisFuse;

    public Body mFuseBody;

    private long mLastSparkle;

    // ---------------------------------------------------- M�todos est�ticos

    static double getBombBaseThread() {
        return 1.5f;
    }

    // --------------------------------------------------- Constructor

    public BombActor(JumplingsGameWorld world) {
        super(world, Z_INDEX);
        mRadius = BombActor.DEFAULT_RADIUS;
        mCode = BombActor.JUMPER_CODE_BOMB;
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

        // Mecha
        {
            float w = mRadius / 3;
            float h = mRadius / 2;
            // Create Shape with Properties
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.setAsBox(w, h);
            PointF pos = new PointF(worldPos.x, worldPos.y + mRadius + h);
            mFuseBody = getWorld().createBody(this, pos, true);
            mFuseBody.setBullet(false);

            // Assign shape to Body
            Fixture f = mFuseBody.createFixture(polygonShape, 1.0f);
            f.setRestitution(AUX_BODIES_RESTITUTION);
            f.setFilterData(NO_CONTACT_FILTER);
            polygonShape.dispose();

            // Uni�n
            WeldJointDef jointDef = new WeldJointDef();

            jointDef.initialize(mMainBody, mFuseBody, Viewport.pointFToVector2(pos));

            getWorld().createJoint(this, jointDef);
        }
    }

    @Override
    protected void initBitmaps() {
        BitmapManager mb = getWorld().getBitmapManager();
        // vivo
        mBmpBody = mb.getBitmap(R.drawable.bomb_body);
        mBmpBodyFuse = mb.getBitmap(R.drawable.bomb_fuse);

        // debris
        mBmpDebrisBody = mb.getBitmap(R.drawable.bomb_debris_body);
        mBmpDebrisFuse = mb.getBitmap(R.drawable.bomb_debris_fuse);
    }

    @Override
    protected ArrayList<JumplingActor<?>> getDebrisBodies() {
        ArrayList<JumplingActor<?>> debrisActors = new ArrayList<JumplingActor<?>>();

        // Main Body
        {
            Body body = mMainBody;
            DebrisActor debrisActor = getWorld().getFactory()
                                                .getDebrisActor(body, mBmpDebrisBody);
            getWorld().addActor(debrisActor);
            debrisActors.add(debrisActor);
        }

        // Fuse
        {
            Body body = mFuseBody;
            DebrisActor debrisActor = getWorld().getFactory()
                                                .getDebrisActor(body, mBmpDebrisFuse);
            getWorld().addActor(debrisActor);
            debrisActors.add(debrisActor);
        }

        return debrisActors;
    }

    @Override
    public void processFrame(float gameTimeStep) {
        long now = System.currentTimeMillis();
        if (now - mLastSparkle >= SPARKS_LAPSE) {
            int sparkles = (int) (Math.random() * SPARKS_PER_LAPSE);

            for (int i = 0; i < sparkles; i++) {
                PointF aux = Viewport.vector2ToPointF(mFuseBody.getWorldCenter());
                PointF pos = new PointF(aux.x, aux.y);
                SparksActor sparkle = getWorld().getFactory()
                                                .getSparksActor(pos, SPARKLE_LONGEVITY_FUSE);
                getWorld().addActor(sparkle);
                mLastSparkle = now;
            }
        }
    }

    @Override
    protected void drawBitmaps(Canvas canvas) {
        getWorld().drawBitmap(canvas, this.mMainBody, mBmpBody);
        getWorld().drawBitmap(canvas, this.mFuseBody, mBmpBodyFuse);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        getWorld().onBombActorAdded(this);
        getWorld().getSoundManager()
                  .play(JumplingsGameWorld.SAMPLE_BOMB_LAUNCH);
        getWorld().getSoundManager()
                  .play(JumplingsGameWorld.SAMPLE_FUSE, true);
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        getWorld().onBombActorRemoved(this);
        if (getWorld().mBombActors.size() == 0) {
            getWorld().getSoundManager()
                      .stop(JumplingsGameWorld.SAMPLE_FUSE);
        }
    }

    // ------------------------------------------------ M�todos propios

    @Override
    public void onHitted() {
        getWorld().onBombExploded(this);

        // sonido
        getWorld().getSoundManager()
                  .play(JumplingsGameWorld.SAMPLE_BOMB_BOOM);

        // Se genera una onda expansiva sobre los enemigos
        Object[] as = getWorld().mJumplingActors.toArray();

        int l = as.length;

        for (int i = 0; i < l; i++) {
            JumplingActor<?> a = (JumplingActor<?>) as[i];
            getWorld().applyBlast(mMainBody.getWorldCenter(), a.mMainBody, BLAST_RADIUS, BLAST_FORCE);
        }

        // Se crean chispas de la explosi�n
        int rounds = 3;
        for (int round = 0; round < rounds; round++) {
            Vector2 aux = mMainBody.getWorldCenter();
            ArrayList<JumplingActor<?>> sparkles = new ArrayList<JumplingActor<?>>();
            float force = EXPLOSION_SPARKLE_FORCE / (round + 1);
            for (int j = 0; j < SPARKS_AT_EXPLOSION / rounds; j++) {
                SparksActor sparkle = getWorld().getFactory()
                                                .getSparksActor(new PointF(aux.x, aux.y), SPARKLE_LONGEVITY_EXPLOSION);
                getWorld().addActor(sparkle);
                sparkles.add(sparkle);
            }

            // se aceleran para que salgan disparadas
            applyBlast(sparkles, force);
        }

        super.onHitted();
    }

    @Override
    protected void free(JumplingsFactory factory) {
        getWorld().getFactory()
                  .free(this);
    }

    @Override
    public void onBumpChange(boolean bumped) {
        // nothing
    }

    @Override
    protected void dispose() {
        super.dispose();
        mBmpBody = null;
        mBmpBodyFuse = null;
        mBmpDebrisBody = null;
        mBmpDebrisFuse = null;
        mFuseBody = null;
    }

}
