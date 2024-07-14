package tranqol.world.blocks.payload;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;

import static mindustry.Vars.*;

public class PayloadRail extends PayloadBlock{
    public float maxPayloadSize = 3f;
    public float railSpeed = -1f;
    public float followSpeed = 0.1f;
    public float range = 10f * tilesize;

    public PayloadRail(String name){
        super(name);
        size = 3;
        configurable = true;
        saveConfig = false;
        outputsPayload = true;
        acceptsPayload = true;
        update = true;
        rotate = true;
        solid = true;

        config(Integer.class, (PayloadRailBuild build, Integer pos) -> {
            build.items.each(RailPayload::removed);
            build.items.clear();
            build.link = pos;
            if(pos != -1) ((PayloadRailBuild)world.build(pos)).incoming = build.pos();
        });

    }

    @Override
    public void init(){
        super.init();

        if(railSpeed < 0) railSpeed = payloadSpeed;
        clipSize = Math.max(clipSize, (range + maxPayloadSize + 2) * 2f);
    }

    public boolean linkValid(Tile tile, Tile other, boolean checkLink){
        if(tile == null || other == null || !positionsValid(tile.x, tile.y, other.x, other.y)) return false;

        return tile.block() == other.block()
            && tile.within(other, range)
            && (!checkLink || ((other.build instanceof PayloadRailBuild b) && b.link == -1 && b.incoming == -1));
    }

    public boolean positionsValid(int x1, int y1, int x2, int y2){
        if(x1 == x2){
            return Math.abs(y1 - y2) <= range;
        }else if(y1 == y2){
            return Math.abs(x1 - x2) <= range;
        }else{
            return false;
        }
    }

    /** Convert hitbox side length to corner dist. */
    public static float payRadius(Payload p){
        float a = p.size() / 2f;
        return Mathf.sqrt(2 * a * a);
    }

    public class PayloadRailBuild extends PayloadBlockBuild<Payload>{
        public Seq<RailPayload> items = new Seq<>();
        public int link = -1;
        public int incoming = -1;

        @Override
        public void draw(){
            super.draw();

            if(link == -1) return;

            Draw.z(Layer.power);
            items.each(r -> r.payload.draw());

            PayloadRailBuild other = (PayloadRailBuild)world.build(link);
            Lines.line(x, y, other.x, other.y);
        }

        @Override
        public void updateTile(){
            if(link == -1){
                checkIncoming();
                moveOutPayload();
                return;
            }

            if(moveInPayload()){
                if(items.isEmpty() || dst(items.peek()) > items.peek().radius() + payRadius(payload)){
                    items.add(new RailPayload(payload, x, y));
                    payload = null;
                }
            }

            PayloadRailBuild other = (PayloadRailBuild)world.build(link);
            for(int i = 0; i < items.size; i++){
                Position target = i == 0 ? other : items.get(i - 1);
                items.get(i).update(target);
            }

            if(items.any()){
                RailPayload first = items.first();
                if(first.arrived(other) && other.acceptPayload(this, first.payload)){
                    other.handlePayload(other, first.payload);
                    items.remove(0);
                }
            }
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(linkValid(tile, other.tile, true)){
                if(link == other.pos()){
                    configure(-1);
                }else{
                    configure(other.pos());
                }
                return false;
            }
            return true;
        }

        public void checkIncoming(){
            Tile other = world.tile(incoming);
            if(!linkValid(tile, other, false) || ((PayloadRailBuild)other.build).link != pos()){
                incoming = -1;
            }
        }
    }

    public class RailPayload implements Position{
        public Payload payload;
        public float x, y;

        public RailPayload(Payload payload, float x, float y){
            this.payload = payload;
            this.x = x;
            this.y = y;
        }

        public RailPayload(){
        }

        @Override
        public float getX(){
            return x;
        }

        @Override
        public float getY(){
            return y;
        }

        public void update(Position target){
            if(target == null) return;

            Tmp.v1.set(target);
            if(target instanceof RailPayload r){
                float dst = r.radius() + radius();
                Tmp.v2.set(this);
                Tmp.v1.approach(Tmp.v2, dst);
            }

            x = Mathf.approachDelta(x, Tmp.v1.x, railSpeed);
            y = Mathf.approachDelta(y, Tmp.v1.y, railSpeed);

            payload.set(
                Mathf.lerpDelta(payload.x(), x, followSpeed),
                Mathf.lerpDelta(payload.y(), y, followSpeed),
                payload.rotation()
            );
        }

        public boolean arrived(Position target){
            return within(target, 0.001f) && payload.within(this, 0.001f);
        }

        public float radius(){
            return payRadius(payload);
        }

        public void removed(){
            payload.dump();
            payload = null;
        }
    }
}
