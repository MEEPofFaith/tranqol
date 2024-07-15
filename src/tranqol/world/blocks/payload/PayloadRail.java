package tranqol.world.blocks.payload;

import arc.graphics.*;
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
    protected static float zeroPrecision = 0.1f;

    public float maxPayloadSize = 3f;
    public float railSpeed = -1f;
    public float followSpeed = 0.1f;
    public float bufferDst = 1f;
    public float range = 10f * tilesize;

    public PayloadRail(String name){
        super(name);
        size = 3;
        configurable = true;
        copyConfig = false;
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
            && (!checkLink || ((other.build instanceof PayloadRailBuild b) && b.link == -1 && b.incoming == -1));
    }

    public boolean positionsValid(int x1, int y1, int x2, int y2){
        return Mathf.dst(x1, y1, x2, y2) <= range;
    }

    @Override
    public void drawOverlay(float x, float y, int rotation){
        Lines.stroke(1f);
        Draw.color(Pal.accent);
        Drawf.circles(x, y, range);
        Draw.reset();
    }

    /** Convert hitbox side length to corner dist. */
    public static float payRadius(Payload p){
        float a = p.size() / 2f;
        return Mathf.sqrt(2 * a * a);
    }

    public class PayloadRailBuild extends PayloadBlockBuild<Payload>{
        public Seq<RailPayload> items = new Seq<>(); //TODO read/write
        public int link = -1;
        public int incoming = -1;

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            boolean fallback = true;
            for(int i = 0; i < 4; ++i) {
                if (blends(i) && i != rotation) {
                    Draw.rect(inRegion, x, y, (float)(i * 90 - 180));
                    fallback = false;
                }
            }

            if (fallback) {
                Draw.rect(inRegion, x, y, (float)(rotation * 90));
            }

            Draw.rect(outRegion, x, y, rotdeg());
            Draw.rect(topRegion, x, y);
            Draw.z(35f);
            drawPayload();

            if(link == -1) return;

            Draw.z(Layer.power);
            items.each(RailPayload::draw);

            PayloadRailBuild other = (PayloadRailBuild)world.build(link);
            Lines.stroke(2, Color.red);
            Lines.line(x, y, other.x, other.y);
        }

        @Override
        public void updateTile(){
            if(link == -1){
                checkIncoming();
                moveOutPayload();
                return;
            }

            PayloadRailBuild other = (PayloadRailBuild)world.build(link);
            if(other == null){
                configure(-1);
                return;
            }

            if(moveInPayload()){
                if(items.isEmpty() || dst(items.peek()) > items.peek().radius() + payRadius(payload) + bufferDst){
                    items.add(new RailPayload(payload, x, y));
                    payload = null;
                }
            }

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
            Tmp.v2.set(this);
            if(target instanceof RailPayload r){
                float dst = r.radius() + radius() + bufferDst;
                Tmp.v1.approach(Tmp.v2, dst);
            }

            Tmp.v2.approachDelta(Tmp.v1, railSpeed);
            x = Tmp.v2.x;
            y = Tmp.v2.y;

            payload.set(
                Mathf.lerpDelta(payload.x(), x, followSpeed),
                Mathf.lerpDelta(payload.y(), y, followSpeed),
                payload.rotation()
            );
        }

        public void draw(){
            //temp
            payload.draw();
            Lines.stroke(1f, Color.white);
            Lines.line(payload.x(), payload.y(), x, y);
            Draw.color(Pal.accent);
            Fill.circle(x, y, 2f);
            Draw.color();
        }

        public boolean arrived(Position target){
            return within(target, zeroPrecision) && payload.within(this, zeroPrecision);
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
