package tranqol.world.blocks.liquid;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.pooling.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class LiquidUnloader extends LiquidBlock{
    public TextureRegion centerRegion;

    public LiquidUnloader(String name){
        super(name);

        configurable = true;
        saveConfig = true;
        liquidCapacity = 0;
        noUpdateDisabled = true;
        clearOnDoubleTap = true;
        unloadable = false;

        config(Liquid.class, (LiquidUnloaderBuild tile, Liquid liquid) -> tile.unloadLiquid = liquid);
        configClear((LiquidUnloaderBuild tile) -> tile.unloadLiquid = null);
    }

    @Override
    public void load(){
        super.load();

        centerRegion = Core.atlas.find(name + "-center", "tranqol-liquid-unloader-center");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        drawPlanConfigCenter(plan, plan.config, "tranqol-liquid-unloader-center");
    }
    
    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.liquidCapacity);
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("liquid");
    }

    public static class LiquidContainerStat{
        Building building;
        float loadFactor;
        boolean canLoad;
        boolean canUnload;
        int lastUsed;

        @Override
        public String toString(){
            return "ContainerStat{" +
                "building=" + building.block + "#" + building.id +
                ", loadFactor=" + loadFactor +
                ", canLoad=" + canLoad +
                ", canUnload=" + canUnload +
                ", lastUsed=" + lastUsed +
                '}';
        }
    }

    public class LiquidUnloaderBuild extends Building{
        public Liquid unloadLiquid = null;
        public LiquidContainerStat dumpingFrom, dumpingTo;
        public final Seq<LiquidContainerStat> possibleBlocks = new Seq<>();

        protected final Comparator<LiquidContainerStat> comparator = (x, y) -> {
            //sort so it gives priority for blocks that can only either receive or give (not both), and then by load, and then by last use
            //highest = unload from, lowest = unload to
            int unloadPriority = Boolean.compare(x.canUnload && !x.canLoad, y.canUnload && !y.canLoad); //priority to receive if it cannot give
            if(unloadPriority != 0) return unloadPriority;
            int loadPriority = Boolean.compare(x.canUnload || !x.canLoad, y.canUnload || !y.canLoad); //priority to give if it cannot receive
            if(loadPriority != 0) return loadPriority;
            int loadFactor = Float.compare(x.loadFactor, y.loadFactor);
            if(loadFactor != 0) return loadFactor;
            return Integer.compare(y.lastUsed, x.lastUsed); //inverted
        };

        private boolean isPossibleLiquid(Liquid liquid){
            boolean hasProvider = false,
                hasReceiver = false,
                isDistinct = false;

            for(int i = 0; i < possibleBlocks.size; i++){
                var pb = possibleBlocks.get(i);
                var other = pb.building;

                //set the stats of buildings in possibleBlocks while we are at it
                pb.canLoad = !(other.block instanceof LiquidRouter) && other.acceptLiquid(this, liquid);
                pb.canUnload = other.canUnload() && other.liquids != null && other.liquids.get(liquid) > 0.01f;

                //thats also handling framerate issues and slow conveyor belts, to avoid skipping items if nulloader
                if((hasProvider && pb.canLoad) || (hasReceiver && pb.canUnload)) isDistinct = true;
                hasProvider |= pb.canUnload;
                hasReceiver |= pb.canLoad;
            }
            return isDistinct;
        }

        @Override
        public void onProximityUpdate(){
            //filter all blocks in the proximity that will never be able to trade liquids

            super.onProximityUpdate();
            Pools.freeAll(possibleBlocks, true);
            possibleBlocks.clear();

            for(int i = 0; i < proximity.size; i++){
                var other = proximity.get(i);
                if(!other.interactable(team)) continue; //avoid blocks of the wrong team
                LiquidContainerStat pb = Pools.obtain(LiquidContainerStat.class, LiquidContainerStat::new);

                //partial check
                boolean canLoad = !(other.block instanceof LiquidRouter);
                boolean canUnload = other.canUnload() && other.liquids != null;

                if(canLoad || canUnload){ //avoid blocks that can neither give nor receive items
                    pb.building = other;
                    //TODO store the partial canLoad/canUnload?
                    possibleBlocks.add(pb);
                }
            }
        }

        @Override
        public void updateTile(){
            if(possibleBlocks.size < 2) return;

            //Only extract 1 liquid at a time. You never liquids in pipes anyways.
            if(unloadLiquid != null && isPossibleLiquid(unloadLiquid)){
                for(int i = 0; i < possibleBlocks.size; i++){
                    var pb = possibleBlocks.get(i);
                    var other = pb.building;
                    pb.loadFactor = (other.block.liquidCapacity == 0) || (other.liquids == null) ? 0 : other.liquids.get(unloadLiquid) / other.block.liquidCapacity;
                    pb.lastUsed = (pb.lastUsed + 1) % Integer.MAX_VALUE; //increment the priority if not used
                }

                possibleBlocks.sort(comparator);

                dumpingTo = null;
                dumpingFrom = null;

                //choose the building to accept the item
                for(int i = 0; i < possibleBlocks.size; i++){
                    if(possibleBlocks.get(i).canLoad){
                        dumpingTo = possibleBlocks.get(i);
                        break;
                    }
                }

                //choose the building to take the item from
                for(int i = possibleBlocks.size - 1; i >= 0; i--){
                    if(possibleBlocks.get(i).canUnload){
                        dumpingFrom = possibleBlocks.get(i);
                        break;
                    }
                }

                //trade the liquids
                if(dumpingFrom != null && dumpingTo != null && (dumpingFrom.loadFactor != dumpingTo.loadFactor || !dumpingFrom.canLoad)){
                    float ofract = dumpingTo.building.liquids.get(unloadLiquid) / dumpingTo.building.block.liquidCapacity;
                    float fract = dumpingFrom.building.liquids.get(unloadLiquid) / dumpingFrom.building.block.liquidCapacity * dumpingFrom.building.block.liquidPressure;
                    float flow = Math.min(Mathf.clamp(fract - ofract) * dumpingFrom.building.block.liquidCapacity, dumpingFrom.building.liquids.get(unloadLiquid));
                    flow = Math.min(flow, dumpingTo.building.block.liquidCapacity - dumpingTo.building.liquids.get(unloadLiquid));

                    dumpingTo.building.handleLiquid(this, unloadLiquid, flow);
                    dumpingFrom.building.liquids.remove(unloadLiquid, flow);
                    dumpingTo.lastUsed = 0;
                    dumpingFrom.lastUsed = 0;
                }
            }
        }

        @Override
        public void draw(){
            super.draw();

            Draw.color(unloadLiquid == null ? Color.clear : unloadLiquid.color);
            Draw.rect(centerRegion, x, y);
            Draw.color();
        }

        @Override
        public void buildConfiguration(Table table){
            ItemSelection.buildTable(LiquidUnloader.this, table, content.liquids(), () -> unloadLiquid, this::configure, selectionRows, selectionColumns);
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
