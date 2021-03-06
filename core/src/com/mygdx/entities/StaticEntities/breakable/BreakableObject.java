/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.entities.StaticEntities.breakable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.entities.StaticEntities.StaticEntity;
import com.mygdx.entities.ImageSprite;
import com.mygdx.entities.pickups.Pickup;
import com.mygdx.environments.EnvironmentManager;
import static com.mygdx.utilities.UtilityVars.BIT_ATT;
import static com.mygdx.utilities.UtilityVars.BIT_EN;
import static com.mygdx.utilities.UtilityVars.BIT_PLAYER;
import static com.mygdx.utilities.UtilityVars.BIT_WALL;
import static com.mygdx.utilities.UtilityVars.PPM;

/**
 *
 * @author looch
 */
public class BreakableObject extends StaticEntity{

    protected ImageSprite idleSprite;
    
    protected final Array<Pickup> itemRewardPool = new Array<Pickup>();
  
    
    public BreakableObject(Vector2 pos, float w, float h) {
        super(pos, w, h);
        
        userdata = "en_" + id;
        bd.position.set(pos.x/PPM, pos.y/PPM);
        cshape.setRadius(width/PPM);
        fd.restitution = 0.8f;
        fd.filter.categoryBits = BIT_EN;
        fd.filter.maskBits = BIT_PLAYER | BIT_WALL | BIT_ATT | BIT_EN;
        fd.shape = cshape;
        
        
    }
    
    
    @Override
    public void dispose(){
        spawnReward();
        super.dispose();
    }
    
    public void spawnReward(){
        //spawn reward items
        for(Pickup item: itemRewardPool){
            Vector2 iv = new Vector2(
                    body.getPosition().x*PPM + 25*rng.nextFloat()*rngNegSet.random(),
                    body.getPosition().y*PPM + 25*rng.nextFloat()*rngNegSet.random());
            item.setPosition(iv);
            Pickup p = (Pickup)EnvironmentManager.currentEnv.spawnEntity(item);
            p.spawnForce();
        }
    }
    
    public void addReward(Pickup p){
        itemRewardPool.add(p);
    }
    
    
}
