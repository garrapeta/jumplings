package com.garrapeta.jumplings.game.actor;

/**
 * Intarface of those object that can be bumped (hit against walls or other
 * objects)
 * 
 * @author garrapeta
 * 
 */
public interface IBumpable {

    public void onBumpChange(boolean bumped);
}
