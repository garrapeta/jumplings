package net.garrapeta.jumplings;

import java.util.ArrayList;

import net.garrapeta.gameengine.Actor;
import net.garrapeta.gameengine.GameView;
import net.garrapeta.gameengine.SoundManager;
import net.garrapeta.gameengine.VibratorManager;
import net.garrapeta.jumplings.actor.BladePowerUpActor;
import net.garrapeta.jumplings.actor.BombActor;
import net.garrapeta.jumplings.actor.EnemyActor;
import net.garrapeta.jumplings.actor.FlashActor;
import net.garrapeta.jumplings.actor.JumplingActor;
import net.garrapeta.jumplings.actor.LifePowerUpActor;
import net.garrapeta.jumplings.actor.MainActor;
import net.garrapeta.jumplings.scenario.Scenario;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Mundo del juego
 * 
 * @author GaRRaPeTa
 */
public class JumplingsGameWorld extends JumplingsWorld {

	// -------------------------------------------------------- Constantes
	
    
	public static final float LIFE_LOSS_FACTOR 				= 5;
	
	public static final float INVULNERABLE_TIME				= 1500;
	
	public static final int WEAPON_GUN						= 0;
	public static final int WEAPON_SHOTGUN					= 1;
	public static final int WEAPON_BLADE					= 2;
	
	// ------------------------------------------------------------ Variables
	
	public JumplingsGameActivity jgActivity;
	
	/** Jugador */
	private Player player;
	
	/** Si el mundo ha sido creado */
	boolean isCreated;

	public ArrayList<JumplingActor> jumplingActors = new ArrayList<JumplingActor>();

	public ArrayList<MainActor> mainActors = new ArrayList<MainActor>();

	public ArrayList<EnemyActor> enemies = new ArrayList<EnemyActor>();
	
	// N�mero de bombar actuales
	public int bombCount = 0;
	
	
	/** Duranci�n en ms del shake actual */
	private float shakeDuration  = 0;
	/** Tiempo que le queda al shake actual */
	private float shakeRemaining = 0;
	/** Intensidad, en unidades del mundo, del shake actual */
	private float shakeIntensity = 0;
	
	private ArrayList<double[]> motionEvents = new ArrayList<double[]>();
	
	/** Arma actual */
	public Weapon weapon;
	
	/** Escenario actual */
	Scenario scenario;

	/** Escenario que est� desapareciendo */
	Scenario fadingScenario;
	
	
	//----------------------------------------------------------- Constructor
	
	public JumplingsGameWorld(JumplingsGameActivity jgActivity, GameView gameView) {
		super(jgActivity, gameView);
		this.jgActivity = jgActivity;
		player = new Player(this);
	}
	
	// ----------------------------------------------------- M�todos de World
	
