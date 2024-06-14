package tranqol.world.blocks.hybrid;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class ItemLiquidJunction extends LiquidBlock{
    public float speed = 26; //frames taken for item go through this junction
    public int capacity = 6;

    protected TextureRegion[][] directionRegions;

    public ItemLiquidJunction(String name){
        super(name);

        rotate = true;
        rotateDraw = false;
        drawArrow = false;
        unloadable = false;
        floating = true;
        noUpdateDisabled = true;
    }

    @Override
    public void load(){
        super.load();

        directionRegions = new TextureRegion[2][2];
        for(int j = 0; j <= 1; j++){
            directionRegions[j][0] = Core.atlas.find(name + "-item" + j, name + "-item");
            directionRegions[j][1] = Core.atlas.find(name + "-liquid" + j, name + "-liquid");
        }
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);

        for(int i = 0; i < 4; i++){
            Draw.rect(directionRegions[i > 1 ? 1 : 0][(plan.rotation + i) % 2], plan.drawx(), plan.drawy(), i * 90f);
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
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

    @Override
    public boolean outputsItems(){
        return true;
    }

    public class ItemLiquidJunctionBuild extends LiquidBuild{
        public DirectionalItemBuffer buffer = new DirectionalItemBuffer(capacity);

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            for(int i = 0; i < 4; i++){
                Draw.rect(directionRegions[i > 1 ? 1 : 0][(rotation + i) % 2], x, y, i * 90f);
            }
        }

        @Override
        public Building getLiquidDestination(Building source, Liquid liquid){
            if(!enabled || relativeSelf(source) % 2 == 0) return this;

            int relative = source.relativeTo(tile.x, tile.y);
            int dir = (relative + 4) % 4;
            Building next = nearby(dir);
            if(next == null || (!next.acceptLiquid(this, liquid) && !(next.block instanceof LiquidJunction))){
                return this;
            }
            return next.getLiquidDestination(this, liquid);
        }

        @Override
        public void updateTile(){
            for(int i = 0; i < 2; i++){ //Stolen from Junction and modified.
                int ii = rotation % 2 + i * 2;
                if(buffer.indexes[ii] > 0){
                    if(buffer.indexes[ii] > capacity) buffer.indexes[ii] = capacity;
                    long l = buffer.buffers[ii][0];
                    float time = BufferItem.time(l);

                    if(Time.time >= time + speed / timeScale || Time.time < time){
                        Item item = content.item(BufferItem.item(l));
                        Building dest = nearby(ii);

                        //skip blocks that don't want the item, keep waiting until they do
                        if(item == null || dest == null || !dest.acceptItem(this, item) || dest.team != team){
                            continue;
                        }

                        dest.handleItem(this, item);
                        System.arraycopy(buffer.buffers[ii], 1, buffer.buffers[ii], 0, buffer.indexes[ii] - 1);
                        buffer.indexes[ii]--;
                    }
                }
            }
        }

        @Override
        public void handleItem(Building source, Item item){
            int relative = source.relativeTo(tile);
            buffer.accept(relative, item);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            int relative = source.relativeTo(tile);

            if(relative == -1 || !buffer.accepts(relative) || relativeSelf(source) % 2 == 1) return false;
            Building to = nearby(relative);
            return to != null && to.team == team;
        }

        /** Relativity with rotation */
        public int relativeSelf(Building source){
            return Mathf.mod(source.relativeTo(tile) - rotation, 4);
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            return 0;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            buffer.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            buffer.read(read);
        }
    }
}
