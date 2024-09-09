package omaloon.content;

import arc.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import omaloon.content.blocks.*;
import omaloon.graphics.g3d.*;
import omaloon.maps.ColorPass.*;
import omaloon.maps.*;
import omaloon.maps.HeightPass.MultiHeight.*;
import omaloon.maps.planets.*;
import omaloon.type.*;

import static arc.Core.*;
import static arc.graphics.Color.*;
import static mindustry.Vars.*;

public class OlPlanets {
	public static Planet omaloon, glasmore;

	public static void load() {
		//TODO remove in release
		//PlanetDialog.debugSelect = true;

		omaloon = new Planet("omaloon", null, 4f, 0) {{
			bloom = true;
			accessible = false;
			hasAtmosphere = true;
			solarSystem = this;

			meshLoader = () -> new SunMesh(
					this, 4, 5, 0.3f, 1.0f, 1.2f, 1, 1.3f,

					valueOf("#8B4513"),
					valueOf("#A0522D"),
					valueOf("c2311e"),
					valueOf("ff6730"),
					valueOf("bf342f"),
					valueOf("8e261d")
			);
		}};

		glasmore = new BetterPlanet("glasmore", omaloon, 1f, 4){{
			icon = "glasmore";
			solarSystem = omaloon;
			alwaysUnlocked = true;
			orbitRadius = 40f;
			atmosphereRadIn = 0f;
			atmosphereRadOut = 0.3f;
			atmosphereColor = OlEnvironmentBlocks.glacium.mapColor;

			itemWhitelist.addAll(
				OlItems.cobalt,
				OlItems.carborundum,
				Items.beryllium
			);

			Events.on(EventType.ContentInitEvent.class, e -> {
				if(itemWhitelist.size > 0){
					hiddenItems.addAll(content.items().select(i -> !itemWhitelist.contains(i)));
				}
			});

			Vec3 ringPos = new Vec3(0,1,0).rotate(Vec3.X, 25);

			generator = new GlasmorePlanetGenerator() {{
				baseHeight = 0;
				baseColor = OlEnvironmentBlocks.albaster.mapColor;

				heights.add(new HeightPass.NoiseHeight() {{
					offset.set(1000, 0, 0);
					octaves = 7;
					persistence = 0.5;
					magnitude = 1;
					heightOffset = -0.5f;
				}});

				Mathf.rand.setSeed(2);
				Seq<HeightPass> mountains = new Seq<>();
				for (int i = 0; i < 20; i++) {
					mountains.add(new HeightPass.DotHeight() {{
						dir.setToRandomDirection().y = Mathf.random(2f, 5f);
						min = 0.99f;
						magnitude = Math.max(0.7f, dir.nor().y) * 0.3f;
						interp = Interp.exp10In;
					}});
				}
				heights.add(new HeightPass.MultiHeight(mountains, MixType.max, Operation.add));

				mountains = new Seq<>();
				for (int i = 0; i < 20; i++) {
					mountains.add(new HeightPass.DotHeight() {{
						dir.setToRandomDirection().y = Mathf.random(-2f, -5f);
						min = 0.99f;
						magnitude = Math.max(0.7f, dir.nor().y) * 0.3f;
						dir.rotate(Vec3.X, 22f);
						interp = Interp.exp10In;
					}});
				}
				heights.add(new HeightPass.MultiHeight(mountains, MixType.max, Operation.add));

				Seq<HeightPass> craters = new Seq<>();
				Mathf.rand.setSeed(3);
				for(int i = 0; i < 5; i++) {
					craters.add(new HeightPass.SphereHeight() {{
						pos.set(Vec3.Y).rotate(Vec3.X, 115f).rotate(ringPos, Mathf.random(360f));
						radius = 0.14f + Mathf.random(0.05f);
						offset = 0.2f;
						set = true;
					}});
				}
				heights.addAll(new HeightPass.MultiHeight(craters, MixType.max, Operation.set));
				Mathf.rand.setSeed(3);
				for(int i = 0; i < 5; i++) {
					heights.add(new HeightPass.SphereHeight() {{
						pos.set(Vec3.Y).rotate(Vec3.X, 115f).rotate(ringPos, Mathf.random(360f));
						radius = 0.07f + Mathf.random(0.05f);
						set = true;
					}});
				}
				heights.add(new HeightPass.ClampHeight(0f, 0.8f));

				colors.addAll(
					new NoiseColorPass() {{
						scale = 1.5;
						persistence = 0.5;
						octaves = 3;
						magnitude = 1.2f;
						min = 0.3f;
						max = 0.6f;
						out = OlEnvironmentBlocks.deadGrass.mapColor;
						offset.set(1500f, 300f, -500f);
					}},
					new NoiseColorPass() {{
						seed = 5;
						scale = 1.5;
						persistence = 0.5;
						octaves = 5;
						magnitude = 1.2f;
						min = 0.1f;
						max = 0.4f;
						out = OlEnvironmentBlocks.aghatite.mapColor;
						offset.set(1500f, 300f, -500f);
					}},
					new NoiseColorPass() {{
						seed = 8;
						scale = 1.5;
						persistence = 0.5;
						octaves = 7;
						magnitude = 1.2f;
						min = 0.1f;
						max = 0.4f;
						out = OlEnvironmentBlocks.quartzSand.mapColor;
						offset.set(1500f, 300f, -500f);
					}}
				);
				for(int i = 0; i < 5; i++) {
					colors.add(new SphereColorPass(new Vec3().setToRandomDirection(), 0.06f, OlEnvironmentBlocks.grenite.mapColor));
				}
				colors.add(
					new FlatColorPass() {{
						min = max = 0f;
						out = OlEnvironmentBlocks.blueIce.mapColor;
					}},
					new FlatColorPass() {{
						min = 0.3f;
						max = 0.5f;
						out = OlEnvironmentBlocks.deadGrass.mapColor;
					}},
					new FlatColorPass() {{
						max = 1f;
						min = 0.5f;
						out = OlEnvironmentBlocks.blueIce.mapColor;
					}}
				);
				craters.map(height -> (HeightPass.SphereHeight) height).each(height -> colors.add(
					new SphereColorPass(height.pos, height.radius/1.75f, OlEnvironmentBlocks.glacium.mapColor)
				));
				defaultLoadout = Schematics.readBase64("bXNjaAF4nGNgZmBmZmDJS8xNZeB0zi9KVXDLyU9l4E5JLU4uyiwoyczPY2BgYMtJTErNKWZgio5lZBDMz03Myc/P000GKtdNAylnYGAEISABADBoE3w=");
			}};

			meshLoader = () -> new MultiMesh(
					new AtmosphereHexMesh(7),
					new HexMesh(this, 7),

					new CircleMesh(atlas.find("omaloon-ring4"), this, 80, 2.55f, 2.6f, ringPos),
					new CircleMesh(atlas.find("omaloon-ring3"), this,80, 2.2f, 2.5f, ringPos),
					new CircleMesh(atlas.find("omaloon-ring2"), this,80, 1.9f, 2.1f, ringPos),
					new CircleMesh(atlas.find("omaloon-ring1"), this,80, 1.8f, 1.85f, ringPos)
			);

			cloudMeshLoader = () -> new MultiMesh(
					new HexSkyMesh(this, 6, -0.5f, 0.14f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.2f), 2, 0.42f, 1f, 0.6f),
					new HexSkyMesh(this, 1, 0.6f, 0.15f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.2f), 2, 0.42f, 1.2f, 0.5f)
			);
		}};
	}
}
