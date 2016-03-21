/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.combat.skills;

import com.badlogic.gdx.math.Vector2;
import static com.mygdx.combat.skills.Skill.SkillType.LIGHT;
import com.mygdx.entities.Entity;
import com.mygdx.entities.ImageSprite;
import com.mygdx.entities.esprites.PermSprite;
import static com.mygdx.game.MainGame.RATIO;
import com.mygdx.screen.GameScreen;
import static com.mygdx.utilities.UtilityVars.PPM;

/**
 *
 * @author looch
 */
public abstract class LightSkill extends Skill{
 
    public LightSkill(){
        type = LIGHT;
        COST = 20.0f;
        
        prepTime = 0;
        attTime = 0.75f;
        recovTime = 0.1f;
        
        FORCE = 250.0f;
        
        skillSprite = new ImageSprite("poe-attack-light",false);
        skillSprite.sprite.setScale(0.35f*RATIO);
        
    }
    
    @Override
    public void effect(boolean isCombo, Skill prevSkill){
        //screen shake
        screenShake();
        
        
        //sound
        boolean playSound = false;
        
        //movementForce();
        
        //effected enemies
        for(Entity ent: GameScreen.player.getAttTargets()){
            //damage enemy
            damageEnemy(ent,isCombo, GameScreen.player.getPreviousSkill());
            
            
            //knockback force
            knockbackEnemy(ent);
                  
            
            
            //impact sprites
            addImpactSprite(ent);
            
                    
            
            //sound
            playSound = true;
        }
        
        //sound
        if(playSound) impactSound.play(false);
        
        
        //buff
        addBuff();
        
        //attack force
        attackForce();
        
        reset();
        
        //permanance sprite
        addPermSprite();
            
    }
    
    public void attackForce(){
        if (GameScreen.player.isUserMoving()) {
            Vector2 dir = GameScreen.player.getCurrentDirection().cpy();
            dir.scl(FORCE * 0.8f);
            GameScreen.player.getBody().applyForce(dir, GameScreen.player.getBody().getPosition(), true);
        }
    }

    public void screenShake() {
        if(!GameScreen.player.getAttTargets().isEmpty())
            GameScreen.camera.shake(3, 0.35f);
    }

    public void damageEnemy(Entity e, boolean combo, Skill prevSkill) {
        if (combo) {
            
           comboEffect(prevSkill);
            
            e.damage(
                    GameScreen.player.getCurrentDamage() * GameScreen.player.getLightMod() * damageMod * comboBonus,
                    true);
        } else {
            e.damage(GameScreen.player.getCurrentDamage() * GameScreen.player.getLightMod() * damageMod);
        }
    }
    
    public void comboEffect(Skill prevSkill){}

    public void knockbackEnemy(Entity e) {
        Vector2 dv = e.getBody().getPosition().sub(GameScreen.player.getBody().getPosition()).cpy().nor();
        e.getBody().applyForce(dv.scl(FORCE), e.getBody().getPosition(), true); 
    }

    public void addImpactSprite(Entity e) {
        ImageSprite isprite = new ImageSprite(impactTemplates.get(rng.nextInt(impactTemplates.size)));

        if (GameScreen.player.getBody().getPosition().x < e.getBody().getPosition().x) {
            isprite.setXFlip(true);
        }

        GameScreen.player.addImpactSprite(e.getBody().getPosition().x * PPM - isprite.sprite.getWidth() / 2,
                e.getBody().getPosition().y * PPM - isprite.sprite.getHeight() / 2,
                isprite);
    }

    public void addBuff() {}

    public void addPermSprite() {
        PermSprite p = new PermSprite("perm1",
                    new Vector2(
                            GameScreen.player.getBody().getPosition().x *PPM, 
                            GameScreen.player.getBody().getPosition().y*PPM));
            p.start();
    }
    
    /*
    public void movementForce(){
        PlayerEntity p = GameScreen.player;
        
        Vector2 cd = p.getCurrentDirection();
        System.out.println("@LightSKill cd " + cd.x + " y: " + cd.y);
        p.getBody().applyForce(cd.cpy().scl(100f), p.getBody().getPosition(), true);
    }*/
    
    
}