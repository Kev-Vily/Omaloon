package omaloon.content.blocks;

import arc.graphics.*;
import arc.math.geom.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.content.*;
import omaloon.graphics.*;
import omaloon.world.blocks.environment.*;

public class OlEnvironmentBlocks {
    public static Block
            //cliff
            cliffUp, cliffHelper,
            //ores
            oreCobalt, oreBeryllium, oreCoal,
            //liquid floors
            deepGlacium, glacium, greniteGlacium,
            //floors
            deadThickets, deadShrub,
            deadGrass,

            frozenSoilWall, frozenSoilBoulder,
            frozenSoil,

            albasterWall, albasterBoulder,
            albaster, albasterTiles, albasterCrater,

            aghatiteWall, weatheredAghaniteWall, cobaltedAghaniteWall,
            aghatiteBoulder, weatheredAghaniteBoulder, berylledAghaniteBoulder, cobaltedAghaniteStones,
            aghatite, aghatitePebbles,
            smoothAghanite, weatheredAghanite, aghaniteShale, aghaniteDust,
            coastalAghanite, crackedAghanite, berylledAghanite, cobaltedAghanite,

            quartzSandWall, quartzSandBoulder,
            quartzSand,

            greniteWall, darkGreniteWall, greniteBoulder,
            grenite, coastalGrenite,

            blueIceWall, blueSnowWall, blueBoulder,
            blueIce, blueIcePieces, blueSnow, blueSnowdrifts, weatheredIce,

            //dead tree
            fallenDeadTree, fallenDeadTreeTopHalf, fallenDeadTreeBottomHalf, standingDeadTree, deadTreeStump,

    end;

