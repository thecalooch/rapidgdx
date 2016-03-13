/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.entities.DynamicEntities.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.StreamUtils;
import com.mygdx.entities.esprites.EntitySprite;
import static com.mygdx.game.MainGame.RATIO;
import com.mygdx.screen.GameScreen;
import com.mygdx.utilities.FrameCounter_Attack;
import static com.mygdx.utilities.UtilityVars.BIT_EN;
import static com.mygdx.utilities.UtilityVars.BIT_PLAYER;
import static com.mygdx.utilities.UtilityVars.PPM;
import java.io.Reader;

/**
 *
 * @author looch
 */
public class En_DarklingLg extends EnemyEntity2{

    private FixtureDef attFd1, attFd2, attFd3, attFd4;
    
    public En_DarklingLg(Vector2 pos) {
        super(pos, 45f, 45f);
        
        moveSprite = new EntitySprite("darkling-move",true);
        moveSprite.sprite.setScale(0.57f);
        prepSprite = new EntitySprite("darkling-prep", true);
        prepSprite.sprite.setScale(0.57f);
        attackSprite = new EntitySprite("darkling-att", true);
        attackSprite.sprite.setScale(0.57f);
        
        this.maxLinearSpeed = 50f;
        this.maxLinearAcceleration = 500f;
        this.maxAngularSpeed = 3000f;
        this.maxAngularAcceleration = 500f;
        
        MAX_HP = 60;
        CURRENT_HP = MAX_HP;
        DAMAGE = 25;
        
        prepTime = 0.85f;
        attTime = 0.3f;
        recovTime = 5f;
        attackFC = new FrameCounter_Attack(prepTime, attTime, recovTime);
        
        moveToSB = new Arrive<Vector2>(this, GameScreen.player)
                .setTimeToTarget(0.01f)
                .setArrivalTolerance(2f)
                .setDecelerationRadius(10);
        
        wanderSB = new Wander<Vector2>(this)
                .setFaceEnabled(false)
                .setAlignTolerance(0.001f)
                .setDecelerationRadius(5)
                .setTimeToTarget(0.1f)
                .setWanderOffset(90)
                .setWanderOrientation(10)
                .setWanderRadius(40f)
                .setWanderRate(MathUtils.PI2 * 4);
                
        seekWanderSB = new Seek<Vector2>(this, seekWanderTarget);
        
        
        attFd1 = new FixtureDef();
        PolygonShape vertBox = new PolygonShape();
        vertBox.setAsBox(width*0.4f/PPM, (113f/PPM)*RATIO, new Vector2(0,(113f/PPM)*RATIO), 0);
        attFd1.shape = vertBox;
        attFd1.isSensor = true;
        attFd1.filter.categoryBits = BIT_EN;
        attFd1.filter.maskBits = BIT_PLAYER;
        
        
        attFd2 = new FixtureDef();
        PolygonShape horzBox = new PolygonShape();
        horzBox.setAsBox((113f/PPM)*RATIO, width*0.4f/PPM, new Vector2((113f/PPM)*RATIO,0), 0);
        attFd2.shape = horzBox;
        attFd2.isSensor = true;
        attFd2.filter.categoryBits = BIT_EN;
        attFd2.filter.maskBits = BIT_PLAYER;
        
        attFd3 = new FixtureDef();
        PolygonShape vertBoxSouth = new PolygonShape();
        vertBoxSouth.setAsBox(width*0.4f/PPM, (113f/PPM)*RATIO, new Vector2(0,-(113f/PPM)*RATIO), 0);
        attFd3.shape = vertBoxSouth;
        attFd3.isSensor = true;
        attFd3.filter.categoryBits = BIT_EN;
        attFd3.filter.maskBits = BIT_PLAYER;
        
        attFd4 = new FixtureDef();
        PolygonShape horzBoxWest = new PolygonShape();
        horzBoxWest.setAsBox((113f/PPM)*RATIO, width*0.4f/PPM, new Vector2(-(113f/PPM)*RATIO,0), 0);
        attFd4.shape = horzBoxWest;
        attFd4.isSensor = true;
        attFd4.filter.categoryBits = BIT_EN;
        attFd4.filter.maskBits = BIT_PLAYER;
        
    }
    
    @Override
    public void init(World world){
        super.init(world);
        
        //att sensors
        body.createFixture(attFd1).setUserData(attSensorData);
        body.createFixture(attFd2).setUserData(attSensorData);
        body.createFixture(attFd3).setUserData(attSensorData);
        body.createFixture(attFd4).setUserData(attSensorData);
        
        //ai
        Reader reader = null;
        try {
            reader = Gdx.files.internal("ai/enemies/en_goober2.tree").reader();
            BehaviorTreeParser<EnemyEntity2> parser = new BehaviorTreeParser<EnemyEntity2>(BehaviorTreeParser.DEBUG_NONE);
            enemybt = parser.parse(reader,this);
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }
    
    
    @Override
    public void attack(){
        super.attack();
        
        Vector2 attDest = body.getPosition().cpy().sub(
                GameScreen.player.getBody().getPosition().cpy());

        body.applyForce(attDest.scl(-1000.0f), body.getPosition(), true);


        System.out.println("@En_Darkling attack");
    }
}
