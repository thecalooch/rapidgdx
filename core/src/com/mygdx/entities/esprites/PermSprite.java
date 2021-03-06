/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.entities.esprites;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.environments.EnvironmentManager;
import static com.mygdx.game.MainGame.RATIO;
import com.mygdx.utilities.FrameCounter;
import java.util.Random;

/**
 *
 * @author looch
 */
public class PermSprite extends EntitySprite{

    private final FrameCounter durationFC;
    private final float DURATION = 60f;
    private final Random rng = new Random();
    
    public PermSprite(String key, Vector2 pos) {
        super(pos,325f*RATIO,325f*RATIO,key, false, false, false, false, 0.8f, false, false, false, false);
        
        durationFC = new FrameCounter(DURATION);
        
        //isprite.sprite.setPosition(pos.x - isprite.sprite.getWidth()/2, pos.y - isprite.sprite.getHeight()/2);
        isprite.sprite.rotate(360 * rng.nextFloat());
        
        //this.pos = new Vector2(pos.x - isprite.sprite.getWidth()/2, pos.y - isprite.sprite.getHeight()/2);
    }
    
    @Override
    public void update(){
        super.update();
        
        if(durationFC.complete)
            dispose();
    }
    
    public void start(){
        durationFC.start(EnvironmentManager.currentEnv.getFrameManager());
        EnvironmentManager.currentEnv.spawnEntity(this);
        
    }
    
    
    
}
