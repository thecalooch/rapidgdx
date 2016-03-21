/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.environments.tears;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.entities.ImageSprite;
import com.mygdx.entities.StaticEntities.StaticEntity;
import com.mygdx.entities.pickups.Pickup;
import com.mygdx.entities.text.TextDamage;
import com.mygdx.environments.Environment;
import com.mygdx.environments.EnvironmentManager;
import static com.mygdx.game.MainGame.RATIO;
import com.mygdx.managers.ResourceManager;
import com.mygdx.screen.GameScreen;
import com.mygdx.utilities.FrameCounter;
import com.mygdx.utilities.SoundObject_Sfx;
import static com.mygdx.utilities.UtilityVars.BIT_ATT;
import static com.mygdx.utilities.UtilityVars.BIT_PLAYER;
import static com.mygdx.utilities.UtilityVars.BIT_TEAR;
import static com.mygdx.utilities.UtilityVars.BIT_WALL;
import static com.mygdx.utilities.UtilityVars.PPM;

/**
 *
 * @author looch
 */
public class TearPortal extends StaticEntity{
    
    protected int linkid;
    protected ImageSprite dmgSprite, openSprite;
    protected float spriteScale = 1.0f;
    
    protected FixtureDef tearfd, warpfd;
    protected Object teardata, warpdata;
    protected Environment warpenv;//todo:not needed
    
    //description: has the tear been entered
    protected boolean finished = false;
    
    protected FrameCounter healFC;
    protected float beginTime, endTime, healTime;
    
    
    //sound
    protected SoundObject_Sfx warpSound;
    
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }
    public Environment getWarpEnv() { return warpenv; }
    
    public TearPortal(Vector2 pos, int linkid){
        super(pos,50f*RATIO,50f*RATIO);
        
        
        bd.position.set(pos.x/PPM,pos.y/PPM);
        cshape.setRadius(0.5f * width/PPM);
        
        teardata = "tear_" + id;
        tearfd = new FixtureDef();
        tearfd.filter.categoryBits = BIT_TEAR;
        tearfd.filter.maskBits = BIT_ATT;
        tearfd.shape = cshape;
        
        
        warpdata = "warp_" + id;
        warpfd = new FixtureDef();
        warpfd.filter.categoryBits = BIT_WALL;
        warpfd.filter.maskBits = BIT_PLAYER;
        warpfd.shape = cshape;
        
        userdata = teardata;
        fd = tearfd;
        
        this.linkid = linkid;
        
        MAX_HP = 15;
        CURRENT_HP = MAX_HP;
        
        flaggedForRenderSort = false;
        
        spriteScale = width / 64;
        
        //frame counter times for Env
        beginTime = 2.0f;
        endTime = 1.0f;
        
        healTime = 10;
        healFC = new FrameCounter(healTime);
        
        //sound
        warpSound = new SoundObject_Sfx(ResourceManager.SFX_WARP_IN);
        
    }
    
    
    @Override 
    public void init(World world){
        super.init(world);
        
        dmgSprite = new ImageSprite("damaged2",true);
        dmgSprite.sprite.setPosition(body.getPosition().x*PPM,body.getPosition().y*PPM);
        dmgSprite.sprite.setScale(spriteScale);
        
        openSprite = new ImageSprite("tear-open2",true);
        openSprite.sprite.setPosition(body.getPosition().x*PPM,body.getPosition().y*PPM);
        openSprite.sprite.setScale(spriteScale);
        
    }
    
    @Override
    public void update(){
        super.update();
        
        
        if(!dead && CURRENT_HP < MAX_HP){
            isprite = dmgSprite;
        }
        
        //heal tear if not finished
        if(!finished && !dead){
            if(healFC.complete)
                healTear();
            
            if(CURRENT_HP < MAX_HP
                    && !healFC.running)
                healFC.start(fm);
            
        }
        
    }
    
    @Override
    public void render(SpriteBatch sb){
        super.render(sb);
        
        if(isprite != null){
            isprite.sprite.setPosition(
                    (body.getPosition().x * PPM - isprite.sprite.getWidth() / 2),
                    (body.getPosition().y * PPM - isprite.sprite.getHeight() / 2));
            isprite.step();
            isprite.sprite.draw(sb);
        }
    }

    @Override
    public void death() {
        super.death();
        
        if(GameScreen.player.getAttTargets().contains(this)){
            GameScreen.player.getAttTargets().remove(this);
        }
        
        openWarp();
    }
    
    public void openWarp(){
        
        isprite = openSprite;
        
        Array<Fixture> fixtures = body.getFixtureList();
        for(Fixture f: fixtures)
            body.destroyFixture(f);
        userdata = warpdata;
        body.createFixture(warpfd).setUserData(warpdata);
        
        //play open sound
    }
    
    
    @Override
    public void alert(String string){
        if(string.equals("warp")){
            warp();
        }
        
        
        if(finished && string.equals("tear_resume")){
            Array<Fixture> fixtures = body.getFixtureList();
            for(Fixture f: fixtures)
                body.destroyFixture(f);
            userdata = teardata;
            body.createFixture(tearfd).setUserData(teardata);
            
            CURRENT_HP = 5;
            dead = false;
            deadCheck = true;
            
            isprite = dmgSprite;
        }
    }
    
    public void warp(){
        GameScreen.player.warp(body.getPosition());
        EnvironmentManager.currentEnv.end(id, endTime);
        finished = true;
        
        //play VOID_WARP sound
        warpSound.play(false);
    }
    
    public void addReward(Pickup item){
        EnvironmentManager.get(id).addReward(item);
    }
    
    private void healTear(){
        isprite = null;
        CURRENT_HP = MAX_HP;
        healFC.reset();
        
    }
    
    public void disable(){
        Array<Fixture> fixtures = body.getFixtureList();
        for(Fixture f: fixtures)
            body.destroyFixture(f);
    }
    
    public void enable(){
        userdata = teardata;
        body.createFixture(fd).setUserData(teardata);
    }
    
}