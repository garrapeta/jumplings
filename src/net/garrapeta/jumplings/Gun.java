package net.garrapeta.jumplings;

import net.garrapeta.jumplings.actor.BulletActor;
import net.garrapeta.jumplings.actor.FlashActor;
import android.graphics.PointF;
import android.view.MotionEvent;

public class Gun extends Weapon {

	// -------------------------------------------------------------------- Constantes
	
	public final static short WEAPON_CODE_GUN = 0;
	
	private static int WEAPON_TIME_GUN = Integer.MAX_VALUE;
	

	
	// -------------------------------------------------------- Variables de instancia

	
	protected float bulletRadius;
	protected float bulletLongevity;
	
	protected long lastShootTimeStamp;
	
	protected int shootTimeGap;
	
	public Gun(JumplingsGameWorld jgWorld) {
		super(jgWorld);
		
		shootTimeGap = 100;
		bulletRadius = 0.5f;
		bulletLongevity = 150;
	}
	
	public void doLogic(float gameTimeStep) {
	
	}

	@Override
	public void onTouchEvent(double[] info) {
		if (info[0] == MotionEvent.ACTION_DOWN && 
				(System.currentTimeMillis() - lastShootTimeStamp) >= shootTimeGap) {
			
			if (mJGWorld.mFlashCfgLevel == PermData.CFG_LEVEL_ALL) {
				FlashActor flash = new FlashActor(mJGWorld,
						                          FlashActor.FLASH_SHOT_COLOR, 
						                          FlashActor.FLASH_SHOT_ALPHA, 
						                          FlashActor.FLASH_SHOT_DURATION);
				
				mJGWorld.addActor(flash);
			}

			mJGWorld.getSoundManager().play(JumplingsGameWorld.SAMPLE_SLAP);
			
			lastShootTimeStamp = System.currentTimeMillis();
			
			PointF worldPos = mJGWorld.viewport.screenToWorld((float)info[1], (float)info[2]);
			BulletActor bullet = new BulletActor(mJGWorld, worldPos, bulletRadius);
			bullet.longevity = bulletLongevity;
			bullet.lifeTime = bulletLongevity;
			
			mJGWorld.addActor(bullet);
		}
	}
	
	public short getWeaponCode() {
		return WEAPON_CODE_GUN;
	}
	
	public int getMaxTime() {
		return WEAPON_TIME_GUN;
	}

    @Override
    public void onEnded() {
    }

}
