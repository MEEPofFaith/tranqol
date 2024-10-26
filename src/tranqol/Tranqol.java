package tranqol;

import arc.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import tranqol.content.*;
import tranqol.graphics.*;
import tranqol.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Tranqol extends Mod{
    public Tranqol(){
        // Load all assets once they're added into Vars.tree
        Events.on(FileTreeInitEvent.class, e -> app.post(() -> {
            if(!headless){
                TQShaders.init();
            }
        }));
    }

    @Override
    public void init(){
        if(!headless){
            TQDialogs.init();
        }
    }

    @Override
    public void loadContent(){
        TQBlocks.load();
        TQTechTree.load();
    }
}
