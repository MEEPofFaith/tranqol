package tranqol.world.blocks.payload;

import arc.*;
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
import tranqol.graphics.*;

import static mindustry.Vars.*;

public class PayloadRail extends PayloadBlock{
    protected static float zeroPrecision = 0.5f;

    public float maxPayloadSize = 3f;
    public float railSpeed = -1f;
    public float followSpeed = 0.1f;
    public float bufferDst = 1f;
    public float range = 10f * tilesize;
    public float arrivedRadius = 4f;
    public float clawWarmupRate = 0.08f;
    public float clawRotSpeed = 10f;

    protected TextureRegion railEndRegion;
    protected TextureRegion[] railRegions, clawRegions;

    public PayloadRail(String name){
        super(name);
        size = 3;
        configurable = true;
        outputsPayload = true;
        acceptsPayload = true;
        update = true;
        rotate = true;
        solid = true;
        canOverdrive = false;

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
        return new TextureRegion[]{region, inRegion, outRegion, topRegion, railEndRegion};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(inRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(outRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
        Draw.rect(railEndRegion, plan.drawx(), plan.drawy());
    }

    @Override
    public void load(){
        super.load();
        railEndRegion = Core.atlas.find(name + "-rail-end");

        railRegions = new TextureRegion[3];
        clawRegions = new TextureRegion[3];
        for(int i = 0; i < 3; i++){
            railRegions[i] = Core.atlas.find(name + "-rail-" + i);
            clawRegions[i] = Core.atlas.find(name + "-claw-" + i);
        }
    }

    public class PayloadRailBuild extends PayloadBlockBuild<Payload>{
        public Seq<RailPayload> items = new Seq<>();
        public int link = -1;
        public int incoming = -1;
        public float clawAlpha;
        public Vec2 clawVec = new Vec2();
        public float clawRot;

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            if(incoming == -1){
                boolean fallback = true;
                for(int i = 0; i < 4; ++i){
                    if(blends(i)){
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

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);
            Draw.z(Layer.power + 0.1f);
            Draw.rect(railEndRegion, x, y);
            Draw.z(Layer.power + 0.2f);
            TQDrawf.spinSprite(clawRegions, x + clawVec.x, y + clawVec.y, clawRot, clawAlpha);
            Draw.color();
            drawPayload();

            if(link == -1) return;
            Building other = world.build(link);
            if(!(other instanceof PayloadRailBuild)) return;

            items.each(p -> {
                Draw.z(Layer.power - 1);
                p.draw();
            });

            Draw.z(Layer.power);
            float texW = railRegions[0].width / 4f;
            int count = Mathf.round(dst(other) / texW);
            float width = dst(other) / (count * texW);
            float ang = angleTo(other);
            float dx = (other.x - x) / count;
            float dy = (other.y - y) / count;
            for(int i = 0; i < count; i++){
                float j = (i + 0.5f);
                TQDrawf.spinSprite(railRegions, x + dx * j, y + dy * j, texW * width, railRegions[0].height / 4f, ang);
            }
        }

        @Override
        public void drawPayload(){
            if(payload != null){
                updatePayload();

                Draw.z((incoming != -1 && !checkLink()) ? Layer.power - 1 : Layer.blockOver);
                payload.draw();
            }
        }

        @Override
        public void updateTile(){
            if(incoming != -1){
                checkIncoming();
            }

            if(incoming != -1){
                clawAlpha = Mathf.approachDelta(clawAlpha, 0, clawWarmupRate);
            }
            if(link != -1){
                clawAlpha = Mathf.approachDelta(clawAlpha, 1, clawWarmupRate);
            }
            clawVec.approach(Vec2.ZERO, railSpeed * delta());

            if(link == -1){
                moveOutPayload();
                return;
            }

            if(checkLink()){
                items.each(RailPayload::removed);
                items.clear();
                moveOutPayload();
                return;
            }

            if(moveInPayload(true)){
                if(items.isEmpty() || dst(items.peek()) > items.peek().radius() + payRadius(payload) + bufferDst){
                    items.add(new RailPayload(payload, x, y));
                    payload = null;
                    clawAlpha = 0f;
                }
            }

            updateRail();
        }

        public void updateRail(){
            PayloadRailBuild other = (PayloadRailBuild)world.build(link);

            clawRot = Angles.moveToward(clawRot, angleTo(other), clawRotSpeed);

            for(int i = 0; i < items.size; i++){
                Position target = i == 0 ? other : items.get(i - 1);
                items.get(i).update(target);
            }

            if(items.any()){
                RailPayload first = items.first();
                if(first.payArrived(other) && other.acceptPayload(this, first.payload)){
                    other.handlePayload(this, first.payload);
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
            if(incoming != -1){
                this.payload = payload;
                payVector.set(payload).sub(this);
                payRotation = payload.rotation();
                updatePayload();

                clawVec.set(payload).sub(this);
                clawRot = source.angleTo(this);
                clawAlpha = 1f;
            }else{
                super.handlePayload(source, payload);
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

        /** @return true if link invalid */
        public boolean checkLink(){
            if(link == -1) return true;
            Building other = world.build(link);
            if(!(other instanceof PayloadRailBuild build)){
                return true;
            }
            if(build.incoming == -1){
                build.incoming = pos();
            }
            return build.incoming != pos();
        }

        public void checkIncoming(){
            Building other = world.build(incoming);
            if(!(other instanceof PayloadRailBuild build) || build.link != pos()){
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
        public float dir;

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

            dir = payload.angleTo(target);

            payload.set(
                Mathf.lerpDelta(payload.x(), x, followSpeed),
                Mathf.lerpDelta(payload.y(), y, followSpeed),
                Angles.moveToward(payload.rotation(), dir, payloadRotateSpeed * Time.delta)
            );
        }

        public void draw(){
            payload.draw();
            Draw.z(Layer.power + 0.2f);
            TQDrawf.spinSprite(clawRegions, payload.x(), payload.y(), dir);
        }

        public boolean railArrived(Position target){
            return Mathf.within(target.getX(), target.getY(), x, y, zeroPrecision);
        }

        public boolean payArrived(Position target){
            return Mathf.within(target.getX(), target.getY(), payload.x(), payload.y(), arrivedRadius);
        }

        public boolean clawArrived(Position target){
            return Mathf.within(target.getX(), target.getY(), payload.x(), payload.y(), 0.5f);
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
