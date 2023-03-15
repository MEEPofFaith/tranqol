package tranqol.content;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import tranqol.world.blocks.distribution.*;
import tranqol.world.blocks.liquid.*;
import tranqol.world.blocks.payload.*;
import tranqol.world.blocks.power.*;

import static mindustry.type.ItemStack.*;

public class TranqolBlocks{
    public static Block

    // region distribution - Erekir

    ductJunction,

    // region liquid - Serpulo

    liquidOverflowValve, liquidUnderflowValve, liquidUnloader,

    // region liquid - Erekir

    reinforcedLiquidOverflowValve, reinforcedLiquidUnderflowValve, reinforcedLiquidUnloader,

    // endregion
    // region payload - Serpulo

    payloadJunction,

    // endregion
    // region payload - Erekir

    reinforcedPayloadJunction,

    // region power - Erekir

    beamDiode;

    // endregion

    public static void load(){
        // region distribution - Erekir

        ductJunction = new DuctJunction("duct-junction"){{
            requirements(Category.distribution, with(Items.beryllium, 2));
            health = 75;
            speed = 4f;
        }};

        // region liquid - Serpulo

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
        // region payload - Serpulo

        payloadJunction = new PayloadJunction("payload-junction"){{
            requirements(Category.units, with(Items.graphite, 10, Items.copper, 15, Items.phaseFabric, 5));
            canOverdrive = false;
            payloadLimit = 2f;
        }};

        // endregion
        // region payload - Erekir

        reinforcedPayloadJunction = new PayloadJunction("reinforced-payload-junction"){{
            requirements(Category.units, with(Items.tungsten, 10, Items.beryllium, 5, Items.phaseFabric, 5));
            moveTime = 35f;
            canOverdrive = false;
            payloadLimit = 2f;
            health = 800;
            researchCostMultiplier = 4f;
            underBullets = true;
        }};

        // endregion
        // region power - Erekir

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
