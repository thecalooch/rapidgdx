/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.entities.DynamicEntities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.combat.NormAttackSensor;
import com.mygdx.combat.Buff;
import com.mygdx.combat.skills.Skill_HeavyBasic;
import com.mygdx.combat.skills.Skill_LightBasic;
import com.mygdx.combat.skills.Skill;
import com.mygdx.combat.skills.Skill.SkillType;
import static com.mygdx.combat.skills.Skill.SkillType.DEFENSE;
import static com.mygdx.combat.skills.Skill.SkillType.HEAVY;
import static com.mygdx.combat.skills.Skill.SkillType.LIGHT;
import static com.mygdx.combat.skills.Skill.SkillType.NONE;
import static com.mygdx.combat.skills.Skill.SkillType.PASSIVE;
import static com.mygdx.combat.skills.Skill.SkillType.SPECIAL;
import com.mygdx.entities.DynamicEntities.SteerableEntity;
import com.mygdx.entities.Entity;
import com.mygdx.entities.ImageSprite;
import com.mygdx.environments.EnvironmentManager;
import com.mygdx.game.MainGame;
import static com.mygdx.game.MainGame.RATIO;
import com.mygdx.managers.GameInputProcessor;
import com.mygdx.managers.ResourceManager;
import com.mygdx.managers.StateManager;
import com.mygdx.managers.StateManager.State;
import com.mygdx.screen.GameScreen;
import com.mygdx.utilities.Direction;
import com.mygdx.utilities.FrameCounter;
import com.mygdx.utilities.FrameCounter_Combo;
import com.mygdx.utilities.SoundObject_Sfx;
import com.mygdx.utilities.UtilityVars.AttackState;
import static com.mygdx.utilities.UtilityVars.BIT_EN;
import static com.mygdx.utilities.UtilityVars.BIT_PICKUP;
import static com.mygdx.utilities.UtilityVars.BIT_PLAYER;
import static com.mygdx.utilities.UtilityVars.BIT_WALL;
import static com.mygdx.utilities.UtilityVars.PPM;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author looch
 */
public class PlayerEntity extends SteerableEntity{
    
    //graphics
    
    protected ImageSprite frontSprite,backSprite, rightSprite, leftSprite;
    protected ImageSprite idleSprite, diveSprite, warpSprite, recovSprite;
    protected ImageSprite attackSprite, attackHeavySprite, playerBuffSprite;//player body animations
    //protected ImageSprite attackLeftSprite, attackRightSprite;
    protected ImageSprite beginSpectralSprite, deathSprite;
    
    protected float spriteScale = 1.0f;
    protected boolean warping = false;
    protected float endTime = 2.0f;
    
    
    //***********************************************
    //PLAYER STATS
    
    protected String playerName = "player";
    
    //base values for each stat (count == 1 will equal base value)
    protected final float BASE_LIFE = 15f;
    protected final int BASE_ENERGY = 1;
    protected final float BASE_DAMAGE = 1.0f;
    protected final float BASE_SPEED = 35f * RATIO;
    protected final float BASE_SPECIAL = 1.0f;
    
    //stat points
    protected int LIFE_STAT_COUNT = 1;
    protected int ENERGY_STAT_COUNT = 1;
    protected int DAMAGE_STAT_COUNT = 1;
    protected int SPEED_STAT_COUNT = 1;
    protected int SPECIAL_STAT_COUNT = 1;
    
    //values for each stat type, (COUNT * VALUE = ingame number)
    protected final float LIFE_STAT_VALUE = 10f;
    protected final int ENERGY_STAT_VALUE = 1;  
    protected final float DAMAGE_STAT_VALUE = 1.0f;
    protected final float SPEED_STAT_VALUE = 2f;
    protected final float SPECIAL_STAT_VALUE = 0.2f;
    
    protected float CURRENT_LIFE;
    protected int CURRENT_ENERGY_MAX;
    protected float CURRENT_DAMAGE;
    protected float CURRENT_SPEED;
    protected float CURRENT_SPECIAL;
    
    //ingame stats
    protected float life; //current life, <= CURRENT_LIFE
    protected int energy; //current energy, <= CURRENT_ENERGY
    protected int soulCount = 0;
    protected int SOUL_MAX = 3;
    protected Texture soulTexture;
    
