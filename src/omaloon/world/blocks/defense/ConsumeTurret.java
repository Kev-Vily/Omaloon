package omaloon.world.blocks.defense;

import arc.struct.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static mindustry.Vars.*;

public class ConsumeTurret extends Turret {
	public PressureConfig pressureConfig = new PressureConfig();

	/**
	 * if true, min range this turret can only target
	 */
	public boolean minRangeShoot = true;

	public BulletType shootType = Bullets.placeholder;

	public ConsumeTurret(String name) {
		super(name);
	}

	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		super.drawPlace(x, y, rotation, valid);

		if (!minRangeShoot) Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, minRange, Pal.placing);
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);
	}

	@Override
	public void setBars() {
		super.setBars();
		pressureConfig.addBars(this);
		stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(this, shootType)));
	}

	public class ConsumeTurretBuild extends TurretBuild implements HasPressure {
		public PressureModule pressure = new PressureModule();

		@Override
		public void drawSelect() {
			super.drawSelect();

			if (!minRangeShoot) Drawf.dashCircle(x, y, minRange, team.color);
		}

		@Override
		protected void findTarget() {
			super.findTarget();
			if (target != null && dst(target) < minRange && !minRangeShoot) target = null;
		}

		@Override public boolean hasAmmo() {
			return canConsume();
		}

		@Override public BulletType peekAmmo() {
			return shootType;
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
		protected void shoot(BulletType type) {
			super.shoot(type);
			consume();
		}

		@Override
		public void updateTile() {
			updatePressure();
			dumpPressure();
			super.updateTile();
		}

		@Override public BulletType useAmmo() {
			return shootType;
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
		}
	}
}
