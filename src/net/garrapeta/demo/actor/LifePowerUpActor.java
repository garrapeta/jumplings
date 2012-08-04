package net.garrapeta.demo.actor;

import net.garrapeta.demo.JumplingsApplication;
import net.garrapeta.demo.JumplingsGameWorld;
import net.garrapeta.demo.R;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

public class LifePowerUpActor extends PowerUpActor {

	// ----------------------------------------------------------- Constantes
	
	public final static short JUMPER_CODE_POWER_UP_LIFE = 7;
	
	public  final static float  DEFAULT_RADIUS = BASE_RADIUS * 1.05f;
	
	// ------------------------------------------------- Variables estáticas
	
	// vivo
	protected final static Bitmap BMP_HEART;
	
	// debris
	protected final static Bitmap BMP_DEBRIS_HEART;	

	
	
	// --------------------------------------------------- Constructor
	
	public LifePowerUpActor(JumplingsGameWorld jgWorld, PointF worldPos) {
		super(jgWorld, worldPos);
		
		this.code = LifePowerUpActor.JUMPER_CODE_POWER_UP_LIFE;
		
		// vivo
		bmpIcon 	  = BMP_HEART;
		
		// debris
		bmpDebrisIcon = BMP_DEBRIS_HEART;
		
	}
	
	// ------------------------------------------------- Métodos estáticos
	
	static double getLifePowerUpHitCount() {
		// Se le pone un valor positivo, para incentivar que el jugador la coja
		return 1.5f;
	}

	// ----------------------------------------------- Inicialización estática
	
	static {

		Resources r = JumplingsApplication.getInstance().getResources();

		// vivo
		BMP_HEART		= BitmapFactory.decodeResource(r, R.drawable.powerup_heart);
		
		// muerto
		BMP_DEBRIS_HEART	= BitmapFactory.decodeResource(r, R.drawable.powerup_debris_heart);
	}
	
	// --------------------------------------------- Métodos heredados

	@Override
	public void doLogic(float gameTimeStep) {
	}
	
	@Override
	public void onHitted() {
		jgWorld.onLifePowerUp(this);
		super.onHitted();
	}

}
