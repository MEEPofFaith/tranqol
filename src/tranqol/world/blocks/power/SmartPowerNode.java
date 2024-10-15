package tranqol.world.blocks.power;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.world.blocks.power.*;

public class SmartPowerNode extends PowerNode{
    public SmartPowerNode(String name){
        super(name);
        update = true;
    }

    public class SmartPowerNodeBuild extends PowerNodeBuild{
        public int lastID = -1;
        public Color darkColor = new Color(), lightColor = new Color();

        @Override
        public void updateTile(){
            updatePowerColor();
        }

        public void updatePowerColor(){
            int id = power.graph.getID();
            if(id != lastID){
                float hue = Mathf.randomSeed(id, 360f);
                lightColor.fromHsv(hue, 1f, 1f);
                darkColor.fromHsv(hue + 6f, 1f, 1f).mul(0.75f);
                lastID = id;
            }
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);
            drawTeamTop();

            if(!Mathf.zero(Renderer.laserOpacity) && !this.isPayload()){
                Draw.z(70.0F);
                setupColor(power.graph.getSatisfaction());

                for(int i = 0; i < this.power.links.size; ++i){
                    Building link = Vars.world.build(this.power.links.get(i));
                    if(linkValid(this, link) && (!(link.block instanceof PowerNode) || link.id < this.id)){
                        drawLaser(this.x, this.y, link.x, link.y, size, link.block.size);
                    }
                }

                Draw.reset();
            }
        }

        protected void setupColor(float satisfaction) {
            Draw.color(lightColor, darkColor, (1f - satisfaction) * 0.86f + Mathf.absin(3f, 0.1f));
            Draw.alpha(Renderer.laserOpacity);
        }
    }
}
