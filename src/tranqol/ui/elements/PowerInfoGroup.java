package tranqol.ui.elements;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import tranqol.ui.*;
import tranqol.ui.dialogs.PowerGraphInfoDialog.*;

public class PowerInfoGroup extends Table{
    private static final int cols = 3;

    public PowerInfoGroup(Seq<Building> buildings, PowerInfoType type, boolean isOpen, InfoToggled toggled){
        Table collT = new Table();
        int col = 0;
        for(int i = 0; i < buildings.size; i++){
            Building b = buildings.get(i);
            collT.label(() -> "[lightgray](" + b.tileX() + ", " + b.tileY() + "): " + getData(b, type)).pad(6f);

            col++;
            if(col == cols){
                collT.row();
                col = 0;
            }
        }

        Collapser coll = new Collapser(collT, !isOpen);

        Block block = buildings.first().block;
        Button b = button(Icon.downOpen, TQStyles.powerInfoStyle, () -> {
            coll.toggle(false);
            toggled.get(block.id, !coll.isCollapsed());
        }).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).growX().top().get();
        b.image(block.fullIcon).size(24f).padLeft(6f);
        b.add(block.localizedName).padLeft(6f);

        row();

        add(coll).grow();
    }

    private String getData(Building building, PowerInfoType type){
        return switch(type){
            case producer -> {
                float produced = building.getPowerProduction() * building.delta() / Time.delta;
                yield "[#98ffa9]" + Strings.fixed(produced, 2) + StatUnit.powerSecond.localized() + "[]";
            }
            case consumer -> {
                var consumePower = building.block.consPower;
                float consumed = consumePower.requestedPower(building) * building.delta() / Time.delta;
                yield "[#e55454]" + Strings.fixed(consumed, 2) + StatUnit.powerSecond.localized() + "[]";
            }
            case battery -> {
                float stored = building.power.status * building.block.consPower.capacity;
                yield "[#fbad67]" + Strings.fixed(stored, 2) + " " + StatUnit.powerUnits.localized() + "[]";
            }
        };
    }

    public interface InfoToggled{
        void get(int id, boolean collapsed);
    }
}
