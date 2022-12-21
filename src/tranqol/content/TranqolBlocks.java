package tranqol.content;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import tranqol.world.blocks.liquid.*;
import tranqol.world.blocks.power.*;

import static mindustry.type.ItemStack.*;

public class TranqolBlocks{
    public static Block

    // region liquid

    liquidOverflowValve, liquidUnderflowValve, liquidUnloader,

    // region liquid - Erekir

    reinforcedLiquidOverflowValve, reinforcedLiquidUnderflowValve, reinforcedLiquidUnloader,

    // endregion
    // region power - Erekir

    beamDiode;

    // endregion

    public static void load(){
        // region liquid

        liquidOverflowValve = new LiquidOverflowValve("liquid-overflow-valve"){{
            requirements(Category.liquid, with(Items.graphite, 6, Items.metaglass, 10));
            solid = false;
            underBullets = true;
        }};

        liquidUnderflowValve = new LiquidOverflowValve("liquid-underflow-valve"){{
            requirements(Category.liquid, with(Items.graphite, 6, Items.metaglass, 10));
            solid = false;
            underBullets = true;
            invert = true;
        }};

        liquidUnloader = new LiquidUnloader("liquid-unloader"){{
            requirements(Category.liquid, with(Items.titanium, 15, Items.metaglass, 10));
            health = 70;
            hideDetails = false;
        }};

        // endregion
        // region liquid - Erekir

        reinforcedLiquidOverflowValve = new LiquidOverflowValve("reinforced-liquid-overflow-valve"){{
            requirements(Category.liquid, with(Items.graphite, 6, Items.beryllium, 10));
            buildCostMultiplier = 3f;
            health = 260;
            researchCostMultiplier = 1;
            solid = false;
            underBullets = true;
        }};

        reinforcedLiquidUnderflowValve = new LiquidOverflowValve("reinforced-liquid-underflow-valve"){{
            requirements(Category.liquid, with(Items.graphite, 6, Items.beryllium, 10));
            buildCostMultiplier = 3f;
            health = 260;
            researchCostMultiplier = 1;
            invert = true;
            solid = false;
            underBullets = true;
        }};

        reinforcedLiquidUnloader = new DirectionalLiquidUnloader("reinforced-liquid-unloader"){{
            requirements(Category.liquid, with(Items.tungsten, 10, Items.beryllium, 15));
            buildCostMultiplier = 3f;
            health = 570;
            researchCostMultiplier = 1;
            solid = false;
            underBullets = true;
        }};

        // endregion
        // region power -

        beamDiode = new BeamDiode("beam-diode"){{
            requirements(Category.power, with(
                Items.beryllium, 10,
                Items.silicon, 10,
                Items.surgeAlloy, 5
            ));
            health = 90;
            range = 10;
            fogRadius = 1;
        }};

        //endregion
    }
}