	@Override
	public void create() {
		super.create();
		
		// Se pone el HighScore
		HighScore hs = PermData.getInstance().getLocalGetHighScore();
		if (hs != null) {
			long localHighScore =  hs.score;
			if (localHighScore > 0) {
				TextView highScoreTextView = jgActivity.localHighScoreTextView;
				highScoreTextView.setText(" Highscore: " + localHighScore);
			}
		}
		
		// Inicializaci�n del arma
		setWeapon(Gun.WEAPON_CODE_GUN);
		
		nextScenario();
		
	}
	
	
	@Override
	public synchronized void processFrame(float gameTimeStep) {
		if (weapon.getWeaponCode() != Gun.WEAPON_CODE_GUN) {
			if (weapon.getRemainingTime() <= 0) {
				setWeapon(Gun.WEAPON_CODE_GUN);
			} else {
				jgActivity.updateSpecialWeaponBar();
			}
		}
		
		dispatchMotionEvents();
		
		super.processFrame(gameTimeStep);
		
		if (shakeRemaining > 0) { 
			shakeRemaining -= gameTimeStep;
		}
		
		// scenario
		scenario.processFrame(gameTimeStep);
		if (fadingScenario != null) {
			fadingScenario.processFrame(gameTimeStep);
			if (fadingScenario.fadingOutRemainigTime <= 0) {
				fadingScenario = null;
			}
		}
		
		// FIXME: Chapuza para evitar problema del cuelgue al inicial el juego
		if (!view.isSyncDrawing() && currentGameMillis() > 100) {
			view.setSyncDrawing(true);
		}
	}
	

	
	@Override
	protected void drawWorld(Canvas canvas) {
		drawWorldBackground(canvas);
		
		if (this.shakeRemaining <= 0) {
			drawActors(canvas);
			
		} else {
			float intensity = (shakeRemaining / shakeDuration) * shakeIntensity;
				
			float pixels = (int) viewport.worldUnitsToPixels(intensity);
			
			float pixelsX = pixels;
			if (Math.random() > 0.5) {
				pixelsX *=-1; 
			}
			
			float pixelsY = pixels;
			if (Math.random() > 0.5) {
				pixelsY *=-1; 
			}

			
			canvas.save();
			canvas.translate(pixelsX, pixelsY);
			drawActors(canvas);
			
			canvas.restore();
			
			
		}
	}
	
	private void drawWorldBackground(Canvas canvas) {
		super.drawBackground(canvas);
		
		// TODO: evitar esta comporbaci�n de nulidad
		// TODO: pasar las medidas de la pantalla al escenario en reset()
		if (scenario != null) {
			scenario.draw(canvas);	
			if (fadingScenario != null) {
				fadingScenario.draw(canvas);	
			}
		}
	}



	// -------------------------------------------------------- M�todos propios
	
	public void nextScenario() {
		Log.i(LOG_SRC, " Next Scenario");
		if (scenario != null) {
			fadingScenario = scenario;
			fadingScenario.fadingOut = true;
		}
		scenario = new Scenario(this);
		scenario.reset();
	}
	
	

	
	// M�todos de gesti�n de actores
	
	@Override
	public synchronized void addActor(Actor a) {
		super.addActor(a);
		if (a instanceof JumplingActor) {
			addJumplingActor((JumplingActor) a);
		}
	}
	
	@Override
	public synchronized void removeActor(Actor a) {
		super.removeActor(a);
		if (a instanceof JumplingActor) {
			removeJumplingActor((JumplingActor) a);
		}
	}
	
	private synchronized void addJumplingActor(JumplingActor pa) {
		jumplingActors.add(pa);
		if (pa instanceof MainActor) {
			addMainActor((MainActor) pa);
		}
	}
	
	private synchronized void removeJumplingActor(JumplingActor pa) {
		jumplingActors.remove(pa);
		if (pa instanceof MainActor) {
			removeMainActor((MainActor) pa);
		}
	}
	
	private synchronized void addMainActor(MainActor mainActor) {
		mainActors.add(mainActor);
		if (mainActor instanceof EnemyActor) {
			addEnemy((EnemyActor) mainActor);
		}
	}
	
	private synchronized void removeMainActor(MainActor mainActor) {
		mainActors.remove(mainActor);
		if (mainActor instanceof EnemyActor) {
			removeEnemy((EnemyActor) mainActor);
		}
	}
	
	private synchronized void addEnemy(EnemyActor enemy) {
		enemies.add(enemy);		
	}
	
	private synchronized void removeEnemy(EnemyActor enemy) {
		enemies.remove(enemy);
	}
	
	
	public synchronized int getHitsCount() {
		int hits = 0;
		int s = mainActors.size();
		for (int i = 0; i < s; i++) {
			hits += MainActor.getHitCount(mainActors.get(i).getCode());
		}
		return hits;
	}
	
	public void onEnemyScaped(EnemyActor e) {
		if (!jgActivity.isGameOver()) {
		
			if ( player.isVulnerable() && !wave.onEnemyScaped(e)) {
				onPostEnemyScaped(e);
			}
		}
	}
	