    public String getPlayerName() { return playerName; }
    public float getLife() { return life; }
    public int getEnergy() { return energy; }
    //public float getEnergyRegen() { return ENERGY_REGEN; }  
    public float getCurrentLife() { return CURRENT_LIFE; }
    public int getCurrentEnergyMax() { return CURRENT_ENERGY_MAX; }
    public float getCurrentDamage() { return CURRENT_DAMAGE; }
    public float getCurrentSpeed() { return CURRENT_SPEED; }
    public float getCurrentSpecial() { return CURRENT_SPECIAL; }
    public int getLifeStatCount() { return LIFE_STAT_COUNT; }
    public int getEnergyStatCount() { return ENERGY_STAT_COUNT; }
    public int getDamagerStatCount() { return DAMAGE_STAT_COUNT; }
    public int getSpeedStatCount() { return SPEED_STAT_COUNT; }
    public int getSpecialStatCount() { return SPECIAL_STAT_COUNT; }
    public float getBaseSpeed() { return BASE_SPEED; }
    public int getSoulCount() { return soulCount; }
    
    
    public void setCurrentDamage(float dmg) { this.CURRENT_DAMAGE = dmg; }
    public void setCurrentSpeed(float speed) { this.CURRENT_SPEED = speed; }
    
   
    //***********************************************
    //damage to player
    private FrameCounter dmgFC = new FrameCounter(0.25f);
    
    //movement
    private boolean moveUp, moveDown,moveLeft,moveRight = false;
    private Vector2 currentDirection = new Vector2(0,0);
    private Vector2 prevLocation = new Vector2(0,0);
    private float currentAngle = 0;

    //dash
    private final Array<Sprite> dashSprites = new Array<Sprite>();
    private final Array<Float> dashAlphas = new Array<Float>();
    
    //player state
    private final StateManager sm;
    
    //Combat
    private LinkedBlockingQueue<Integer> attackQueue = new LinkedBlockingQueue<Integer>();
    private Array<SkillType []> comboChains = new Array<SkillType []>();
    
    private FrameCounter_Combo attackFC = new FrameCounter_Combo(0, 0,0,0);
    
    protected final Skill[] skillSet = {null,null,null,null, null};
    private Skill currentSkill;     //SKILL_LIGHT, SKILL_HEAVY, SKILL_SPEC, SKILL_PASSIVE;
    private float LIGHT_MOD, HEAVY_MOD, SPECIAL_MOD;
    
    
    private final ArrayList<Entity> attTargets = new ArrayList<Entity>();
    private NormAttackSensor normAttSensor;
    
    private Array<ImageSprite> skillSprites = new Array<ImageSprite>();//collection of skillSprite, skillHeavySprite
    private Array<ImageSprite> skillsToRemove = new Array<ImageSprite>();
    private Array<ImageSprite> impactSprites = new Array<ImageSprite>();//todo: place in EntityEnemy, not here
    private Array<Float> impactAlphas = new Array<Float>();
    
    
    
    //buffs
    private final Array<Buff> buffs = new Array<Buff>();
    private final Array<Buff> buffsToAdd = new Array<Buff>();
    private final Array<Buff> buffsToRemove = new Array<Buff>();
    
    //items
    private BitmapFont font;
    private Texture actionTexture;
    
    //***************
    //SOUND
    private SoundObject_Sfx SFX_YELL1, SFX_YELL2, SFX_YELL3, SFX_DMG;
    private Array<SoundObject_Sfx> SFX_YELLS = new Array<SoundObject_Sfx>();
    private SoundObject_Sfx soulSound;
    
    //***************
    
    public boolean isDead() { return dead; }
    public ImageSprite getBeginSpectralSprite() { return beginSpectralSprite; }
    public ImageSprite getDeathSprite() { return deathSprite; }
    public ImageSprite getRecovSprite() { return recovSprite; }
    public ImageSprite getDiveSprite() { return diveSprite; }
    public ImageSprite getBuffSprite() { return playerBuffSprite; }
    public Texture getSoulTexture() { return soulTexture; }
    public ArrayList<Entity> getAttTargets() { return attTargets; }
    public Skill[] getSkillSet() { return skillSet; }
    public StateManager getStateManager() { return sm; }
    public float getSpriteScale() { return spriteScale; }
    public float getLightMod() { return LIGHT_MOD; }
    public float getHeavyMod() { return HEAVY_MOD; }
    public float getSpecialMod() { return SPECIAL_MOD; }
    public Array<Buff> getBuffs() { return buffs; }
    public NormAttackSensor getNormAttSensor() { return normAttSensor; }
    public float getCurrentAngle() { return currentAngle; } 
    public Vector2 getCurrentDirection() { return currentDirection; }
    
