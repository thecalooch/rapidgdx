/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.entities.pickups.statups;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.environments.EnvironmentManager;
import com.mygdx.game.MainGame;
import com.mygdx.managers.ResourceManager;
import com.mygdx.screen.GameScreen;
import static com.mygdx.utilities.UtilityVars.PPM;

/**
 *
 * @author saynt
 */
public class Pickup_Statup_Life extends Pickup_Statup{
 
    
    public Pickup_Statup_Life(Vector2 pos){
        super(pos);
        
        name = "Life Orb";
    }
    
    public Pickup_Statup_Life(){
        this(new Vector2(0,0));
    }
    
    @Override
    public void death(){
        super.death();
        GameScreen.player.addStatPoints(1,0,0,0,0);
        GameScreen.player.refreshStats();
        GameScreen.player.restoreHp();
        EnvironmentManager.currentEnv.addDamageText("Life Up", 
                new Vector2(
                            body.getPosition().x * PPM - width * rng.nextFloat() / 2,
                            body.getPosition().y * PPM + height * 1.1f) );
        System.out.println("@Pickup_Statup_Life increase life");
        
        //dispose();
    }
}
