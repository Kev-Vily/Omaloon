package omaloon.ui.dialogs;

import arc.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ui.fragments.HintsFragment.*;
import omaloon.content.blocks.*;
import omaloon.world.interfaces.*;

public enum EventHints implements Hint {
	drill(
		() -> false,
		() -> Vars.state.teams.get(Vars.state.rules.defaultTeam).getBuildings(OlProductionBlocks.hammerDrill).find(b -> ((HasPressure) b).getPressure() < 10f) != null
	),
	pump(
		() -> false,
		() -> !Vars.state.teams.get(Vars.state.rules.defaultTeam).getBuildings(OlDistributionBlocks.liquidPump).isEmpty()
	),
	valve(
		() -> false,
		() -> !Vars.state.teams.get(Vars.state.rules.defaultTeam).getBuildings(OlDistributionBlocks.liquidPump).isEmpty(),
		pump
	);

	final Boolp complete;
	Boolp shown = () -> true;
	EventHints[] requirements;

	int visibility = visibleAll;
	boolean cached, finished;

	static final String prefix = "omaloon-";
	
	public static void addHints() {
		Vars.ui.hints.hints.add(Seq.with(EventHints.values()).removeAll(
			hint -> Core.settings.getBool(prefix + hint.name() + "-hint-done", false)
		));
	}

	EventHints(Boolp complete) {
		this.complete = complete;
	}
	EventHints(Boolp complete, Boolp shown) {
		this(complete);
		this.shown = shown;
	}
	EventHints(Boolp complete, Boolp shown, EventHints... requirements) {
		this(complete, shown);
		this.requirements = requirements;
	}

	@Override public boolean complete() {
		return complete.get();
	}

	@Override
	public void finish(){
		Core.settings.put(prefix + name() + "-hint-done", finished = true);
	}

	@Override
	public boolean finished(){
		if(!cached){
			cached = true;
			finished = Core.settings.getBool(prefix + name() + "-hint-done", false);
		}
		return finished;
	}

	@Override public boolean show(){
		return shown.get() && (requirements == null || (requirements.length == 0 || !Structs.contains(requirements, d -> !d.finished())));
	}

	@Override public int order() {
		return ordinal();
	}

	@Override public String text() {
		return Core.bundle.get("hint." + prefix + name(), "Missing bundle for hint: hint." + prefix + name());
	}

	@Override
	public boolean valid(){
		return (Vars.mobile && (visibility & visibleMobile) != 0) || (!Vars.mobile && (visibility & visibleDesktop) != 0);
	}
}
