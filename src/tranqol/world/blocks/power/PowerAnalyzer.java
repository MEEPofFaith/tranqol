package tranqol.world.blocks.power;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

public class PowerAnalyzer extends PowerBlock{
    public float displayLength = 8f;
    public float displaySpacing = 4f;
    public float displayThickness = 2f;
    public boolean horizontal = false;

    public TextureRegion topRegion;

    public PowerAnalyzer(String name){
        super(name);
        //update = false; //Does not need to update, probably
    }

    @Override
    public void load(){
        super.load();

        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, topRegion};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
    }

    public class PowerAnalyzerBlock extends Building{
        @Override
        public void draw(){
            Draw.rect(region, x, y);

            float produced = power.graph.getLastScaledPowerIn() * 60f, //per tick -> per sec
                consumed = power.graph.getLastScaledPowerOut() * 60f,
                stored = power.graph.getLastPowerStored(),
                cap = power.graph.getLastCapacity();

            Lines.stroke(displayThickness);
            if(produced + consumed > 0.001f) drawUsage(produced, consumed);
            drawStorage(stored, cap, produced - consumed);
            Draw.color();

            Draw.rect(topRegion, x, y);
        }

        public void drawUsage(float produced, float consumed){
            float total = produced + consumed;
            if(horizontal){
                Draw.color(Pal.heal);
                Lines.lineAngle(x - displayLength / 2f, y - displaySpacing / 2f, 0, displayLength * (produced / total), false);
                Draw.color(Pal.remove);
                Lines.lineAngle(x + displayLength / 2f, y - displaySpacing / 2f, 180f, displayLength * (consumed / total), false);
            }else{
                Draw.color(Pal.heal);
                Lines.lineAngle(x - displaySpacing / 2f, y - displayLength / 2f, 90f, displayLength * (produced / total), false);
                Draw.color(Pal.remove);
                Lines.lineAngle(x - displaySpacing / 2f, y + displayLength / 2f, -90f, displayLength * (consumed / total), false);
            }
        }

        public void drawStorage(float stored, float capacity, float net){
            Draw.color(Pal.power);
            float powLen = displayLength * (stored / capacity);
            float alpha = Mathf.absin(25f / Mathf.PI2, 1f);
            if(horizontal){
                Lines.lineAngle(x - displayLength / 2f, y + displaySpacing / 2f, 0f, powLen, false);
                Draw.color(net < 0 ? Pal.remove : Pal.heal, alpha);
                float netLen = Math.min(displayLength - powLen, displayLength * (net / capacity));
                netLen = Math.max(-powLen, netLen);
                Lines.lineAngle(x - displayLength / 2f + powLen, y + displaySpacing / 2f, 0f, netLen, false);
            }else{
                Lines.lineAngle(x + displaySpacing / 2f, y - displayLength / 2f, 90f, powLen, false);
                Draw.color(net < 0 ? Pal.remove : Pal.heal, alpha);
                float netLen = Math.min(displayLength - powLen, displayLength * (net / capacity));
                netLen = Math.max(-powLen, netLen);
                Lines.lineAngle(x + displaySpacing / 2f, y - displayLength / 2f + powLen, 90f, netLen, false);
            }
        }

        @Override
        public BlockStatus status(){
            return super.status();
        }
    }
}