    public static void load(){
        //region cliffs
        cliffUp = new OlCliff("cliff-up");
        cliffHelper = new CliffHelper("cliff-helper");
        //endregion cliffs
        //region ores
        oreCobalt = new OreBlock("ore-cobalt", OlItems.cobalt){{
            mapColor = Color.valueOf("85939d");
            oreThreshold = 0.81f;
            oreScale = 23.47619f;
        }};

        oreBeryllium = new OreBlock("ore-beril", Items.beryllium){{
            mapColor = Color.valueOf("3a8f64");
        }};

        oreCoal = new OreBlock("ore-coal", Items.coal){{
            oreThreshold = 0.846f;
            oreScale = 24.428572f;
        }};
        //endregion
        //region liquid floor
        deepGlacium = new Floor("deep-glacium", 0){{
            speedMultiplier = 0.1f;
            liquidDrop = OlLiquids.glacium;
            liquidMultiplier = 1.3f;
            isLiquid = true;
            status = OlStatusEffects.glacied;
            statusDuration = 120f;
            drownTime = 200f;
            cacheLayer = OlShaders.dalaniLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        glacium = new Floor("shallow-glacium", 0){{
            speedMultiplier = 0.3f;
            status = OlStatusEffects.glacied;
            statusDuration = 90f;
            liquidDrop = OlLiquids.glacium;
            isLiquid = true;
            cacheLayer = OlShaders.dalaniLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        greniteGlacium = new Floor("grenite-glacium", 3){{
            speedMultiplier = 0.6f;
            status = OlStatusEffects.glacied;
            statusDuration = 60f;
            liquidDrop = OlLiquids.glacium;
            isLiquid = true;
            cacheLayer = OlShaders.dalaniLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};
        //endregion
        //region block sets
        deadGrass = new Floor("dead-grass", 4){{
            wall = deadThickets;
        }};

        deadThickets = new StaticWall("dead-thickets"){{
            variants = 2;
        }};

        deadShrub = new Prop("dead-shrub"){{
            customShadow = true;
            variants = 3;
            deadGrass.asFloor().decoration = this;
        }};

        fallenDeadTree = new CustomShapeProp("fallen-dead-tree") {{
            clipSize = 144f;
            variants = 8;
            canMirror = true;
            spriteOffsets = new Vec2[]{
                new Vec2(-16f, -32f),
                new Vec2(8f, -32f),
                new Vec2(-16, -32f),
                new Vec2(-8f, -32f),

                new Vec2(-8f, -16f),
                new Vec2(-32f, -16f),
                new Vec2(0f, -16f),
                new Vec2(-32f, -16f)
            };
        }};
        fallenDeadTreeTopHalf = new CustomShapeProp("fallen-dead-tree-top-half") {{
            clipSize = 80f;
            variants = 8;
            canMirror = true;
            spriteOffsets = new Vec2[]{
                new Vec2(-8f, -16f),
                new Vec2(-8f, -16f),
                new Vec2(-8f, -16f),
                new Vec2(0f, -16f),

                new Vec2(-16f, -8f),
                new Vec2(-16f, -8f),
                new Vec2(-8f, -8f),
                new Vec2(-16f, -8f)
            };
        }};
        fallenDeadTreeBottomHalf = new CustomShapeProp("fallen-dead-tree-bottom-half") {{
            clipSize = 64f;
            variants = 8;
            canMirror = true;
            spriteOffsets = new Vec2[]{
                new Vec2(-12f, -8f),
                new Vec2(-4f, -8f),
                new Vec2(-12f, -8f),
                new Vec2(-12f, -8f),

                new Vec2(-8f, -12f),
                new Vec2(-8f, -12f),
                new Vec2(0f, -12f),
                new Vec2(-8f, -12f)
            };
        }};
        standingDeadTree = new CustomShapeProp("standing-dead-tree") {{
            clipSize = 32f;
            variants = 1;
            spriteOffsets = new Vec2[]{
                new Vec2(-4f, -12f),
            };
        }};
        deadTreeStump = new CustomShapeProp("dead-tree-stump") {{
            clipSize = 16f;
            variants = 1;
            rotateRegions = drawUnder = true;
            spriteOffsets = new Vec2[]{
                new Vec2(-4f, -4f),
            };
        }};

        frozenSoil = new Floor("frozen-soil", 4){{
           wall = frozenSoilWall;
        }};

        frozenSoilWall = new StaticWall("frozen-soil-wall"){{
            variants = 4;
        }};

        frozenSoilBoulder = new Prop("frozen-soil-boulder"){{
            variants = 3;
            frozenSoil.asFloor().decoration = this;
        }};

        albaster = new Floor("albaster", 3){{
            wall = albasterWall;
        }};

        albasterTiles = new Floor("albaster-tiles", 3){{
            wall = albasterWall;
        }};

        albasterCrater = new Floor("albaster-craters", 3){{
            blendGroup = albaster;
            wall = albasterWall;
        }};

        albasterWall = new StaticWall("albaster-wall"){{
            variants = 3;
        }};

        albasterBoulder = new Prop("albaster-boulder"){{
            variants = 3;
            albaster.asFloor().decoration = this;
            albasterTiles.asFloor().decoration = this;
            albasterCrater.asFloor().decoration = this;
        }};

        aghatite = new Floor("aghatite", 3){{
            wall = aghatiteWall;
        }};

        aghatitePebbles = new Floor("aghatite-pebbles", 4){{
            wall = aghatiteWall;
        }};

        coastalAghanite = new Floor("coastal-aghanite",3);
        crackedAghanite = new Floor("cracked-aghanite",2);

        aghatiteWall = new StaticWall("aghatite-wall"){{
            variants = 4;
        }};

        aghatiteBoulder = new Prop("aghatite-boulder"){{
            variants = 2;
            aghatite.asFloor().decoration = this;
            aghatitePebbles.asFloor().decoration = this;
            coastalAghanite.asFloor().decoration = this;
            crackedAghanite.asFloor().decoration = this;
        }};

        smoothAghanite = new Floor("smooth-aghanite", 4);
        weatheredAghanite = new Floor("weathered-aghanite", 2);
        aghaniteShale = new Floor("aghanite-shale", 4);

        aghaniteDust = new OverlayFloor("aghanite-dust"){{
           variants = 2;
        }};

        weatheredAghaniteWall = new StaticWall("weathered-aghanite-wall"){{
           variants = 2;
        }};

        weatheredAghaniteBoulder = new Prop("weathered-aghanite-boulder"){{
            customShadow = true;
            variants = 2;
            weatheredAghanite.asFloor().decoration = this;
            smoothAghanite.asFloor().decoration = this;
            aghaniteShale.asFloor().decoration = this;
        }};

        berylledAghanite = new Floor("berylled-aghanite", 3);

        berylledAghaniteBoulder = new Prop("berylled-aghanite-boulder"){{
            customShadow = true;
            variants = 2;
            berylledAghanite.asFloor().decoration = this;
        }};

        cobaltedAghanite = new Floor("cobalted-aghanite", 2);

        cobaltedAghaniteWall = new StaticWall("cobalted-aghanite-wall"){{
           variants = 2;
        }};

        cobaltedAghaniteStones = new Prop("cobalted-aghanite-stones"){{
            customShadow = true;
            variants = 2;
            cobaltedAghanite.asFloor().decoration = this;
        }};

        quartzSand = new Floor("quartz-sand-floor", 3){{
            wall = albasterWall;
        }};

        quartzSandWall = new StaticWall("quartz-sand-wall"){{
            variants = 4;
        }};

        quartzSandBoulder = new Prop("quartz-sand-boulder"){{
            variants = 3;
            quartzSand.asFloor().decoration = this;
        }};

        grenite = new Floor("grenite", 4){{
            wall = albasterWall;
        }};

        coastalGrenite = new Floor("coastal-grenite", 3){{
            wall = albasterWall;
        }};

        greniteWall = new StaticWall("grenite-wall"){{
            variants = 2;
        }};

        darkGreniteWall = new StaticWall("dark-grenite-wall"){{
           variants = 2;
        }};

        greniteBoulder = new Prop("grenite-boulder"){{
            variants = 3;
            grenite.asFloor().decoration = this;
            coastalGrenite.asFloor().decoration = this;
        }};

        blueIce = new Floor("blue-ice", 3){{
            mapColor = Color.valueOf("5195ab");
            wall = blueIceWall;
            albedo = 0.9f;
        }};

        blueIcePieces = new OverlayFloor("blue-ice-pieces"){{
            variants = 3;
        }};
        weatheredIce = new OverlayFloor("weathered-ice"){{
            variants = 2;
        }};

        blueIceWall = new StaticWall("blue-ice-wall"){{
            mapColor = Color.valueOf("b3e7fb");
            variants = 2;
        }};

        blueSnow = new Floor("blue-snow", 3){{
            mapColor = Color.valueOf("9fd3e7");
            wall = blueIceWall;
            albedo = 0.7f;
        }};

        blueSnowdrifts = new OverlayFloor("blue-snowdrifts"){{
            variants = 3;
        }};

        blueSnowWall = new StaticWall("blue-snow-wall"){{
            mapColor = Color.valueOf("d4f2ff");
            variants = 2;
        }};

        blueBoulder = new Prop("blue-boulder"){{
           variants = 3;
           blueIce.asFloor().decoration = this;
           blueSnow.asFloor().decoration = this;
        }};
        //endregion
    }
}
