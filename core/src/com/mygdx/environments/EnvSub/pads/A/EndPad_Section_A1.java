/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.environments.EnvSub.pads.A;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.environments.EnvSub.pads.EndPad_Section;
import com.mygdx.environments.EnvSub.pads.EndPiece;
import com.mygdx.game.MainGame;
import com.mygdx.managers.ResourceManager;

/**
 *
 * @author looch
 */
public class EndPad_Section_A1 extends EndPad_Section{

    public EndPad_Section_A1(Vector2 pos) {
        super(pos, 144f, 117f);
        
        piece = new EndPiece_A1();
        emptyTexture = MainGame.am.get(ResourceManager.ENDSECTION_A_1);
        fillTexture = MainGame.am.get(ResourceManager.ENDSECTION_A_1_FILL);
        
        texture = emptyTexture;
    }
    
    @Override
    public EndPad_Section copy(){
        return new EndPad_Section_A1(pos);
    }
    
    private class EndPiece_A1 extends EndPiece {

        public EndPiece_A1(Vector2 pos) {
            super(pos);

            texture = MainGame.am.get(ResourceManager.ENDPIECE_A_1);
            name = "Wierd Piece " + id;
        }

        public EndPiece_A1() {
            this(new Vector2(0, 0));
        }

    }

}