    public void setLightMod(float light) { this.LIGHT_MOD = light; }
    public void setHeavyMod(float heavy) { this.HEAVY_MOD = heavy; }
    public void setSpecialMod(float special) { this.SPECIAL_MOD = special; }
    
    
    public PlayerEntity(Vector2 pos, float w, float h){
        super(pos,w,h);
        
        userdata = "player_" + id;
        fd.filter.categoryBits = BIT_PLAYER;
        fd.filter.maskBits = BIT_WALL | BIT_EN | BIT_PICKUP;
        fd.restitution = 0.05f;
        fd.density = 5.0f;
        fd.shape = shape;
        bd.fixedRotation = true;
        sensordata = "playatt_norm";
        
        sm = new StateManager();
        
        //*********     STATS & SKILLS     ******************
        LIFE_STAT_COUNT = 1;
        ENERGY_STAT_COUNT = 1;
        DAMAGE_STAT_COUNT = 1;
        SPEED_STAT_COUNT = 1;
        SPECIAL_STAT_COUNT = 1;
        
        refreshStats();
        
        LIGHT_MOD = 1.0f;
        HEAVY_MOD = 1.0f;
        SPECIAL_MOD = 1.0f;
        RANGE = 1.3f*RATIO;     //needed for NormalAttackSensor size, radius
        
        life = CURRENT_LIFE;
        
        
        //Default Light and Heavy skills
        //skillSet[0] = new Skill_LightBasic();
        //skillSet[1] = new Skill_HeavyBasic();
        
        
        //soul - default texture
        soulTexture = MainGame.am.get(ResourceManager.SKILL_RED);
        
        //***************************************************
        
        
        beginSpectralSprite = new ImageSprite("poeSpectral", false);
        beginSpectralSprite.sprite.setScale(1.06f*RATIO);
        deathSprite = new ImageSprite("poe-death", false);
        
        
        
        
        
        SFX_DMG = new SoundObject_Sfx(ResourceManager.SFX_POE_DMG);
        
        soulSound = new SoundObject_Sfx(ResourceManager.SFX_PICKUP_SKILL);
        
        //font
        font = new BitmapFont(Gdx.files.internal("fonts/nav-impact.fnt"));
        font.setScale(RATIO);
        
        //action key
        actionTexture = MainGame.am.get(ResourceManager.GUI_PAD_Y);
        
    }
    
    @Override
    public void init(World world) {
        super.init(world);
        System.out.println("@PlayerEntity init");
        //combat
        //todo: might not need initial init
        attackFC = new FrameCounter_Combo(0,0,0,0);
        
        //combat
        normAttSensor = new NormAttackSensor(this);
        body.createFixture(normAttSensor).setUserData(sensordata);
        body.resetMassData();   //needed for setting density
        
        
        //activate passive skills
        for(Skill skill: skillSet){
            if(skill != null){
                if(skill.getType() == SkillType.PASSIVE && !skill.isActive()){
                    skill.effect();
                }else{
                    skill.activate();
                }
            }
        }
        
        warping = false;
    }
    
    @Override
    public void render(SpriteBatch sb){
        
        //attack sprite
        for(ImageSprite e: skillsToRemove){
            skillSprites.removeValue(e, false);
        }
        
        for(ImageSprite e: skillSprites){
            e.render(sb);
            
            if(e.isComplete()){
                skillsToRemove.add(e);
            }
        }
        
        
        if(isprite != null){
            isprite.render(sb);
        
        }else
            super.render(sb);
        
        
        renderSprites(sb);
        
        
        //dash effect
        for(int i = 0; i < dashSprites.size; i++){
            dashSprites.get(i).setAlpha(dashAlphas.get(i));
            dashAlphas.set(i, dashAlphas.get(i) - 0.15f < 0 ? 0 : dashAlphas.get(i) - 0.15f);
            dashSprites.get(i).draw(sb);
        }
        
        //impact effect
        for(int i = 0; i < impactSprites.size; i++){
            impactSprites.get(i).sprite.setAlpha(impactAlphas.get(i));
            
            if(impactAlphas.get(i) >0.75f)
                impactSprites.get(i).sprite.setScale(impactSprites.get(i).sprite.getScaleX()* impactAlphas.get(i));
            
            impactSprites.get(i).step();
            
            //flip sprite
            if(impactSprites.get(i).getXFlip())  
                impactSprites.get(i).sprite.flip(true, false);
            
            impactSprites.get(i).sprite.draw(sb);
            
            //unflip sprite
            if(impactSprites.get(i).getXFlip())  
                impactSprites.get(i).sprite.flip(true, false);
            
            impactAlphas.set(i, impactAlphas.get(i) - 0.05f < 0 ? 0 : impactAlphas.get(i) - 0.05f);
        }
        
        //particle effects
        renderEffects(sb);
        
        renderActionKey(sb);
    }
    
    
    
    @Override 
    public void renderEffects(SpriteBatch sb){
        
        for(PooledEffect e : effects){
            e.setPosition(
                    body.getPosition().x*PPM - e.getEmitters().peek().getXOffsetValue().getLowMax(), 
                    body.getPosition().y*PPM - height);
        }
        super.renderEffects(sb);
    }
    
