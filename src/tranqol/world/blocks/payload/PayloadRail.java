package tranqol.world.blocks.payload;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;

import static mindustry.Vars.*;

public class PayloadRail extends PayloadBlock{
    protected static float zeroPrecision = 0.5f;

    public float maxPayloadSize = 3f;
    public float railSpeed = -1f;
    public float followSpeed = 0.1f;
    public float bufferDst = 1f;
    public float range = 10f * tilesize;

    public PayloadRail(String name){
        super(name);
        size = 3;
        configurable = true;
        outputsPayload = true;
        acceptsPayload = true;
        update = true;
        rotate = true;
        solid = true;

        //Point2 is relative
        config(Point2.class, (PayloadRailBuild build, Point2 point) -> {
            build.link = Point2.pack(point.x + build.tileX(), point.y + build.tileY());
        });
        config(Integer.class, (PayloadRailBuild build, Integer pos) -> {
            build.items.each(RailPayload::removed);
            build.items.clear();
            build.link = pos;
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
            && (!checkLink || ((other.build instanceof PayloadRailBuild b) && b.incoming == -1));
    }

    public boolean positionsValid(int x1, int y1, int x2, int y2){
        return Mathf.dst(x1, y1, x2, y2) <= range / tilesize;
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

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, inRegion, outRegion, topRegion};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(inRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(outRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
    }

    public class PayloadRailBuild extends PayloadBlockBuild<Payload>{
        public Seq<RailPayload> items = new Seq<>();
        public int link = -1;
        public int incoming = -1;

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            if(incoming == -1){
                boolean fallback = true;
                for(int i = 0; i < 4; ++i){
                    if(blends(i) && i != rotation){
                        Draw.rect(inRegion, x, y, (float)(i * 90 - 180));
                        fallback = false;
                    }
                }

                if(fallback){
                    Draw.rect(inRegion, x, y, (float)(rotation * 90));
                }
            }

            if(link == -1){
                Draw.rect(outRegion, x, y, rotdeg());
            }

            Draw.rect(topRegion, x, y);
            Draw.z(35f);
            drawPayload();

            if(link == -1) return;

            Draw.z(Layer.power);
            items.each(RailPayload::draw);

            PayloadRailBuild other = (PayloadRailBuild)world.build(link);
            if(other == null) return;
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

            if(checkLink()){
                items.each(RailPayload::removed);
                items.clear();
                link = -1;
                moveOutPayload();
                return;
            }

            PayloadRailBuild other = (PayloadRailBuild)world.build(link);

            if(moveInPayload(true)){
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
                if(first.payArrived(other) && other.acceptPayload(this, first.payload)){
                    other.handlePayload(other, first.payload);
                    items.remove(0);
                }
            }
        }

        @Override
        public void remove(){
            super.remove();
            items.each(RailPayload::removed);
            items.clear();
        }

        @Override
        public boolean moveInPayload(boolean rotate){
            if(payload == null) return false;

            updatePayload();

            if(rotate){
                PayloadRailBuild other = (PayloadRailBuild)world.build(link);
                float rotTarget =
                    other != null ? angleTo(other) :
                        block.rotate ? rotdeg() :
                            90f;
                payRotation = Angles.moveToward(payRotation, rotTarget, payloadRotateSpeed * delta());
            }
            payVector.approach(Vec2.ZERO, payloadSpeed * delta());

            return hasArrived();
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return super.acceptPayload(source, payload) && (incoming == -1 || source == world.build(incoming));
        }

        @Override
        public void handlePayload(Building source, Payload payload){
            super.handlePayload(source, payload);
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

        /** @return true if link invalid */
        public boolean checkLink(){
            if(link == -1) return true;
            PayloadRailBuild other = (PayloadRailBuild)world.build(link);
            if(other == null){
                return true;
            }
            if(other.incoming == -1){
                other.incoming = tile.pos();
            }
            return other.incoming != tile.pos();
        }

        public void checkIncoming(){
            Tile other = world.tile(incoming);
            if(!linkValid(tile, other, false) || ((PayloadRailBuild)other.build).link != pos()){
                incoming = -1;
            }
        }

        @Override
        public Point2 config(){
            if(tile == null) return null;
            return Point2.unpack(link).sub(tile.x, tile.y);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(link);
            write.i(incoming);
            write.i(items.size);

            for(RailPayload p : items){
                p.write(write);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            link = read.i();
            incoming = read.i();

            int amount = read.i();
            for(int i = 0; i < amount; i++){
                RailPayload p = new RailPayload();
                p.read(read);
                items.add(p);
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
                Angles.moveToward(payload.rotation(), payload.angleTo(target), payloadRotateSpeed * Time.delta)
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

        public boolean railArrived(Position target){
            return Mathf.zero(target.getX() - x, zeroPrecision)
                && Mathf.zero(target.getY() - y, zeroPrecision);
        }

        public boolean payArrived(Position target){
            return Mathf.zero(target.getX() - payload.x(), zeroPrecision)
                && Mathf.zero(target.getY() - payload.y(), zeroPrecision);
        }

        public float radius(){
            return payRadius(payload);
        }

        public void removed(){
            payload.dump();
            payload = null;
        }

        public void write(Writes write){
            write.f(x);
            write.f(y);
            Payload.write(payload, write);
            write.f(payload.x());
            write.f(payload.y());
            write.f(payload.rotation());
        }

        public void read(Reads read){
            x = read.f();
            y = read.f();
            payload = Payload.read(read);
            payload.set(read.f(), read.f(), read.f());
        }
    }
}
