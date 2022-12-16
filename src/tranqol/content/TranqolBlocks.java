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

    liquidOverflowGate, liquidUnderflowGate,

    // region liquid - Erekir

    reinforcedLiquidOverflowGate, reinforcedLiquidUnderflowGate,

    // endregion
    // region power - Erekir

    beamDiode;

    // endregion

    public static void load(){
        // region liquid

        liquidOverflowGate = new LiquidOverflowGate("liquid-overflow-gate"){{
            requirements(Category.liquid, with(Items.graphite, 6, Items.metaglass, 10));
            solid = false;
            underBullets = true;
        }};

        liquidUnderflowGate = new LiquidOverflowGate("liquid-underflow-gate"){{
            requirements(Category.liquid, with(Items.graphite, 6, Items.metaglass, 10));
            solid = false;
            underBullets = true;
            invert = true;
        }};

        // endregion
        // region liquid - Erekir

        reinforcedLiquidOverflowGate = new LiquidOverflowGate("reinforced-liquid-overflow-gate"){{
            requirements(Category.liquid, with(Items.graphite, 6, Items.beryllium, 10));
            buildCostMultiplier = 3f;
            health = 260;
            researchCostMultiplier = 1;
            solid = false;
            underBullets = true;
        }};

        reinforcedLiquidUnderflowGate = new LiquidOverflowGate("reinforced-liquid-underflow-gate"){{
            requirements(Category.liquid, with(Items.graphite, 6, Items.beryllium, 10));
            buildCostMultiplier = 3f;
            health = 260;
            researchCostMultiplier = 1;
            invert = true;
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
