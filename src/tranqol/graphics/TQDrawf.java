package tranqol.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Mathf.*;
import static mindustry.Vars.*;
import static tranqol.graphics.TQShaders.*;

public class TQDrawf{
    private static void drawSpinSprite(TextureRegion[] regions, float x, float y, float w, float h, float r){
        float ar = mod(r, 360f);

        alpha(1f);
        if(ar > 45f && ar <= 225f){
            rect(regions[0], x, y, w, h * -1f, r);
        }else{
            rect(regions[0], x, y, w, h, r);
        }

        if(ar >= 180 && ar < 270){ //Bottom Left
            float a = Interp.slope.apply(Mathf.curve(ar, 180, 270));
            alpha(a);
            rect(regions[1], x, y, w, h, r);
        }else if(ar < 90 && ar >= 0){ //Top Right
            float a = Interp.slope.apply(Mathf.curve(ar, 0, 90));
            alpha(a);
            rect(regions[2], x, y, w, h, r);
        }
        alpha(1f);
    }

    /** Draws a sprite that should be light-wise correct. Provided sprites must be similar in shape and face towards the right. */
    public static void spinSprite(TextureRegion[] regions, float x, float y, float w, float h, float r, float alpha){
        if(alpha < 0.99f){
            FrameBuffer buffer = renderer.effectBuffer;
            float z = Draw.z();
            float xScl = xscl, yScl = yscl;
            Draw.draw(z, () -> {
                buffer.begin(Color.clear);
                Draw.scl(xScl, yScl);
                drawSpinSprite(regions, x, y, w, h, r);
                buffer.end();

                alphaShader.alpha = alpha;
                buffer.blit(alphaShader);
            });
        }else{
            drawSpinSprite(regions, x, y, w, h, r);
        }
    }

    /** Draws a sprite that should be light-wise correct. Provided sprites must be similar in shape and face towards the right. */
    public static void spinSprite(TextureRegion[] regions, float x, float y, float w, float h, float r){
        spinSprite(regions, x, y, w, h, r, 1f);
    }


    /** Draws a sprite that should be light-wise correct. Provided sprites must be similar in shape and face towards the right. */
    public static void spinSprite(TextureRegion[] regions, float x, float y, float r, float alpha){
        spinSprite(regions, x, y, regions[0].width / 4f, regions[0].height / 4f, r, alpha);
    }

    /** Draws a sprite that should be light-wise correct. Provided sprites must be similar in shape and face towards the right. */
    public static void spinSprite(TextureRegion[] regions, float x, float y, float r){
        spinSprite(regions, x, y, regions[0].width / 4f, regions[0].height / 4f, r);
    }
}