    public void renderActionKey(SpriteBatch sb){
        if(actionEntities.size > 0){
            if (!GameInputProcessor.controller) {
                font.draw(sb,
                        Keys.toString(GameInputProcessor.KEY_ACTION_ACTION),
                        pos.x - width,
                        pos.y + height * 2.5f + font.getCapHeight());
            } else {
                sb.draw(
                        actionTexture,
                        pos.x - width,
                        pos.y + height * 2.5f,
                        50f * RATIO, 50f * RATIO);
            }
        }
    }
    
    private void updateSprites() {
         if(attackFC.complete){//TODO: clean up to only execute once
            boolean clearAlphas = true;
            for (Float alpha : impactAlphas) {
                if (alpha > 0) {
                    clearAlphas = false;
                }
            }
            
            if(clearAlphas){
                impactSprites.clear();
                impactAlphas.clear();
            }
            
            if(isprite != null)
                isprite.sprite.setAlpha(1.0f);//TODO clean up
        }
         
         
         //skillSprites
         for(ImageSprite i : skillSprites){
            i.step();
         }
        
        //impact effect
        for(int i = 0; i < impactSprites.size; i++){
            impactSprites.get(i).sprite.setAlpha(impactAlphas.get(i));
            
            if(impactAlphas.get(i) >0.75f)
                impactSprites.get(i).sprite.setScale(impactSprites.get(i).sprite.getScaleX()* impactAlphas.get(i));
            
            impactSprites.get(i).step();
            
            //flip sprite
            
            
            impactAlphas.set(i, impactAlphas.get(i) - 0.05f < 0 ? 0 : impactAlphas.get(i) - 0.05f);
        }
        
        if(isprite != null && !warping){
            
            //damage animation
            if(dmgFC.running){
                isprite = recovSprite;
                isprite.sprite.setAlpha(0.65f);
            }else{
                isprite.sprite.setAlpha(1.0f);
            }
            
            if (isprite.getXFlip()) {
                isprite.sprite.setPosition(
                        (pos.x + (isprite.sprite.getWidth() * 0.15f)),
                        (pos.y - (isprite.sprite.getHeight() / 2)));
            } else {
                isprite.sprite.setPosition(
                        (pos.x - (isprite.sprite.getWidth() / 2)),
                        (pos.y - (isprite.sprite.getHeight() / 2)));
            }
            
            //isprite.render(sb);
            
            //*******************
            //dash effect
            
            /*
            if(dashFC.running){
                dashSprites.add(new Sprite(isprite.sprite));
                dashAlphas.add(1.0f);
            }else if(dashFC.complete && dashSprites.size > 0){
                boolean clearAlphas = true;
                for (Float alpha : dashAlphas) {
                    if (alpha > 0) {
                        clearAlphas = false;
                    }
                }
            
            
                if(clearAlphas){
                    dashSprites.clear();
                    dashAlphas.clear();
                }
            //*******************
            }*/
            
        }
        //dash effect
        /*
        for(int i = 0; i < dashSprites.size; i++){
            dashSprites.get(i).setAlpha(dashAlphas.get(i));
            dashAlphas.set(i, dashAlphas.get(i) - 0.15f < 0 ? 0 : dashAlphas.get(i) - 0.15f);
            
        }*/
    }
    
    private void renderSprites(SpriteBatch sb){
        
        
        for(ImageSprite i : impactSprites){
            if(i.getXFlip())  
                i.sprite.flip(true, false);
            
            i.sprite.draw(sb);
            
            //unflip sprite
            if(i.getXFlip())  
                i.sprite.flip(true, false);
        }
        
        for(Sprite i : dashSprites){
            i.draw(sb);
        }
    }
    

    @Override
    public void update() {
        super.update();
        
        //todo: have this check in env.update(), not here
        //if(EnvironmentManager.currentEnv.getStateManager().getState() == State.PLAYING){
        
            
        
            moveUpdate();
            
            //combat
            updateSkill();
            
            
            //impact
            updateSprites();

            
            //buffs
            updateBuffs();
            
            
            //direction
            updateDirection();

        
        //}
    }
    
    
    @Override
    public void death(){
        super.death();
        
        //EnvironmentManager.respawn();
        
        
    }
    
    public void killSwitch(){
        life = 0;
    }
    
    public void revive(){
        dead = false;
        life = CURRENT_LIFE*0.5f;
    }
    
    private Vector2 dv = new Vector2(0,0);
    
