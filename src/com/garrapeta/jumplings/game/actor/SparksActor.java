package com.garrapeta.jumplings.game.actor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.garrapeta.gameengine.BitmapManager;
import com.garrapeta.jumplings.R;
import com.garrapeta.jumplings.game.JumplingsWorld;

public class SparksActor extends JumplingActor<JumplingsWorld> {

    // ----------------------------------------------------------- Constantes
    public final static float DEFAULT_RADIUS = BASE_RADIUS * 1.2f;

    /**
     * Z-Index del actor
     */
    public final static int Z_INDEX = 0;

    public final static int SPARKS_FILTER_BIT = 0x00020;

    private final static Filter SPARKS_FILTER;

    // ------------------------------------------------- Variables est�ticas

    // Vivo
    protected static final int[] bmpsSparkles = {
            R.drawable.sparks_big_0,
            R.drawable.sparks_big_1,
            R.drawable.sparks_big_2,
            R.drawable.sparks_big_3 };

    // ----------------------------------------------- Variables de instancia

    float mLongevity;

    float mLifeTime;

    protected int mAlpha;

    protected Bitmap mBmpSparkle;

    protected Paint mPaint;

    // ----------------------------------------------- Inicializaci�n est�tica

    static {
        SPARKS_FILTER = new Filter();

        SPARKS_FILTER.categoryBits = SPARKS_FILTER_BIT;

        SPARKS_FILTER.maskBits = WallActor.WALL_FILTER_BIT;
    }

    // ---------------------------------------------------- M�todos est�ticos

    // --------------------------------------------------- Constructor

    public SparksActor(JumplingsWorld world) {
        super(world, Z_INDEX);
        mRadius = SparksActor.DEFAULT_RADIUS;
        mPaint = new Paint();
    }

    public void init(PointF worldPos, int longevity) {
        mLongevity = mLifeTime = longevity;
        init(worldPos);
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
            f.setFilterData(SPARKS_FILTER);
            circleShape.dispose();
        }

    }

    @Override
    protected void initBitmaps() {
        BitmapManager mb = getWorld().getBitmapManager();
        mBmpSparkle = mb.getBitmap(bmpsSparkles[(int) (Math.random() * bmpsSparkles.length)]);
    }

    @Override
    public void processFrame(float gameTimeStep) {
        mLifeTime = Math.max(0, mLifeTime - gameTimeStep);
        if (mLifeTime <= 0) {
            getWorld().removeActor(this);
        }
        mAlpha = (int) (255 * mLifeTime / mLongevity);
    }

    @Override
    protected void drawBitmaps(Canvas canvas) {
        mPaint.setAlpha(mAlpha);
        getWorld().drawBitmap(canvas, this.mMainBody, mBmpSparkle, mPaint);
    }

    @Override
    protected void free(JumplingsFactory factory) {
        getWorld().getFactory()
                  .free(this);
    }

    @Override
    protected void dispose() {
        super.dispose();
        mBmpSparkle = null;
        mPaint = null;
    }

}
