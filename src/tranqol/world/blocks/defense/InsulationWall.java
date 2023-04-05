package tranqol.world.blocks.defense;

import arc.*;
import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.world.blocks.defense.*;

import static mindustry.Vars.*;

public class InsulationWall extends Wall{
    public final int timerToggle = timers++;
    public Effect openfx = Fx.dooropen;
    public Effect closefx = Fx.doorclose;
    public Sound doorSound = Sounds.door;
    public TextureRegion openRegion;

    public InsulationWall(String name){
        super(name);
        consumesTap = true;

        config(Boolean.class, (InsulationWallBuild base, Boolean open) -> {
            doorSound.at(base);
            base.effect();
            base.open = open;
            world.tileChanges++;
        });
    }

    @Override
    public void load(){
        super.load();

        openRegion = Core.atlas.find(name + "-open");
    }

    @Override
    public TextureRegion getPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        return plan.config == Boolean.TRUE ? openRegion : region;
    }

    public class InsulationWallBuild extends WallBuild{
        public boolean open = false;

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.enabled) return open ? 1 : 0;
            return super.sense(sensor);
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.enabled){
                boolean shouldOpen = !Mathf.zero(p1);

                if(net.client() || open == shouldOpen || timer(timerToggle, 60f)){
                    return;
                }

                configureAny(shouldOpen);
            }
        }

        public void effect(){
            (open ? closefx : openfx).at(this, size);
        }

        @Override
        public void draw(){
            Draw.rect(open ? openRegion : region, x, y);
        }

        @Override
        public Cursor getCursor(){
            return interactable(player.team()) ? SystemCursor.hand : SystemCursor.arrow;
        }

        @Override
        public boolean isInsulated(){
            return !open;
        }

        @Override
        public void tapped(){
            if(!timer(timerToggle, 60f)) return;

            configure(!open);
        }

        @Override
        public Boolean config(){
            return open;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(open);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            open = read.bool();
        }
    }
}
