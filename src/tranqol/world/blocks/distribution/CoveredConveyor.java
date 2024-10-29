package tranqol.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.distribution.*;
import tranqol.util.*;

public class CoveredConveyor extends Conveyor{
    public TextureRegion[] coverRegions = new TextureRegion[5];
    public TextureRegion inputRegion, outputRegion;

    public CoveredConveyor(String name){
        super(name);
    }

    @Override
    public void init(){
        super.init();
    }

    @Override
    public void load(){
        super.load();
        for(int i = 0; i < 5; i++){
            coverRegions[i] = Core.atlas.find(name + "-cover-" + i);
        }
        inputRegion = Core.atlas.find(name + "-cover-in");
        outputRegion = Core.atlas.find(name + "-cover-out");
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){
        super.drawPlanRegion(req, list);

        int[] bits = getTiling(req, list);

        if(bits == null) return;

        if(!req.tile().floor().isDeep()) return;

        TextureRegion region = coverRegions[bits[0]];
        Draw.rect(region, req.drawx(), req.drawy(), region.width * bits[1] * Draw.scl, region.height * bits[2] * Draw.scl, req.rotation * 90);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{regions[0][0], coverRegions[0]};
    }

    public class FloatingConveyorBuild extends ConveyorBuild{
        public boolean backCap, leftCap, rightCap, frontCap;

        @Override
        public void draw(){
            super.draw();

            Draw.z(Layer.block - 0.08f);

            if(tile().floor().isDeep()){
                Draw.rect(coverRegions[blendbits], x, y, Vars.tilesize * blendsclx, Vars.tilesize * blendscly, rotation * 90);
            }

            if(frontCap) Draw.rect(outputRegion, x, y, rotdeg());
            if(backCap) Draw.rect(inputRegion, x, y, rotdeg());
            if(leftCap) Draw.rect(inputRegion, x, y, rotdeg() - 90f);
            if(rightCap) Draw.rect(inputRegion, x, y, rotdeg() + 90f);
        }

        @Override
        public void unitOn(Unit unit){
            //There is a cover, can't slide on this thing
            if(!tile().floor().isDeep()){
                super.unitOn(unit);
            }
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            frontCap = tile().floor().isDeep() && (nextc == null || !nextc.tile().floor().isDeep());

            Building backB = back();
            backCap = !(blendbits == 1 || blendbits == 4) && tile().floor().isDeep()
                && (backB == null || TQUtls.relativeDirection(backB, this) == 0 && backB.block == block && !backB.tile().floor().isDeep());

            Building leftB = left();
            leftCap = blendbits != 0 && TQUtls.relativeDirection(leftB, this) == 0 && leftB.block != block && tile().floor().isDeep() && leftB.tile().floor().isDeep();

            Building rightB = right();
            rightCap = blendbits != 0 && TQUtls.relativeDirection(rightB, this) == 0 && rightB.block != block && tile().floor().isDeep() && rightB.tile().floor().isDeep();
        }
    }
}
