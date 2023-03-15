package tranqol.world.blocks.payload;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class PayloadJunction extends Block{
    public float moveTime = 45f;
    public Interp interp = Interp.pow5;
    public float payloadLimit = 3f;
    public TextureRegion topRegion, lightRegion;

    public PayloadJunction(String name){
        super(name);
        group = BlockGroup.payloads;
        size = 3;
        update = true;
        outputsPayload = true;
        noUpdateDisabled = true;
        priority = TargetPriority.transport;
        envEnabled |= Env.space | Env.underwater;
        sync = true;
    }

    @Override
    public void load(){
        super.load();
        topRegion = Core.atlas.find(name + "-top");
        lightRegion = Core.atlas.find(name + "-light");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{Core.atlas.find(name + "-icon")};
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        for(int i = 0; i < 4; i++){
            Building other = world.build(x + Geometry.d4x[i] * size, y + Geometry.d4y[i] * size);
            if(other != null && other.block.outputsPayload && other.block.size == size){
                Drawf.selected(other.tileX(), other.tileY(), other.block, other.team.color);
            }
        }
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.payloadCapacity, StatValues.squared(payloadLimit, StatUnit.blocksSquared));
    }

    @Override
    public void init(){
        super.init();

        //increase clip size for oversize loads
        clipSize = Math.max(clipSize, size * tilesize * 2.1f);
    }

    public class PayloadJunctionBuild extends Building{
        public Payload[] payloads = new Payload[2];
        public Building[] surrounding = new Building[4];
        public boolean[] blocked = new boolean[4];
        public int[] from = new int[2];
        public float[] animation = new float[2];
        public float progress, lastInterp, curInterp;
        public int step = -1, stepAccepted = -1;

        @Override
        public void updateTile(){
            if(!enabled) return;

            for(Payload p : payloads){
                if(p != null) p.update(null, this);
            }

            lastInterp = curInterp;
            curInterp = fract();
            //rollover skip
            if(lastInterp > curInterp) lastInterp = 0f;
            progress = Time.time % moveTime;

            updatePayloads();

            int curStep = curStep();
            if(curStep > step){
                boolean valid = step != -1;
                step = curStep;
                for(int i = 0; i < 2; i++){
                    Payload item = payloads[i];
                    boolean had = item != null;

                    if(valid && stepAccepted != curStep && item != null){
                        Building next = surrounding[from[i]];
                        if(next != null){
                            //trigger update forward
                            next.updateTile();

                            if(next.acceptPayload(this, item)){
                                //move forward.
                                next.handlePayload(this, item);
                                payloads[i] = null;
                                moved(i);
                            }
                        }else if(!blocked[from[i]]){
                            //dump item forward
                            if(item.dump()){
                                payloads[i] = null;
                                moved(i);
                            }
                        }
                    }

                    if(had){
                        moveFailed(i);
                    }
                }
            }
        }

        public void updatePayloads(){
            for(int i = 0; i < 2; i++){
                if(payloads[i] != null){
                    if(animation[i] > fract()){
                        animation[i] = Mathf.lerp(animation[i], 0.8f, 0.15f);
                    }

                    animation[i] = Math.max(animation[i], fract());
                    float fract = animation[i];

                    if(fract < 0.5f){
                        Tmp.v1.trns(from[i] * 90f + 180, (0.5f - fract) * tilesize * size);
                    }else{
                        Tmp.v1.trns(from[i] * 90f, (fract - 0.5f) * tilesize * size);
                    }

                    float vx = Tmp.v1.x, vy = Tmp.v1.y;
                    payloads[i].set(x + vx, y + vy, payloads[i].rotation());
                }
            }
        }

        public void moved(int pay){
        }

        public void moveFailed(int pay){
        }

        @Override
        public void draw(){
            super.draw();

            Draw.z(Layer.blockOver);
            for(Payload p : payloads){
                if(p != null) p.draw();
            }

            Draw.z(Layer.blockOver + 0.2f);
            Draw.rect(topRegion, x, y);

            if(lightRegion.found()){
                float dst = 0.8f;
                float glow = Math.max((dst - (Math.abs(fract() - 0.5f) * 2)) / dst, 0);
                Draw.mixcol(team.color, glow);
                Draw.rect(lightRegion, x, y);
            }
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            for(int i = 0; i < 4; i++){
                Building accept = nearby(Geometry.d4(i).x * (size/2+1), Geometry.d4(i).y * (size/2+1));
                //next block must be aligned and of the same size
                if(accept != null && (
                    //same size
                    (accept.block.size == size && tileX() + Geometry.d4(i).x * size == accept.tileX() && tileY() + Geometry.d4(i).y * size == accept.tileY()) ||

                        //differing sizes
                        (accept.block.size > size &&
                            (i % 2 == 0 ? //check orientation
                                Math.abs(accept.y - y) <= (accept.block.size * tilesize - size * tilesize)/2f : //check Y alignment
                                Math.abs(accept.x - x) <= (accept.block.size * tilesize - size * tilesize)/2f   //check X alignment
                            )))){
                    surrounding[i] = accept;
                }else{
                    surrounding[i] = null;
                }

                int ntrns = 1 + size/2;
                Tile next = tile.nearby(Geometry.d4(rotation).x * ntrns, Geometry.d4(rotation).y * ntrns);
                blocked[i] = (next != null && next.solid() && !(next.block().outputsPayload || next.block().acceptsPayload)) || (surrounding[i] != null && surrounding[i].payloadCheck(rotation));
            }
        }

        @Override
        public void payloadDraw(){
            Draw.rect(block.fullIcon, x, y);
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            int relative = source.relativeTo(tile);
            return source != this && payloads[relative % 2] == null && payload.fits(payloadLimit) && enabled && progress <= 5;
        }

        @Override
        public void handlePayload(Building source, Payload payload){
            int relative = source.relativeTo(tile);
            if(payloads[relative % 2] != null) return;

            payloads[relative % 2] = payload;
            animation[relative % 2] = 0;
            from[relative % 2] = relative;

            updatePayloads();
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            for(Payload p : payloads){
                if(p != null) p.dump();
            }
        }

        public float fract(){
            return interp.apply(progress / moveTime);
        }

        public int curStep(){
            return (int)(Time.time / moveTime);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            for(int i = 0; i < 2; i++){
                write.i(from[i]);
                Payload.write(payloads[i], write);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            for(int i = 0; i < 2; i++){
                from[i] = read.i();
                payloads[i] = Payload.read(read);
            }
        }
    }
}