    public void moveUpdate() {
        
        if(body == null) return;
        
        //applies movement and adjusts sprites
        dv.x = 0;
        dv.y = 0;
        //if (!dmgFC.running && !dashFC.running) {
        if (!dmgFC.running){
            //if (moveDown && -CURRENT_SPEED * DASHMOD < body.getLinearVelocity().y) {
            if (moveDown && -CURRENT_SPEED < body.getLinearVelocity().y) {
                dv.y -= 1;
            }

            //if (moveUp && CURRENT_SPEED * DASHMOD > body.getLinearVelocity().y) {
            if (moveUp && CURRENT_SPEED > body.getLinearVelocity().y) {
                dv.y += 1;
            }

            //if (moveRight && CURRENT_SPEED * DASHMOD > body.getLinearVelocity().x) {
            if (moveRight && CURRENT_SPEED > body.getLinearVelocity().x) {
                dv.x += 1;
            }

            //if (moveLeft && -CURRENT_SPEED * DASHMOD < body.getLinearVelocity().x) {
            if (moveLeft && -CURRENT_SPEED < body.getLinearVelocity().x) {
                dv.x -= 1;
            }
            
            float dv_scale = attackFC.running ? CURRENT_SPEED * 0.25f : CURRENT_SPEED;
            
            dv.nor().scl(dv_scale);
            body.applyForce(dv, body.getPosition(), true);

            if (!(moveUp || moveDown || moveRight || moveLeft)
                    || ( (moveUp && moveDown) || (moveLeft && moveRight) )) {
                isprite = idleSprite;
            }
            
            float ang = body.getAngle();
            if(moveDown && !moveUp){
                isprite = frontSprite;
                ang = (float)(180*Math.PI/180);
                //body.setTransform(body.getPosition(), (float)(180 * Math.PI/180));
            }
            if(moveUp && !moveDown){
                isprite = backSprite;
                ang = 0;
                //body.setTransform(body.getPosition(), 0);
            }
            if(moveRight && !moveLeft){
                isprite = rightSprite;
                ang = (float)(270*Math.PI/180);
                //body.setTransform(body.getPosition(), (float)(270 * Math.PI/180));
                
            }
            if(moveLeft && !moveRight){
                isprite = leftSprite;
                ang = (float)(90*Math.PI/180);
                //body.setTransform(body.getPosition(), (float)(90 * Math.PI/180));
            }
            
            if(body.getAngle() != ang){
                //body.setTransform(body.getPosition(), ang);
            }
        }
    }
    
    //@param: Direction up,down,left,right (U,D,L,R)
    //responsible for getting input and setting direction states
    public void move(Direction d) {
        if (sm.getState() == State.PLAYING) {
            switch (d) {
                case UP:
                    moveUp = true;
                    
                    break;
                case DOWN:
                    moveDown = true;
                    
                    break;
                case LEFT:
                    moveLeft = true;
                    
                    break;
                case RIGHT:
                    moveRight = true;
                    
                    break;
                default:
                    break;
            }
        }
    }
    
    //@param: Direction up,down,left,right (U,D,L,R)
    public  void moveStop(Direction d) {
        switch (d) {
            case UP:
                moveUp = false;
                break;
            case DOWN:
                moveDown = false;
                break;
            case LEFT:
                moveLeft = false;
                break;
            case RIGHT:
                moveRight = false;
                break;
            default:
                break;
        }
    }
    
    private void updateDirection(){
        
        if(body == null) return;
        
        currentDirection = body.getPosition().cpy().sub(prevLocation.cpy()).nor();
        prevLocation = body.getPosition().cpy();
        
        currentAngle = currentDirection.angleRad();
        
    }
    
    public boolean isUserMoving() { return moveUp || moveRight || moveLeft || moveDown; }
    
    public void clearMovement(){
        moveUp = false;
        moveDown = false;
        moveRight = false;
        moveLeft = false;
    }
    
    @Override
    public void damage(float damage){
        if(!dmgFC.running){
            
            life -= damage;
            System.out.println("@PlayerEntity " + this.toString() + " damaged - hp: " + life);

            if (life <= 0) {
                dead = true;
            }

            EnvironmentManager.currentEnv.addDamageText(
                    "" + (int) (damage + 1) + "",
                    new Vector2(
                            pos.x - width * rng.nextFloat() / 2,
                            pos.y * PPM + height * 1.1f),
                            "red");
            
            
            isprite = recovSprite;
            isprite.reset();
            
            dmgFC.start(fm);
            
            //sound
            SFX_DMG.play(false);
            
            //screen shake
            GameScreen.camera.shake(2.5f, 0.3f);
        }
    }
    
    
    
    
    
    
    public void attack(int index) throws InterruptedException {
        if (sm.getState() == State.PLAYING
                && skillSet[index] != null
                && !dmgFC.running){                               //while not being damaged

            //CHECK TO RECIEVE INPUT FOR attackQueue
            if(!attackFC.running && attackBlocked){
                //unblock attack input
                attackBlocked = false;
            }
                
            if(!attackBlocked){
                
                attackQueue.put(index);
            }
            
            
        }
    }
    
    
   
    
    //keep track of count in current combo chain
    protected Array<SkillType> currentComboChain = new Array<SkillType>();
    protected boolean attackBlocked = false;
    
