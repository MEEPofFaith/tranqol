package tranqol.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class MultiSorter extends Block{
    protected static byte selectionDir;

    public TextureRegion topRegion, itemRegion;

    public MultiSorter(String name){
        super(name);
        update = false;
        destructible = true;
        underBullets = true;
        instantTransfer = true;
        group = BlockGroup.transportation;
        configurable = true;
        unloadable = false;
        saveConfig = true;
        clearOnDoubleTap = true;
        rotate = true;
        rotateDraw = false;

        config(int[].class, (MultiSorterBuild tile, int[] items) -> {
            tile.leftSort = content.item(items[0]);
            tile.frontSort = content.item(items[1]);
            tile.rightSort = content.item(items[2]);
            tile.outDir = 0;
        });
        configClear((MultiSorterBuild tile) -> {
            tile.leftSort = tile.frontSort = tile.rightSort = null;
            tile.outDir = 0;
        });
    }

    @Override
    public void load(){
        super.load();

        topRegion = Core.atlas.find(name + "-top");
        itemRegion = Core.atlas.find(name + "-item");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, topRegion};
    }

    @Override
    public boolean outputsItems(){
        return true;
    }

    @Override
    public boolean rotatedOutput(int x, int y){
        return false;
    }

    @Override
    public void flipRotation(BuildPlan req, boolean x){
        super.flipRotation(req, x);
        if(req.config instanceof int[] items){
            int i = items[0];
            items[0] = items[2];
            items[2] = i;
        }
    }

    @Override
    public TextureRegion getPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        return region;
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(topRegion, plan.drawx(), plan.drawy(), plan.rotation * 90f);

        if(plan.config instanceof int[] items){
            Item item = content.item(items[0]);
            if(item != null){
                Draw.color(item.color);
                Draw.rect(itemRegion, plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f);
            }
            item = content.item(items[1]);
            if(item != null){
                Draw.color(item.color);
                Draw.rect(itemRegion, plan.drawx(), plan.drawy(), (plan.rotation * 90f));
            }
            item = content.item(items[2]);
            if(item != null){
                Draw.color(item.color);
                Draw.rect(itemRegion, plan.drawx(), plan.drawy(), (plan.rotation - 1) * 90f);
            }
            Draw.color();
        }
    }

    public class MultiSorterBuild extends Building{
        public @Nullable Item leftSort, frontSort, rightSort;
        public byte outDir;

        @Override
        public void draw(){
            super.draw();

            Draw.rect(topRegion, x, y, rotdeg());

            if(leftSort != null){
                Draw.color(leftSort.color);
                Draw.rect(itemRegion, x, y, (rotation + 1) * 90f);
            }
            if(frontSort != null){
                Draw.color(frontSort.color);
                Draw.rect(itemRegion, x, y, rotdeg());
            }
            if(rightSort != null){
                Draw.color(rightSort.color);
                Draw.rect(itemRegion, x, y, (rotation - 1) * 90f);
            }
            Draw.color();
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            Building to = getTileTarget(item, source, false);
            return to != null && to.team == team && to.acceptItem(this, item);
        }
        @Override
        public void handleItem(Building source, Item item){
            getTileTarget(item, source, true).handleItem(this, item);
        }

        public Building getTileTarget(Item item, Building source, boolean flip){
            Building left = (leftSort == null && !(frontSort == item || rightSort == item)) || leftSort == item ? left() : null;
            Building front = (frontSort == null && !(leftSort == item || rightSort == item)) || frontSort == item ? front() : null;
            Building right = (rightSort == null && !(leftSort == item || frontSort == item)) || rightSort == item ? right() : null;

            boolean a = left != null && !(left.block.instantTransfer && source.block.instantTransfer) && left.acceptItem(this, item);
            boolean b = front != null && !(front.block.instantTransfer && source.block.instantTransfer) && front.acceptItem(this, item);
            boolean c = right != null && !(right.block.instantTransfer && source.block.instantTransfer) && right.acceptItem(this, item);

            if(a && !b && !c){
                return left;
            }else if(!a && b && !c){
                return front;
            }else if(!a && !b && c){
                return right;
            }else if(a && b && !c){
                return getTileTarget2(left, front, flip);
            }else if(!a && b && c){
                return getTileTarget2(front, right, flip);
            }else if(a && !b && c){
                return getTileTarget2(left, right, flip);
            }else if(a && b && c){
                return getTileTarget3(left, front, right, flip);
            }
            return null;
        }

        public Building getTileTarget2(Building a, Building b, boolean flip){
            Building out = a;
            if(outDir == 1) out = b;
            if(flip) outDir = (byte)((outDir + 1) % 2);
            return out;
        }

        public Building getTileTarget3(Building a, Building b, Building c, boolean flip){
            Building out = switch(outDir){
                default -> a;
                case 1 -> b;
                case 2 -> c;
            };
            if(flip) outDir = (byte)((outDir + 1) % 3);
            return out;
        }

        @Override
        public void buildConfiguration(Table table){
            Table selection = new Table();
            table.table(t -> {
                t.button(Icon.left, Styles.squareTogglei, () -> {
                    selectionDir = 0;
                    setSelection(selection);
                }).checked(b -> selectionDir == 0).grow();
                t.button(Icon.up, Styles.squareTogglei, () -> {
                    selectionDir = 1;
                    setSelection(selection);
                }).checked(b -> selectionDir == 1).grow();
                t.button(Icon.right, Styles.squareTogglei, () -> {
                    selectionDir = 2;
                    setSelection(selection);
                }).checked(b -> selectionDir == 2).grow();
            }).growX().height(40f);
            table.row();
            setSelection(selection);

            table.add(selection);
        }

        public void setSelection(Table table){
            table.clear();
            switch(selectionDir){
                default -> ItemSelection.buildTable(block, table, content.items(), () -> leftSort, i -> {
                    leftSort = i;
                    configure(config());
                }, false, selectionRows, selectionColumns);
                case 1 -> ItemSelection.buildTable(block, table, content.items(), () -> frontSort, i -> {
                    frontSort = i;
                    configure(config());
                }, false, selectionRows, selectionColumns);
                case 2 -> ItemSelection.buildTable(block, table, content.items(), () -> rightSort, i -> {
                    rightSort = i;
                    configure(config());
                }, false, selectionRows, selectionColumns);
            }
        }

        public int[] config(){
            return new int[]{
                leftSort == null ? -1 : leftSort.id,
                frontSort == null ? -1 : frontSort.id,
                rightSort == null ? -1 : rightSort.id
            };
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.s(leftSort == null ? -1 : leftSort.id);
            write.s(frontSort == null ? -1 : frontSort.id);
            write.s(rightSort == null ? -1 : rightSort.id);
            write.b(outDir);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            leftSort = content.item(read.s());
            frontSort = content.item(read.s());
            rightSort = content.item(read.s());
            outDir = read.b();
        }
    }
}
