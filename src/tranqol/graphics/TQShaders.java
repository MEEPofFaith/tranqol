package tranqol.graphics;

import arc.graphics.gl.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class TQShaders{
    public static AlphaShader alphaShader;

    public static void init(){
        alphaShader = new AlphaShader();
    }

    public static class AlphaShader extends TQLoadShader{
        public float alpha = 1f;

        AlphaShader(){
            super("screenspace", "postalpha");
        }

        @Override
        public void apply(){
            setUniformf("u_alpha", alpha);
        }
    }

    public static class TQLoadShader extends Shader{
        public TQLoadShader(String vert, String frag){
            super(
                files.internal("shaders/" + vert + ".vert"),
                tree.get("shaders/" + frag + ".frag")
            );
        }

        public TQLoadShader(String frag){
            this("default", frag);
        }
    }
}
