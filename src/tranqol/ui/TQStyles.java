package tranqol.ui;

import arc.scene.ui.ImageButton.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class TQStyles{
    public static ImageButtonStyle powerInfoStyle;

    public static void init(){
        powerInfoStyle = new ImageButtonStyle(Styles.defaulti){{
            up = down = checked = Tex.button;
            over = Tex.buttonOver;
        }};
    }
}
