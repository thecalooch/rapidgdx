/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.entities.DynamicEntities.npcs;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.MainGame;
import com.mygdx.managers.ResourceManager;

/**
 *
 * @author looch
 */
public class Npc_SlumGuy1 extends NpcEntity{

    public Npc_SlumGuy1(Vector2 pos, float w, float h) {
        super(pos, w, h);
        
        texture = MainGame.am.get(ResourceManager.NPC_SLUMGUY2);
    }
    
}
