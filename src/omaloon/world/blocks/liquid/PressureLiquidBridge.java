package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.meta.BlockGroup;
import omaloon.world.blocks.distribution.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static arc.Core.*;
import static arc.graphics.g2d.Draw.*;
import static arc.util.Tmp.*;
import static mindustry.Vars.*;

public class PressureLiquidBridge extends TubeItemBridge {
	public PressureConfig pressureConfig = new PressureConfig();

	public float liquidPadding = 1f;

	public TextureRegion
		bottomRegion, endRegion1,
		endBottomRegion, endLiquidRegion,
		bridgeBottomRegion, bridgeLiquidRegion;

	public PressureLiquidBridge(String name) {
		super(name);
		hasLiquids = true;
		hasItems = false;
		outputsLiquid = true;
		canOverdrive = false;
		group = BlockGroup.liquids;
	}

	@Override
	public void drawBridge(BuildPlan req, float ox, float oy, float flip) {
		drawBridge(bridgeBottomRegion, endBottomRegion, new Vec2(req.drawx(), req.drawy()), new Vec2(ox, oy));
		drawBridge(new Vec2(req.drawx(), req.drawy()), new Vec2(ox, oy));
	}

	public void drawBridge(Vec2 pos1, Vec2 pos2){
		boolean line = pos1.x == pos2.x || pos1.y == pos2.y;

		int segments = length(pos1.x, pos1.y, pos2.x, pos2.y) + 1;
		float sl = 0;
		if(!line){
			sl = Mathf.dst(pos1.x, pos1.y, pos2.x, pos2.y) / segments;
		}
		float sa = pos1.angleTo(pos2);
		float oa = pos2.angleTo(pos1);

		if(line){
			if(pos1.y == pos2.y){
				Position a = pos1.x < pos2.x ? pos2 : pos1;
				Position b = pos1.x < pos2.x ? pos1 : pos2;

				segments = (int)(a.getX() / 8 - b.getX() / 8);
			}

			if(pos1.x == pos2.x){
				Position a = pos1.y < pos2.y ? pos2 : pos1;
				Position b = pos1.y < pos2.y ? pos1 : pos2;

				segments = (int)(a.getY() / 8 - b.getY() / 8);
			}
		}

		boolean reverse = pos1.x > pos2.x;

		if(line){
			reverse |= pos1.y < pos2.y;
		}

		float r = sa + (reverse ? 180 : 0);

		TextureRegion end = reverse ? endRegion1 : endRegion;
		TextureRegion str = reverse ? endRegion : endRegion1;

		Draw.rect(end, pos1.x, pos1.y, sa);
		Draw.rect(str, pos2.x, pos2.y, oa);

		for(int i = 1; i < segments; i++){
			float s_x = Mathf.lerp(pos1.x, pos2.x, (float)i / segments);
			float s_y = Mathf.lerp(pos1.y, pos2.y, (float)i / segments);

			if(line){
				Draw.rect(bridgeRegion, s_x, s_y, r);
			}else{
				Draw.rect(bridgeRegion, s_x, s_y, sl, bridgeRegion.height * scl * xscl, r);
			}
		}
	}

	public int length(float x1, float y1, float x2, float y2){
		return (int)(Mathf.dst(x1, y1, x2, y2) / tilesize);
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
		Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
		super.drawPlanRegion(plan, list);
	}

	@Override
	public TextureRegion[] icons(){
		return new TextureRegion[]{atlas.find(name + "-icon")};
	}

	@Override
	public void load() {
		super.load();
		bottomRegion = Core.atlas.find("omaloon-liquid-bottom");
		endRegion1 = Core.atlas.find(name + "-end1");
		endBottomRegion = Core.atlas.find(name + "-end-bottom");
		endLiquidRegion = Core.atlas.find(name + "-end-liquid");
		bridgeBottomRegion = Core.atlas.find(name + "-bridge-bottom");
		bridgeLiquidRegion = Core.atlas.find(name + "-bridge-liquid");
	}

