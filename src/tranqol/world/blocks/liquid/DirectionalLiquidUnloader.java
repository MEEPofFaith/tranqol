package tranqol.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.liquid.*;

import static mindustry.Vars.*;

public class DirectionalLiquidUnloader extends LiquidBlock{
    public TextureRegion centerRegion, topRegion, arrowRegion;

    public DirectionalLiquidUnloader(String name){
        super(name);

        rotate = true;
        configurable = true;
        saveConfig = true;
        liquidCapacity = 0;
        noUpdateDisabled = true;
        clearOnDoubleTap = true;
        unloadable = false;

        config(Liquid.class, (DirectionalLiquidUnloaderBuild tile, Liquid liquid) -> tile.unloadLiquid = liquid);
        configClear((DirectionalLiquidUnloaderBuild tile) -> tile.unloadLiquid = null);
    }

    @Override
    public void load(){
        super.load();

        centerRegion = Core.atlas.find(name + "-center", "tranqol-reinforced-liquid-unloader-center");
        topRegion = Core.atlas.find(name + "-top");
        arrowRegion = Core.atlas.find(name + "-arrow");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, topRegion, arrowRegion};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        drawPlanConfig(plan, list);
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        drawPlanConfigCenter(plan, plan.config, "tranqol-reinforced-liquid-unloader-center");
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("liquid");
    }

    public class DirectionalLiquidUnloaderBuild extends Building{
        public Liquid unloadLiquid = null;

        @Override
        public void updateTile(){
            Building front = front(), back = back();

            if(front != null && back != null && front.team == team && back.team == team && front.liquids != null && back.liquids != null && back.canUnload() && unloadLiquid != null){
                float ofract = front.liquids.get(unloadLiquid) / front.block.liquidCapacity;
                float fract = back.liquids.get(unloadLiquid) / back.block.liquidCapacity * back.block.liquidPressure;
                float flow = Math.min(Mathf.clamp(fract - ofract) * back.block.liquidCapacity, back.liquids.get(unloadLiquid));
                flow = Math.min(flow, front.block.liquidCapacity - front.liquids.get(unloadLiquid));

                front.handleLiquid(this, unloadLiquid, flow);
                back.liquids.remove(unloadLiquid, flow);
            }
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            Draw.rect(topRegion, x, y, rotdeg());

            if(unloadLiquid != null){
                Draw.color(unloadLiquid.color);
                Draw.rect(centerRegion, x, y);
                Draw.color();
            }else{
                Draw.rect(arrowRegion, x, y, rotdeg());
            }
        }

        @Override
        public void buildConfiguration(Table table){
            ItemSelection.buildTable(DirectionalLiquidUnloader.this, table, content.liquids(), () -> unloadLiquid, this::configure, selectionRows, selectionColumns);
        }

        @Override
        public Liquid config(){
            return unloadLiquid;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.s(unloadLiquid == null ? -1 : unloadLiquid.id);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            int id = read.s();
            unloadLiquid = id == -1 ? null : content.liquid(id);
        }
    }
}
