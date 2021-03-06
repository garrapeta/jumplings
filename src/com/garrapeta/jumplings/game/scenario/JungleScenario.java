package com.garrapeta.jumplings.game.scenario;

import android.graphics.Bitmap;

import com.garrapeta.gameengine.BitmapManager;
import com.garrapeta.jumplings.R;
import com.garrapeta.jumplings.game.JumplingsWorld;

/**
 * 
 * Nature scenario
 * 
 * @author GaRRaPeTa
 */
public class JungleScenario extends LayerScenario {

    private final static int LAYER1_ID = R.drawable.scenario_jungle_1;
    private final static int LAYER2_ID = R.drawable.scenario_jungle_2;
    private final static int LAYER3_ID = R.drawable.scenario_jungle_3;
    private final static int LAYER4_ID = R.drawable.scenario_jungle_4;

    // ----------------------------------------------- Constructor

    /**
     * @param dWorld
     */
    JungleScenario(JumplingsWorld world) {
        super(world);
    }

    @Override
    public void initLayers(BitmapManager bm) {
        int viewWidth = mWorld.mGameView.getWidth();
        int viewHeight = mWorld.mGameView.getHeight();

        // Initialisation of Layers
        {
            // TODO: avoid blocking game thread with this load
            Bitmap bmp = bm.loadBitmap(mWorld.mActivity.getResources(), LAYER1_ID);
            int maxHeight = (int) (viewHeight * 1.5);
            addLayer(new Layer(this, bmp, maxHeight, 0, 0, 2, 0, true, true, viewWidth, viewHeight));
        }
        {
            int maxHeight = (int) (viewHeight * 1.4);
            Bitmap bmp = bm.loadBitmap(mWorld.mActivity.getResources(), LAYER2_ID);
            float initYPos = maxHeight - bmp.getHeight();
            addLayer(new Layer(this, bmp, maxHeight, 0, initYPos, 0, 0, true, false, viewWidth, viewHeight));
        }
        {
            Bitmap bmp = bm.loadBitmap(mWorld.mActivity.getResources(), LAYER3_ID);
            int maxHeight = (int) (viewHeight * 2.7);
            addLayer(new Layer(this, bmp, maxHeight, 0, 0, 0, 0, false, false, viewWidth, viewHeight));
        }
        {
            int maxHeight = viewHeight * 2;
            Bitmap bmp = bm.loadBitmap(mWorld.mActivity.getResources(), LAYER4_ID);
            float initYPos = maxHeight - bmp.getHeight();
            addLayer(new Layer(this, bmp, maxHeight, 0, initYPos, 0, 0, true, false, viewWidth, viewHeight));
        }
    }

    @Override
    public void dispose() {
        // TODO: delegate this into the layer
        BitmapManager bm = mWorld.getBitmapManager();
        bm.releaseBitmap(LAYER1_ID);
        bm.releaseBitmap(LAYER2_ID);
        bm.releaseBitmap(LAYER3_ID);
        bm.releaseBitmap(LAYER4_ID);
    }

}
