package tranqol.graphics;

import arc.graphics.g2d.*;
import arc.math.*;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Mathf.*;

public class TQDrawf{
    /** Draws a sprite that should be light-wise correct. Provided sprites must be similar in shape and face towards the right. */
    public static void spinSprite(TextureRegion base, TextureRegion bottomLeft, TextureRegion topRight, float x, float y, float w, float h, float r){
        float ar = mod(r, 360f);

        alpha(1f);
        if(ar > 45f && ar <= 225f){
            rect(base, x, y, w, h * -1f, r);
        }else{
            rect(base, x, y, w, h, r);
        }

        if(ar >= 180 && ar < 270){ //Bottom Left
            float a = Interp.slope.apply(Mathf.curve(ar, 180, 270));
            alpha(a);
            rect(bottomLeft, x, y, w, h, r);
        }else if(ar < 90 && ar >= 0){ //Top Right
            float a = Interp.slope.apply(Mathf.curve(ar, 0, 90));
            alpha(a);
            rect(topRight, x, y, w, h, r);
        }
        alpha(1f);
    }
}