	@Override
	public void setBars() {
		super.setBars();
		pressureConfig.addBars(this);
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);
	}

	public class PressureLiquidBridgeBuild extends TubeItemBridgeBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		@Override
		public boolean acceptLiquid(Building source, Liquid liquid) {
			return source.block.hasLiquids;
		}

		@Override
		public boolean canDumpLiquid(Building to, Liquid liquid) {
			return super.canDumpLiquid(to, liquid) || to instanceof LiquidVoid.LiquidVoidBuild;
		}

		@Override
		public void draw() {
			Draw.rect(bottomRegion, x, y);

			if(liquids.currentAmount() > 0.001f){
				LiquidBlock.drawTiledFrames(size, x, y, liquidPadding, liquids.current(), liquids.currentAmount() / liquidCapacity);
			}

			drawBase();

			Draw.z(Layer.power);
			Tile other = Vars.world.tile(link);
			Building build = Vars.world.build(link);
			if(build == this) build = null;
			if(build != null) other = build.tile;
			if(!linkValid(this.tile, other) || build == null || Mathf.zero(Renderer.bridgeOpacity)) return;
			Vec2 pos1 = new Vec2(x, y), pos2 = new Vec2(other.drawx(), other.drawy());

			if(pulse) Draw.color(Color.white, Color.black, Mathf.absin(Time.time, 6f, 0.07f));

			Draw.alpha(Renderer.bridgeOpacity);
			drawBridge(bridgeBottomRegion, endBottomRegion, pos1, pos2);
			Draw.color(liquids.current().color, liquids.currentAmount()/liquidCapacity * liquids.current().color.a * Renderer.bridgeOpacity);
			drawBridge(bridgeLiquidRegion, endLiquidRegion, pos1, pos2);
			Draw.color();
			Draw.alpha(Renderer.bridgeOpacity);
			drawBridge(pos1, pos2);

			Draw.reset();
		}

		@Override
		public Seq<HasPressure> nextBuilds(boolean flow) {
			Seq<HasPressure> o = HasPressure.super.nextBuilds(flow);
			if (Vars.world.build(link) instanceof PressureLiquidBridgeBuild b) o.add(b);
			for(int pos : incoming.items) if (Vars.world.build(pos) instanceof PressureLiquidBridgeBuild b) o.add(b);
			return o;
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
		}

		@Override
		public void updateTile() {
			incoming.size = Math.min(incoming.size, maxConnections - (link == -1 ? 0 : 1));
			incoming.shrink();

			checkIncoming();

			nextBuilds(true).each(b -> moveLiquidPressure(b, liquids.current()));
			updatePressure();
			dumpPressure();

			Tile other = world.tile(link);
			if(linkValid(tile, other)) {
				if(other.build instanceof TubeItemBridgeBuild && cast(other.build).acceptIncoming(this.tile.pos())){
					configureAny(-1);
					return;
				}

				IntSeq inc = ((ItemBridgeBuild) other.build).incoming;
				int pos = tile.pos();
				if(!inc.contains(pos)){
					inc.add(pos);
				}

				warmup = Mathf.approachDelta(warmup, efficiency(), 1f / 30f);
			}
		}

		@Override
		protected void drawInput(Tile other){
			if(linkValid(this.tile, other, false)){
				final float angle = tile.angleTo(other);
				v2.trns(angle, 2.0F);
				float tx = tile.drawx();
				float ty = tile.drawy();
				float ox = other.drawx();
				float oy = other.drawy();
				Draw.color(Pal.gray);
				Lines.stroke(2.5F);
				Lines.square(ox, oy, 2.0F, 45.0F);
				Lines.square(tx, ty, 2.0F, 45.0F);
				Lines.stroke(2.5F);
				Lines.line(tx + v2.x, ty + v2.y, ox - v2.x, oy - v2.y);
				Draw.color(Pal.place);
				Lines.stroke(1.0F);
				Lines.line(tx + v2.x, ty + v2.y, ox - v2.x, oy - v2.y);
				Lines.square(ox, oy, 2.0F, 45.0F);
				Lines.square(tx, ty, 2.0F, 45.0F);
				Draw.mixcol(Draw.getColor(), 1.0F);
				Draw.color();
				Draw.mixcol();
			}
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
		}

		@Override
		public void read(Reads read){
			super.read(read);
			pressure.read(read);
		}
	}
}
