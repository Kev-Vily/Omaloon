package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.draw.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static omaloon.utils.OlUtils.*;

public class TubeDistributor extends Router {
    public DrawBlock drawer = new DrawDefault();
    public TextureRegion rotorRegion;

    public TubeDistributor(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
        rotorRegion = atlas.find(name + "-rotator");
        uiIcon = atlas.find(name + "-icon");
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);
        Draw.rect(uiIcon, plan.drawx(), plan.drawy());
    }

    public class TubeDistributorBuild extends RouterBuild {
        public Item lastItem;
        public Tile lastInput;
        public int lastTargetAngle, lastSourceAngle;
        public float time, rot, angle, lastRot;

        @Override
        public void updateTile(){
            if(lastItem == null && items.any()){
                lastItem = items.first();
            }

            if(lastItem != null){
                time += 1f / speed * delta();

                Building target = getTileTarget(lastItem, lastInput, false);

                if(target == null && time >= 0.7f) {
                    rot = lastRot;
                    time = 0.7f;
                }

                if(target != null && (time >= 1f)){
                    getTileTarget(lastItem, lastInput, true);
                    target.handleItem(this, lastItem);
                    items.remove(lastItem, 1);
                    lastItem = null;
                }

                if(lastInput != null && lastItem != null){
                    int sa = sourceAngle(), ta = targetAngle();

                    angle = (sa == 0) ? (ta == 2 ? 1 : (ta == 0 || ta == 3) ? -1 : 1) :
                            (sa == 2) ? (ta == 0 || ta == 1) ? -1 : 1 :
                            (sa == 1) ? (ta == 0 || ta == 3) ? -1 : 1 :
                            (ta == 0 || ta == 1) ? 1 : -1;
                }

                if (items.total() > 0 && !Vars.state.isPaused()) {
                    lastRot = rot;
                    rot += speed * angle * delta();
                }
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return team == source.team && lastItem == null && items.total() == 0;
        }

        @Override
        public void handleItem(Building source, Item item){
            items.add(item, 1);
            lastItem = item;
            time = 0f;
            lastInput = source.tile();
        }

        @Override
        public int removeStack(Item item, int amount){
            int result = super.removeStack(item, amount);
            if(result != 0 && item == lastItem){
                lastItem = null;
            }
            return result;
        }

        public int sourceAngle() {
            for(int sourceAngle = 0; sourceAngle < 4; sourceAngle++) {
                if(nearby(sourceAngle) == lastInput.build) {
                    lastSourceAngle = sourceAngle;
                    return sourceAngle;
                }
            }
            return lastSourceAngle;
        }

        public int targetAngle() {
            Building target = getTileTarget(lastItem, lastInput, false);
            if(target != null) {
                for (int targetAngle = 0; targetAngle < 4; targetAngle++) {
                    if (nearby(targetAngle) == target) {
                        lastTargetAngle = targetAngle;
                        return targetAngle;
                    }
                }
            }
            return lastTargetAngle;
        }

        public void drawItem() {
            if (lastInput != null && lastInput.build != null && lastItem != null) {
                Building target = getTileTarget(lastItem, lastInput, false);
                boolean isf = reverse(sourceAngle()) == targetAngle() || sourceAngle() == targetAngle();
                boolean alignment = targetAngle() == 0 || targetAngle() == 2;
                float ox, oy, s = size * 4, s2 = s * 2;
                float linearMove = (float) Math.sin(Math.PI * time) / 2.4f * s;

                if (alignment) {
                    if (isf) {
                        if(sourceAngle() == targetAngle()){
                            oy = time >= 0.5f ? linearMove : -linearMove;
                            ox = time >= 0.5f ? (time * s2 - s) * (targetAngle() == 0 ? 1 : -1)
                                              : (time * s2 - s) * (targetAngle() == 0 ? -1 : 1);
                        } else {
                            oy = linearMove;
                            ox = (time * s2 - s) * (targetAngle() == 0 ? 1 : -1);
                        }
                    } else {
                        oy = sourceAngle() == 1 ? (time * -s + s) : (time * s - s);
                        ox = time * s * (targetAngle() == 0 ? 1 : -1);
                    }
                } else {
                    if (isf) {
                        if(sourceAngle() == targetAngle()){
                            ox = time >= 0.5f ? linearMove : -linearMove;
                            oy = time >= 0.5f ? (time * s2 - s) * (targetAngle() == 1 ? 1 : -1)
                                              : (time * s2 - s) * (targetAngle() == 1 ? -1 : 1);
                        } else {
                            ox = (float) Math.sin(Math.PI * time) / 2.4f * s;
                            oy = (time * s2 - s) * (targetAngle() == 1 ? 1 : -1);
                        }
                    } else {
                        ox = sourceAngle() == 0 ? (time * -s + s) : (time * s - s);
                        oy = time * s * (targetAngle() == 1 ? 1 : -1);
                    }
                }

                Draw.rect(lastItem.fullIcon, x + ox, y + oy, itemSize, itemSize);
            }
        }

        @Override
        public void draw() {
            super.draw();
            drawer.draw(this);
            Draw.z(Layer.block - 0.1f);
            drawItem();
            Draw.z(Layer.blockAdditive);
            Drawf.spinSprite(rotorRegion, x, y, rot % 360);
            Draw.rect(region, x, y);
        }

        public Building getTileTarget(Item item, Tile from, boolean set){
            int counter = rotation;
            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + counter) % proximity.size);
                if(set) rotation = ((byte)((rotation + 1) % proximity.size));
                if(other.tile == from && from.block() == Blocks.overflowGate) continue;
                if(other.acceptItem(this, item)){
                    return other;
                }
            }
            return null;
        }
    }
}