package omaloon.world.interfaces;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

/**
 * @author Liz
 * only added this cause my ide did a funni
 */
public interface HasPressure extends Buildingc {
	/**
	 * @return  true if this building accepts a certain pressure amount from some building
	 */
	default boolean acceptsPressure(HasPressure from, float pressure) {
		return getPressure() + pressure <= from.getPressure() - pressure;
	}

	default boolean acceptsPressurizedFluid(HasPressure from, @Nullable Liquid liquid, float amount) {
		return pressureConfig().acceptsPressure;
	}

	/**
	 * Adds a certain amount of a fluid into this module through the section.
	 */
	default void addFluid(@Nullable Liquid liquid, float amount) {
		if (amount == 0) return;
		if (amount < 0) pressure().section.removeFluid(liquid, -amount);
		pressure().section.addFluid(liquid, amount);
	}

	/**
	 * @return true if both buildings are connected to eachother
	 */
	default boolean connected(HasPressure to) {
		return connects(to) && to.connects(this);
	}
	/**
	 * @return true if this building connects to another one.
	 */
	default boolean connects(HasPressure to) {
		return pressureConfig().outputsPressure || pressureConfig().acceptsPressure;
	}

	/**
	 * dumps pressure onto available builds
	 */
	default void dumpPressure() {
//		for (HasPressure other : nextBuilds(true)) {
//			float diff = (getPressure() - other.getPressure()) / 2f;
//			if (other.getPressure() > getPressure()) diff *= -1f;
//			if (canDumpPressure(other, diff)) {
//				transferPressure(other, diff);
//			}
//		}
	}

	/**
	 * @return current pressure of the building
	 */
	default float getPressure() {
		return pressure().pressure;
	}

	/**
	 * @return building destination to dump pressure
	 */
	default HasPressure getPressureDestination(HasPressure from, float pressure) {
		return this;
	}

	/**
	 * Returns the building whose section should be the same as this build's section.
	 */
	default @Nullable HasPressure getSectionDestination(HasPressure from) {
		if (pressureConfig().fluidGroup == null || pressureConfig().fluidGroup == FluidGroup.unset || pressureConfig().fluidGroup != from.pressureConfig().fluidGroup) return null;
		return this;
	}

	/**
	 * @param flow determines if the returned value will have only builds to which it can flow pressure to
	 */
	default Seq<HasPressure> nextBuilds(boolean flow) {
		return proximity().select(
			b -> b instanceof HasPressure
		).<HasPressure>as().map(
			b -> b.getPressureDestination(this, 0)
		).removeAll(
			b -> !connected(b) && proximity().contains((Building) b) || !pressureConfig().isAllowed(b.block())
		);
	}

	default boolean outputsPressurizedFluid(HasPressure to, @Nullable Liquid liquid, float amount) {
		return pressureConfig().outputsPressure;
	}

	PressureModule pressure();
	PressureConfig pressureConfig();

	/**
	 * Removes a certain amount of a fluid into this module through the section.
	 */
	default void removeFluid(@Nullable Liquid liquid, float amount) {
		if (amount == 0) return;
		if (amount < 0) pressure().section.addFluid(liquid, -amount);
		pressure().section.removeFluid(liquid, amount);
	}

	/**
	 * method to update pressure related things
	 */
	default void updatePressure() {
		Vars.content.liquids().each(liquid -> {
			if (Mathf.round(pressure().getPressure(liquid)) < pressureConfig().minPressure - 1) damage(pressureConfig().underPressureDamage);
			if (Mathf.round(pressure().getPressure(liquid)) > pressureConfig().maxPressure + 1) damage(pressureConfig().overPressureDamage);
		});
	}

	/**
	 * removes pressure not taking anything into account
	 */
	default @Deprecated void removePressure(float pressure) {
		pressure().pressure -= pressure;
	}
}
