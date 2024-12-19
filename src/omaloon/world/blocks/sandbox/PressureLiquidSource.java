package omaloon.world.blocks.sandbox;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.liquid.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidSource extends Block {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion bottomRegion;

	public PressureLiquidSource(String name) {
		super(name);
		solid = true;
		destructible = true;
		update = true;
		configurable = true;
		saveConfig = copyConfig = true;

		config(SourceEntry.class, (PressureLiquidSourceBuild build, SourceEntry entry) -> {
			build.liquid = entry.fluid == null ? -1 : entry.fluid.id;
			build.targetAmount = entry.amount;
		});
	}

	@Override
	public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
		Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
		if (plan.config instanceof SourceEntry e && e.fluid != null) LiquidBlock.drawTiledFrames(size, plan.drawx(), plan.drawy(), 0f, e.fluid, 1f);
		Draw.rect(region, plan.drawx(), plan.drawy());
	}

	@Override
	public void load() {
		super.load();
		bottomRegion = Core.atlas.find(name + "-bottom");
	}

	@Override
	protected TextureRegion[] icons() {
		return new TextureRegion[]{bottomRegion, region};
	}

	@Override
	public void init() {
		super.init();

		if (pressureConfig.fluidGroup != null) pressureConfig.fluidGroup = FluidGroup.transportation;
	}

	@Override
	public void setBars() {
		super.setBars();
		pressureConfig.addBars(this);
	}

	public class PressureLiquidSourceBuild extends Building implements HasPressure {
		PressureModule pressure = new PressureModule();

		public int liquid = -1;
		public float targetAmount;

		@Override
		public void buildConfiguration(Table cont) {
			cont.table(Styles.black6, table -> {
				table.pane(Styles.smallPane, liquids -> Vars.content.liquids().each(liquid -> {
					Button button = liquids.button(
						new TextureRegionDrawable(liquid.uiIcon),
						new ImageButtonStyle() {{
							over = Styles.flatOver;
							down = checked = Tex.flatDownBase;
						}}, () -> {
							if (this.liquid != liquid.id) {
								configure(new SourceEntry() {{
									fluid = liquid;
									amount = targetAmount;
								}});
							} else {
								configure(new SourceEntry() {{
									fluid = null;
									amount = targetAmount;
								}});
							}
						}
					).tooltip(liquid.localizedName).size(40f).get();
					button.update(() -> button.setChecked(liquid.id == this.liquid));
					if ((Vars.content.liquids().indexOf(liquid) + 1) % 4 == 0) liquids.row();
        })).maxHeight(160f).row();
				table.add("@filter.option.amount").padTop(5f).padBottom(5f).row();
				table.field(
					"" + targetAmount,
					(field, c) -> Character.isDigit(c) || ((!field.getText().contains(".")) && c == '.') || (field.getText().isEmpty() && c == '-'),
					s -> configure(new SourceEntry() {{
						fluid = Vars.content.liquid(liquid);
						amount = Strings.parseFloat(s, 0f);
					}})
				);
			}).margin(5f);
		}

		@Override
		public SourceEntry config() {
			return new SourceEntry() {{
				fluid = Vars.content.liquid(liquid);
				amount = targetAmount;
			}};
		}

		@Override
		public void draw() {
			Draw.rect(bottomRegion, x, y);

			if(liquid != -1) {
				LiquidBlock.drawTiledFrames(size, x, y, 0f, Vars.content.liquid(liquid), 1f);
			}

			Draw.rect(region, x, y);
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();

			new PressureSection().mergeFlood(this);
		}

		@Override public PressureModule pressure() {
			return pressure;
		}
		@Override public PressureConfig pressureConfig() {
			return pressureConfig;
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			pressure.read(read);
			liquid = read.i();
			if (Vars.content.liquid(liquid) == null) liquid = -1;
			targetAmount = read.f();
		}

		@Override
		public void updateTile() {
			super.updateTile();

			float difference = (Vars.content.liquid(liquid) == null ? targetAmount : Mathf.maxZero(targetAmount)) - (Vars.content.liquid(liquid) == null ? pressure.air : pressure.liquids[liquid]);

			addFluid(Vars.content.liquid(liquid), difference);
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
			write.i(liquid);
			write.f(targetAmount);
		}
	}

	public static class SourceEntry {
		public @Nullable Liquid fluid;
		public float amount;
	}
}
