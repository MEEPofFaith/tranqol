package tranqol.content;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import tranqol.world.blocks.defense.*;
import tranqol.world.blocks.distribution.*;
import tranqol.world.blocks.hybrid.*;
import tranqol.world.blocks.liquid.*;
import tranqol.world.blocks.payload.*;
import tranqol.world.blocks.power.*;

import static mindustry.type.ItemStack.*;

public class TQBlocks{
    public static Block

    // region distribution - Serpulo

    multiSorter,

    // endregion
    // region distribution - Erekir

    ductJunction, ductMultiSorter,

    // endregion
    // region liquid - Serpulo

    liquidOverflowValve, liquidUnderflowValve, liquidUnloader,

    // endregion
    // region liquid - Erekir

    reinforcedLiquidOverflowValve, reinforcedLiquidUnderflowValve, reinforcedLiquidUnloader,

    // endregion
    // region hybrid - Serpulo

    itemLiquidJunction,

    // endregion
    // region payload - Serpulo

    payloadJunction, payloadRail,

    // endregion
    // region payload - Erekir

    reinforcedPayloadJunction, reinforcedPayloadRail,

    // endregion
    // region power - Serpulo

    smartPowerNode, powerAnalyzer,

    // region power - Erekir

    smartBeamNode, beamInsulator, beamDiode, reinforcedPowerAnalyzer;

    // endregion

    public static void load(){
        // region distribution - Serpulo

        multiSorter = new MultiSorter("multi-sorter"){{
            requirements(Category.distribution, with(Items.lead, 5, Items.copper, 5, Items.silicon, 5));
        }};

        // endregion
        // region distribution - Erekir

        ductJunction = new DuctJunction("duct-junction"){{
            requirements(Category.distribution, with(Items.beryllium, 2));
            health = 90;
            speed = 4f;
        }};

        ductMultiSorter = new MultiSorter("duct-multi-sorter"){{
            requirements(Category.distribution, with(Items.beryllium, 5, Items.silicon, 5));
            health = 90;
        }};

        // endregion
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

        itemLiquidJunction = new ItemLiquidJunction("item-liquid-junction"){{
            requirements(Category.distribution, with(Items.copper, 4, Items.graphite, 6, Items.metaglass, 10));
        }};

        // region payload - Serpulo

        payloadJunction = new PayloadJunction("payload-junction"){{
            requirements(Category.units, with(Items.graphite, 15, Items.copper, 20));
            hideDetails = false;
        }};

        payloadRail = new PayloadRail("payload-rail"){{
            requirements(Category.units, with(Items.graphite, 45, Items.titanium, 35, Items.silicon, 20));
        }};

        // endregion
        // region payload - Erekir

        reinforcedPayloadJunction = new PayloadJunction("reinforced-payload-junction"){{
            requirements(Category.units, with(Items.tungsten, 15, Items.beryllium, 10));
            moveTime = 35f;
            health = 800;
            researchCostMultiplier = 4f;
            underBullets = true;
            hideDetails = false;
        }};

        reinforcedPayloadRail = new PayloadRail("reinforced-payload-rail"){{
            requirements(Category.units, with(Items.tungsten, 55, Items.silicon, 25, Items.oxide, 10));
        }};

        // endregion
        // region power - Serpulo

        smartPowerNode = new SmartPowerNode("smart-power-node"){{ //Copy stats from normal power node
            requirements(Category.power, with(Items.copper, 2, Items.lead, 5, Items.silicon, 1));
            maxNodes = 10;
            laserRange = 6;
        }};

        powerAnalyzer = new PowerAnalyzer("power-analyzer"){{
            requirements(Category.power, with(Items.lead, 60, Items.silicon, 20, Items.metaglass, 10));
            size = 2;
            displayThickness = 9f / 4f;
            displaySpacing = 18f / 4f;
            displayLength = 24f / 4f;
        }};

        // endregion
        // region power - Erekir

        smartBeamNode = new SmartBeamNode("smart-beam-node"){{ //Copy stats from normal beam node
            requirements(Category.power, with(Items.beryllium, 10, Items.silicon, 2));
            consumesPower = outputsPower = true;
            health = 90;
            range = 10;
            fogRadius = 1;

            consumePowerBuffered(1000f);
        }};

        beamInsulator = new InsulationWall("beam-insulator"){{
            requirements(Category.power, with(
                Items.silicon, 10,
                Items.oxide, 5
            ));
            health = 90;
        }};

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

        reinforcedPowerAnalyzer = new PowerAnalyzer("reinforced-power-analyzer"){{
            requirements(Category.power, with(Items.beryllium, 25, Items.silicon, 15));
            size = 2;
            displayThickness = 9f / 4f;
            displaySpacing = 18f / 4f;
            displayLength = 24f / 4f;
            horizontal = true; //Why not
            hideDetails = false;
        }};

        //endregion
    }
}