	public void onPostEnemyScaped(EnemyActor e) {
		if (jgActivity.flashCfgLevel >= PermData.CFG_LEVEL_SOME) { 
			FlashActor flash = new FlashActor(this, 
					FlashActor.FLASH_ENEMY_SCAPED_COLOR, 
					FlashActor.FLASH_ENEMY_SCAPED_ALPHA, 
					FlashActor.FLASH_ENEMY_SCAPED_DURATION);
			addActor(flash);
		}
		
		onFail();
	}
	
	public synchronized void onEnemyKilled(EnemyActor enemy) {
		if (!wave.onEnemyKilled(enemy)) {
			if (jgActivity.soundOn) {
				SoundManager.getInstance().play(JumplingsGameActivity.SAMPLE_ENEMY_KILLED);
			}
			if (jgActivity.vibrateCfgLevel == PermData.CFG_LEVEL_ALL) {
				VibratorManager.getInstance().play(JumplingsGameActivity.VIBRATION_ENEMY_KILLED);
			}
			
			player.onEnemyKilled(enemy);
			scenario.setProgress(wave.getProgress());
			
			if (jgActivity.shakeCfgLevel == PermData.CFG_LEVEL_ALL) {
				createShake(100f, 0.20f);
			}

		}
		
	}
	
	public void onBombExploded(BombActor bomb) {
		if (!jgActivity.isGameOver()) {
			
			if ( player.isVulnerable() && !wave.onBombExploded(bomb)) {
				onPostBombExploded(bomb);
			}
		}		
	}
	
	private void onPostBombExploded(BombActor bomb) {
		if (jgActivity.flashCfgLevel >= PermData.CFG_LEVEL_SOME) { 
			FlashActor flash = new FlashActor(this, 					
					FlashActor.FLASH_BOMB_COLOR, 
					FlashActor.FLASH_BOMB_ALPHA, 
					FlashActor.FLASH_BOMB_DURATION);
			addActor(flash);
			
			FlashActor flash2 = new FlashActor(this, 					
					FlashActor.FLASH_BOMB2_COLOR, 
					FlashActor.FLASH_BOMB2_ALPHA, 
					FlashActor.FLASH_BOMB2_DURATION);
			addActor(flash2);
			
		}
		
		onFail();
	}
	
	public void onLifePowerUp(LifePowerUpActor lifePowerUpActor) {
		if (!jgActivity.isGameOver()) {
			
			if ( player.isVulnerable() && !wave.onLifePowerUp(lifePowerUpActor)) {
				onPostLifePowerUp(lifePowerUpActor);
			}
		}	
	}

	private void onPostLifePowerUp(LifePowerUpActor lifePowerUpActor) {
		if (jgActivity.soundOn) {
			SoundManager.getInstance().play(JumplingsGameActivity.SAMPLE_LIFE_UP);
		}
		if (jgActivity.flashCfgLevel >= PermData.CFG_LEVEL_SOME) {
			FlashActor flash = new FlashActor(this, 					
					FlashActor.FLASH_LIFEUP_COLOR, 
					FlashActor.FLASH_LIFEUP_ALPHA, 
					FlashActor.FLASH_LIFEUP_DURATION);
			addActor(flash);
		}
		
		player.addLifes(1);		
	}
	

	public void onBladePowerUp(BladePowerUpActor bladePowerUpActor) {
		if (!jgActivity.isGameOver()) {
			
			if ( player.isVulnerable() && !wave.onBladePowerUp(bladePowerUpActor)) {
				onPostBladePowerUp(bladePowerUpActor);
			}
		}	
	}

	private void onPostBladePowerUp(BladePowerUpActor bladePowerUpActor) {
		setWeapon(Blade.WEAPON_CODE_BLADE);
		
		if (jgActivity.shakeCfgLevel >= PermData.CFG_LEVEL_SOME) {
			FlashActor flash = new FlashActor(this, 					
					FlashActor.FLASH_BLADE_DRAWN_COLOR, 
					FlashActor.FLASH_BLADE_DRAWN_ALPHA, 
					FlashActor.FLASH_BLADE_DRAWN_DURATION);
			addActor(flash);
		}
	}

