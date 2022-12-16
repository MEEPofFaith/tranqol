package tranqol;

import mindustry.mod.*;
import tranqol.content.*;

public class Tranqol extends Mod{

    public Tranqol(){}

    @Override
    public void loadContent(){
        TranqolBlocks.load();
        TranqolTechTree.load();
    }
}