    //needed to init combo skill of first skill in combo chain
    protected Skill comboSkill;
    
    private void updateSkill() {
        
        /*********************
            ATTACK QUEUE
        **********************/
        
        if(!attackQueue.isEmpty()){
            
            if(!attackFC.running){
                //start new combo chain
                
                initNewSkill(attackQueue.poll(), false);
                currentComboChain.clear();
                if(currentSkill != null){
                    currentComboChain.add(currentSkill.getType());
                }
            }else if (attackFC.state == AttackState.COMBO) {
                
                //get type of skill we are checking
                int pollIndex = attackQueue.poll();
                SkillType pollType = NONE;
                switch (pollIndex) {
                    case 0:
                        pollType = LIGHT;
                        break;
                    case 1:
                        pollType = HEAVY;
                        break;
                    case 2:
                        pollType = SPECIAL;
                        break;
                    case 3:
                        pollType = PASSIVE;
                        break;
                    case 4:
                        pollType = DEFENSE;
                        break;
                    default:
                        pollType = NONE;
                        break;
                }

                //Check available comboChains for completed combo
                boolean comboClear = true;
                for(SkillType[] t : comboChains){
                    
                    try {
                        if (currentComboChain.size >= t.length) {
                            continue;
                        }

                        comboClear = true;
                        //check current combo chain
                        for (int i = 0; i < t.length; i++) {
                            
                            //check current chain against this chian
                            if(i < currentComboChain.size){
                                //break chain if currentChain does not follow this chain
                                if (currentComboChain.get(i) != t[i]) {
                                    comboClear = false;
                                    break;
                                }
                            }else if(i == currentComboChain.size){
                                //check pollType in this chain
                                if(pollType != t[i]){
                                    //continue with this chain
                                    comboClear = false;
                                    break;
                                }
                            }
                            
                            if(!comboClear) break;
                            
                        }

                        //if continuing on this combo chain
                        if (comboClear) {
                            //continue combo
                            initNewSkill(pollIndex, true);
                            currentComboChain.add(currentSkill.getType());
                            
                            //if end of this comboChain, enable comboEffect
                            if(currentComboChain.size == t.length){
                                //currentSkill, combo effect of first skill in chain
                                int comboIndex = 0;
                                switch(currentComboChain.get(0)){
                                    case LIGHT:
                                        comboIndex = 0;
                                        break;
                                    case HEAVY:
                                        comboIndex = 1;
                                        break;
                                    case SPECIAL:
                                        comboIndex = 2;
                                        break;
                                    case PASSIVE:
                                        comboIndex = 3;
                                        break;
                                    case DEFENSE:
                                        comboIndex = 4;
                                        break;
                                    default:
                                        break;
                                        
                                }
                                
                                comboSkill = skillSet[comboIndex];
                                
                                //currentSkill.combo();
                                
                                //end current attackQueue
                                attackQueue.clear();
                                
                                //block new input until end of this attackFC
                                attackBlocked = true;
                            }
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                    
                    //completed combo chain, do not check any more chains
                    if(comboClear)  break;
                }
                
                //For failed combo chain, (chain is not in this player's combo chain pool)
                if(!comboClear) {
                    
                    attackQueue.clear();
                }
            }
            
        }
        
        
        /***************************
            CURRENT SKILL EFFECT
        ****************************/
        if (attackFC.running) {

            //current skill effect
            if (attackFC.state == AttackState.ATTACKING) {

                if (currentSkill != null && currentSkill.isActive()) {
                    currentSkill.effect();
                }
            }

            //combo effect
            //todo: old
            if (comboSkill != null) {
                comboSkill.comboEffect();
                comboSkill = null;
            }

            //update player attack sprite
            switch (currentSkill.getType()) {
                case LIGHT:
                    /*
                    if(moveLeft){
                        isprite = attackLeftSprite;
                    }else if(moveRight){
                        isprite = attackRightSprite;
                    }else{
                        isprite = attackSprite;
                    }*/
                    isprite = attackSprite;
                    break;
                case HEAVY:
                    isprite = attackHeavySprite;
                    isprite.setPause(attackFC.state == AttackState.PREPPING);
                    break;
                case SPECIAL:
                    isprite = playerBuffSprite;
                    break;
                default:
                    
                    break;  
            }
            
        }
        
        
    }
    
    
    private void initNewSkill(int index, boolean combo){
        
        currentSkill = null;
        
        //return if not enough energy
        if(skillSet[index].getCost() > 0 && energy < skillSet[index].getCost()){
            return;
        }else{
            this.removeEnergy(skillSet[index].getCost());
        }
        
        
        currentSkill = skillSet[index];

        //If combo, skill PREP
        FrameCounter_Combo tf = currentSkill.getComboFC();
        attackFC = combo ? new FrameCounter_Combo(0,tf.attTime_real, tf.comboTime_real,tf.recovTime_real) : tf;
        
        attackFC.start(fm);

        //execute background effects of skills (buffs, etc)
        //not the actual damage attack on enemy
        currentSkill.active();

        
        
        //reset appropriate player attack sprite
        switch(currentSkill.getType()){
            case LIGHT:
                attackSprite.reset();
                //attackLeftSprite.reset();
                //attackRightSprite.reset();
            case HEAVY:
                attackHeavySprite.reset();
                break;
            case SPECIAL:
                playerBuffSprite.reset();
                break;
            default:
                attackSprite.reset();
                break;
        }
        
        //sound
        //SFX_YELLS.random().play(false);

        
    }
    
    public void addSkillSprite(ImageSprite s){
        if(s != null){
            skillSprites.add(new ImageSprite(s,
                    body.getPosition().x * PPM - s.sprite.getWidth() / 2, 
                    body.getPosition().y * PPM - s.sprite.getHeight() / 2,
                    0.75f));
        }
    }
    
    public void setCurrentSkill(Skill skill){
       
        try {
            switch (skill.getType()) {
                case LIGHT:
                    if (skillSet[0] != null) {
                        skillSet[0].deactivate();
                    }
                    skillSet[0] = skill;
                    break;
                case HEAVY:
                    if (skillSet[1] != null) {
                        skillSet[1].deactivate();
                    }
                    skillSet[1] = skill;
                    break;
                case SPECIAL:
                    if (skillSet[2] != null) {
                        skillSet[2].deactivate();
                    }
                    skillSet[2] = skill;
                    break;
                case PASSIVE:
                    if (skillSet[3] != null) {
                        skillSet[3].deactivate();
                    }
                    skillSet[3] = skill;
                    break;
                case DEFENSE:
                    if(skillSet[4] != null){
                        skillSet[4].deactivate();
                    }
                    skillSet[4] = skill;
                    break;
                default:
                    return;
            }
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        skill.activate();
        
    }
    
    public void addComboChain(SkillType [] cc){
        if(!comboChains.contains(cc, false)){
            comboChains.add(cc);
        }
        
    }
    
    public void removeComboChain(SkillType [] cc){
        comboChains.removeValue(cc, true);
    }
    
    
    public void restoreHp(float l){
        
        float lp = l * this.LIFE_STAT_COUNT;
        float templife = this.life;
        this.life = 
                this.life + lp > CURRENT_LIFE ? CURRENT_LIFE 
                : this.life + lp;
        
        int ammountRestored = (int)(this.life - templife);
        
        if (ammountRestored > 0) {
            EnvironmentManager.currentEnv.addHealingText(
                    "" + ammountRestored + "",
                    new Vector2(
                            pos.x - width * rng.nextFloat() / 2,
                            pos.y + height * 1.1f));
        }
    }
    
    public void restoreHp(){
        this.life = CURRENT_LIFE;
    }
    
    public void restoreEnergy(int count){
        energy = energy + count > CURRENT_ENERGY_MAX ? CURRENT_ENERGY_MAX : energy + count;
    }
    
    public void removeEnergy(int count){
        energy = energy - count < 0 ? 0 : energy - count;
    }
    
    public Buff addBuff(Buff buff){
        buff.start();
        buff.applyBuff();
        buffsToAdd.add(buff);
        
        return buff;
    }
    
    public void removeBuff(Buff buff){
        buffsToRemove.add(buff);
    }
    
    public boolean hasBuff(Buff buff){
        return buffs.contains(buff, false);
    }
    
    private void updateBuffs(){
        for (Buff b : buffs) {
            b.update();
            if (b.isComplete()) {
                buffsToRemove.add(b);
            }
        }

        for (Buff b : buffsToRemove) {
            b.removeBuff();
            buffs.removeValue(b, false);
        }

        if (buffsToRemove.size > 0) {
            buffsToRemove.clear();
        }

        for (Buff b : buffsToAdd) {
            buffs.add(b);
        }
        if (buffsToAdd.size > 0) {
            buffsToAdd.clear();
        }
    }
    
    @Override
    public void alert(String[] str) {
        try {
            if (str[0].equals("begin") && str[1].contains(sensordata.toString())) {
                for (Entity e : EnvironmentManager.currentEnv.getEntities()) {
                    if (e.getUserData() != null
                            && e.getUserData().equals(str[2])) {

                        addTarget(e);
                    }
                }
            } else if (str[0].equals("end") && str[1].contains(sensordata.toString())) {
                for (Entity e : EnvironmentManager.currentEnv.getEntities()) {
                    if (e.getUserData() != null
                            && e.getUserData().equals(str[2])) {

                        removeTarget(e);
                    }
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
    }
    
    public void addTarget(Entity e){
        if(!attTargets.contains(e)){
            attTargets.add(e);
        }
    }
    
    public void removeTarget(Entity e){
        if(attTargets.contains(e)){
            attTargets.remove(e);
        }
    }
    
    //todo: needed or handled in individual skill??
    public void addImpactSprite(float x, float y, ImageSprite isprite){
        isprite.sprite.setPosition(x, y);
        impactSprites.add(isprite);
        
        impactAlphas.add(1.0f);
        for(ImageSprite impsp: impactSprites){
            impsp.sprite.setScale(spriteScale * 1.4f);
        }
    }
    
    //Description:  sets player warp animation during env end state
    //@param: pos - position of tear
    public void warp(Vector2 pos){
        
        warping = true;
        warpSprite.reset();
        warpSprite.sprite.setPosition(
                pos.x*PPM - warpSprite.sprite.getWidth()/2, 
                pos.y*PPM - warpSprite.sprite.getHeight()/2);
        isprite = warpSprite;
        
    }
    
    @Override
    public void fall(){
        diveSprite.sprite.setPosition(pos.x - diveSprite.sprite.getWidth()/2, pos.y - diveSprite.sprite.getHeight()/2);
        isprite = diveSprite;
        
        clearMovement();
    }
    
    //*****************************
    //Desctiption: for action events involving actionEntity
    //private Entity actionEntity;
    private Array<Entity> actionEntities = new Array<Entity>();
    
    public void inRangeForAction(Entity e){
        //show hot key
        //enable hotkey
        //actionEntity = e;
        actionEntities.add(e);
    }
    
    public void outRangeForAction(Entity e){
        //actionEntity = null;
        actionEntities.removeValue(e, false);
    }
    
    public void beginAction(){
        //if(actionEntity != null)
            //actionEntity.actionEvent();
        
        if(actionEntities.size > 0)
            actionEntities.peek().actionEvent();
    }
    
    public void clearActionEvents(){
        actionEntities.clear();
    }
    //*****************************
    
    public void addSoulPiece(int s){
        soulCount += s;
        
        //alert SoulHud
        GameScreen.overlay.addSoul(soulCount, SOUL_MAX);
        
        if(soulCount >= SOUL_MAX){
            soulCount = 0;
            SOUL_MAX++;
            System.out.println("@PlayereEntity soul up!");
            soulUp();
            soulSound.play(false);
            EnvironmentManager.currentEnv.addDamageText("SOUL UP", 
                    new Vector2(body.getPosition().x*PPM, body.getPosition().y*PPM));
        }
        
        
    }
    
    public void soulUp(){
        addStatPoints(1,1,1,1,1);
    }
    
    //Adds to stat counter
    public void addStatPoints(int life, int energy, int dmg, int speed, int special){
        LIFE_STAT_COUNT += life;
        ENERGY_STAT_COUNT += energy;
        DAMAGE_STAT_COUNT += dmg;
        SPEED_STAT_COUNT += speed;
        SPECIAL_STAT_COUNT += special;
        
        updateCurrentStatValues(life, energy, dmg, speed, special);
    }
    
    public void refreshStats(){
        CURRENT_LIFE =      BASE_LIFE +    LIFE_STAT_COUNT *   LIFE_STAT_VALUE;
        CURRENT_ENERGY_MAX =    BASE_ENERGY +  ENERGY_STAT_COUNT * ENERGY_STAT_VALUE;
        CURRENT_DAMAGE =    BASE_DAMAGE +  DAMAGE_STAT_COUNT * DAMAGE_STAT_VALUE;
        CURRENT_SPEED =     BASE_SPEED +   SPEED_STAT_COUNT *  SPEED_STAT_VALUE;
        CURRENT_SPECIAL =   BASE_SPECIAL + SPECIAL_STAT_COUNT* SPECIAL_STAT_VALUE;
        
        //refresh energy
        energy = CURRENT_ENERGY_MAX;
    }
    
    
    public void updateCurrentStatValues(int life, int energy, int dmg, int speed, int special){
        CURRENT_LIFE +=         LIFE_STAT_VALUE * life;
        CURRENT_ENERGY_MAX +=       ENERGY_STAT_VALUE * energy;
        CURRENT_DAMAGE +=       DAMAGE_STAT_VALUE * dmg;
        CURRENT_SPEED +=        SPEED_STAT_VALUE * speed;
        CURRENT_SPECIAL +=      SPECIAL_STAT_VALUE * special;
    }

    
}