	@Override
	public void onTouchEvent(MotionEvent event) {
		// TODO: No se llama al super, pues en este juego no queremos que el engine detecte los actores pulsados
//		super.onTouchEvent(event);
		
		synchronized (motionEvents) {
			motionEvents.add(new double[] {event.getAction(), event.getX(), event.getY(), System.currentTimeMillis()});
		}
		
	}
	
	public void dispatchMotionEvents() {
		synchronized (motionEvents) {
			
			if (!jgActivity.isGameOver() && !jgActivity.isGamePaused()) {
				for (int i = 0; i < motionEvents.size(); i++) {
					double[] info = motionEvents.get(i);
					dispatchMotionEvent(info);
				}
			}
			motionEvents.clear();
		}
		
	}
	
	// M�todos de ciclo de vida del juego
	public void dispatchMotionEvent(double[] info) {


		weapon.onTouchEvent(info);
		
		/*
		if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
			boolean success = false;
			
			synchronized (this) { 
			
				int size = enemies.size();
				Object[] es = enemies.toArray();
				
				
				for (int i = 0; i < size; i++) {
					ShapeActor e = (ShapeActor) es[i];
					success |= e.onTouchEvent(event);
					if (success) {
						e.explode();
						this.onEnemyKilled(e);
						break;
					}
				}
					
				if (!success) {
					if (!cActivity.isGamePlaying()) {
						return;
					}
					//onMissed();
				}
			}
		}
		*/
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setWeapon(short weaponId) {
		boolean active = false;
		switch(weaponId) {
		case Gun.WEAPON_CODE_GUN:
			if (jgActivity.soundOn) {
				SoundManager.getInstance().play(JumplingsGameActivity.SAMPLE_GUN_CLIP);
			}
			weapon = new Gun(this);
			active = false;
			break;
//		case Shotgun.WEAPON_CODE_SHOTGUN:
//			weapon = new Shotgun(this);
//			active = true;
//			break;
		case Blade.WEAPON_CODE_BLADE:
			if (jgActivity.soundOn) {
				SoundManager.getInstance().play(JumplingsGameActivity.SAMPLE_SWORD_DRAW);
			}
			weapon = new Blade(this);
			active = true;
			break;
		}
		
		jgActivity.activateSpecialWeaponBar(active);
		
	
		// DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG
		if (JumplingsApplication.DEBUG_ENABLED) {	
			jgActivity.updateWeaponsRadioGroup(weaponId);
		}
		// DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG
	}
	
	
	
	/**
	 *  M�todo ejecutado cuando el jugador falla
	 */
	private void onFail() {
		if (jgActivity.soundOn) {
			SoundManager.getInstance().play(JumplingsGameActivity.SAMPLE_FAIL);
		}
		if (jgActivity.vibrateCfgLevel >=PermData.CFG_LEVEL_SOME) { 
			VibratorManager.getInstance().play(JumplingsGameActivity.VIBRATION_FAIL);
		}
		
		Player player = getPlayer();
		player.subLifes(1);
		player.makeInvulnerable(INVULNERABLE_TIME);
		
		if (jgActivity.shakeCfgLevel >= PermData.CFG_LEVEL_SOME) {
			createShake(425f, 0.75f);
		}
		
		if (player.getLifes() <= 0) {
			if (!wave.onGameOver()) {
				jgActivity.onGameOver();
			}
		}	
	}
	

	/**
	 * Programa un temblor de pantalla
	 * @param time
	 * @param intensity
	 */
	private void createShake(float time, float intensity) {
		this.shakeDuration         	= time;
		this.shakeRemaining 	= time;
		this.shakeIntensity = intensity;
	}

}