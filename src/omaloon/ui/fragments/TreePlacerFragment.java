package omaloon.ui.fragments;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.*;
import omaloon.ui.*;
import omaloon.world.blocks.environment.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static omaloon.OmaloonMod.*;

public class TreePlacerFragment extends Table{
    private Table indicator;
    private boolean selecting = false;
    private final Color col2 = Color.valueOf("75edff");
    private Block currentTree;
    private int currentShape = 1;
    private final Vec2 lastMousePosition = new Vec2();

    private final int[] group1 = {1, 5, 3, 7}; // ↑1, →1, ↓1, ←1
    private final int[] group2 = {2, 6, 4, 8}; // ↑2, →2, ↓2, ←2

    public TreePlacerFragment() {
        setFillParent(true);
        visible(() -> ui.hudfrag.shown && OmaloonMod.editorListener.isEditor());
        touchable(() -> selecting && visible ? Touchable.enabled : Touchable.disabled);
        update(() -> {
            if (selecting) {
                scene.setKeyboardFocus(this);
                scene.setScrollFocus(this);
            } else {
                scene.setKeyboardFocus(null);
                scene.setScrollFocus(null);
            }
        });

        bottom();
        table(Styles.black5, t1 -> {
            indicator = t1;
            t1.margin(10f);
            t1.table(t2 -> {
                t2.image(Icon.treeSmall).size(15f).center().padRight(15f).color(col2);
                t2.label(() -> bundle.get("fragment.omaloon.shaped-env-placer")).grow().center().get().setAlignment(Align.center);
                t2.image(Icon.treeSmall).size(15f).center().padLeft(15f).color(col2);
            }).growX();
            t1.row();

            t1.pane(Styles.smallPane, selector -> content.blocks().each(block -> block instanceof CustomShapeProp, block -> {
                Button b = selector.button(
                  button -> button.add(new Image(block.uiIcon).setScaling(Scaling.fit)).size(32),
                  new Button.ButtonStyle() {{
                      up = Tex.windowEmpty;
                      down = Tex.windowEmpty;
                      checked = Tex.buttonSelect;
                  }},
                  () -> {
                      currentTree = block;
                      currentShape = 1;
                  }
                ).size(50f).tooltip(block.localizedName).get();
                b.update(() -> b.setChecked(currentTree == block));
            })).size(300f, 50f).padTop(5f);
            t1.row();

            t1.button(
              b -> b.add("@place"),
              new Button.ButtonStyle() {{
                  up = Tex.windowEmpty;
                  down = Tex.windowEmpty;
                  over = Tex.buttonSelect;
              }},
              () -> {
                  if (selecting && visible) {
                      placeTree();
                  }
              }
            ).size(120f, 40f).pad(5f);
            t1.setTransform(true);
        }).fill().bottom();

        Events.on(WorldLoadEvent.class, e -> {
            selecting = false;
            hideUI();
        });
        Events.run(Trigger.draw, TreePlacerFragment::drawTreePreview);
    }

    public void build(Group parent) {
        indicator.actions(Actions.alpha(0));

        parent.addChildAt(0, this);

        if (!mobile) {
            scene.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, KeyCode keycode) {
                    if (input.keyTap(OlBinding.shaped_env_placer) && visible) {
                        toggle();
                        return true;
                    }
                    if (selecting && visible && currentTree != null && ((CustomShapeProp) currentTree).canMirror) {
                        if (input.keyTap(Binding.schematic_flip_x)) {
                            mirrorHorizontally();
                            return true;
                        }
                        if (input.keyTap(Binding.schematic_flip_y)) {
                            mirrorVertically();
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                    if (selecting && visible) {
                        changeShape((int) Math.signum(-amountY));
                        return true;
                    }
                    return false;
                }
            });
        }

        Core.scene.root.addListener(new ElementGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, KeyCode button) {
                if (selecting && visible && isOverIndicator(x, y)) {
                    updateMousePosition(x, y);
                }
            }
        });
    }

    private boolean isOverIndicator(float x, float y) {
        if (indicator == null) return true;
        return !(x >= indicator.x) || !(x <= indicator.x + indicator.getWidth()) ||
                !(y >= indicator.y) || !(y <= indicator.y + indicator.getHeight());
    }

    //TODO: What a monstrosity...
    private static void drawTreePreview() {
        if (!treePlacerFragment.selecting || !treePlacerFragment.visible || !(treePlacerFragment.currentTree instanceof CustomShapeProp tree)) return;

        int tileX = World.toTile(treePlacerFragment.lastMousePosition.x);
        int tileY = World.toTile(treePlacerFragment.lastMousePosition.y);

        int[][] overlaps = new int[Vars.world.width()][Vars.world.height()];

        for (int i = 0; i < tree.shapes.get(treePlacerFragment.currentShape - 1).blocks.initialWordsAmount; i++) {
            if ((tree.shapes.get(treePlacerFragment.currentShape - 1).blocks.get(i) & 2) == 2) {
                int dx = tree.shapes.get(treePlacerFragment.currentShape - 1).unpackX(i);
                int dy = tree.shapes.get(treePlacerFragment.currentShape - 1).unpackY(i);
                Tile tile = Vars.world.tile(tileX + dx, tileY + dy);
                if (tile != null) {
                    Draw.z(Layer.overlayUI);
                    Lines.stroke(2f, (tile.block() instanceof StaticWall || tile.block() instanceof CustomShapeProp) ? Pal.remove : Pal.accent);
                    Draw.alpha(0.7f);
                    Fill.square(tile.worldx(), tile.worldy(), tilesize / 2f);

                    // Check neighboring tiles only if current tile is not StaticWall or CustomShapeProp
                    if (!(tile.block() instanceof StaticWall || tile.block() instanceof CustomShapeProp)) {
                        for (int j = 0; j < 4; j++) {
                            int neighborX = tileX + dx + Geometry.d4x[j];
                            int neighborY = tileY + dy + Geometry.d4y[j];
                            Tile neighborTile = Vars.world.tile(neighborX, neighborY);

                            if (neighborTile != null && neighborTile.block() instanceof CustomShapeProp && overlaps[neighborX][neighborY] < 2) {
                                Draw.z(Layer.overlayUI);
                                Lines.stroke(2f, Pal.remove);
                                Draw.alpha(0.35f);
                                Fill.square(neighborTile.worldx(), neighborTile.worldy(), tilesize / 2f);
                                overlaps[neighborX][neighborY]++;
                            }
                        }
                    }
                }
            }
        }
        Draw.reset();
    }

    private boolean canPlaceTree() {
        if (!(currentTree instanceof CustomShapeProp tree)) return false;

        int tileX = World.toTile(lastMousePosition.x);
        int tileY = World.toTile(lastMousePosition.y);

        for (int i = 0; i < tree.shapes.get(currentShape - 1).blocks.initialWordsAmount; i++) {
            if ((tree.shapes.get(currentShape - 1).blocks.get(i) & 2) == 2) {
                int dx = tree.shapes.get(currentShape - 1).unpackX(i);
                int dy = tree.shapes.get(currentShape - 1).unpackY(i);
                Tile tile = Vars.world.tile(tileX + dx, tileY + dy);
                if (tile != null && (tile.block() instanceof StaticWall || tile.block() instanceof CustomShapeProp)) {
                    return false;
                }
                for (int j = 0; j < 4; j++) {
                    int neighborX = tileX + dx + Geometry.d4x[j];
                    int neighborY = tileY + dy + Geometry.d4y[j];
                    Tile neighborTile = Vars.world.tile(neighborX, neighborY);
                    if (neighborTile != null && neighborTile.block() instanceof CustomShapeProp) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placeTree() {
        if (!canPlaceTree()) return;

        if (!(currentTree instanceof CustomShapeProp tree)) return;

        int tileX = World.toTile(lastMousePosition.x);
        int tileY = World.toTile(lastMousePosition.y);

        for (int i = 0; i < tree.shapes.get(currentShape - 1).blocks.initialWordsAmount; i++) {
            if ((tree.shapes.get(currentShape - 1).blocks.get(i) & 2) == 2) {
                int dx = tree.shapes.get(currentShape - 1).unpackX(i);
                int dy = tree.shapes.get(currentShape - 1).unpackY(i);
                Tile tile = Vars.world.tile(tileX + dx, tileY + dy);
                if (tile != null) {
                    Call.setTile(tile, currentTree, tile.team(), 0);
                }
            }
        }

        CustomShapePropProcess.instance.init();
    }

    public void toggle() {
        if (!visible || indicator.hasActions()) return;
        selecting = !selecting;
        if (selecting) {
            showUI();
            lastMousePosition.set(Core.input.mouseWorld());
        } else {
            hideUI();
            scene.setKeyboardFocus(null);
            scene.setScrollFocus(null);
        }
    }

    private void showUI() {
        indicator.actions(
                Actions.moveBy(0, -80f),
                Actions.alpha(1),
                Actions.moveBy(0, 80f, 0.3f, Interp.pow3Out)
        );
    }

    private void hideUI() {
        indicator.actions(
                Actions.moveBy(0, -80f, 0.3f, Interp.pow3In),
                Actions.alpha(0),
                Actions.moveBy(0, 80f)
        );
    }

    private void updateMousePosition(float x, float y) {
        lastMousePosition.set(Core.input.mouseWorld(x, y));
    }

    private void changeShape(int delta) {
        if (currentTree instanceof CustomShapeProp) {
            int[] currentGroup = (currentShape % 2 == 1) ? group1 : group2;
            int currentIndex = findIndex(currentGroup, currentShape);

            if (currentIndex != -1) {
                currentIndex = (currentIndex + delta + 4) % 4;
                currentShape = currentGroup[currentIndex];
            }

            updateCurrentShape();
        }
    }

    private void mirrorVertically() {
        int[][] pairs = (currentShape <= 4) ?
                new int[][]{{1, 4}, {2, 3}} :
                new int[][]{{5, 6}, {8, 7}};
        applyMirror(pairs);
    }

    private void mirrorHorizontally() {
        int[][] pairs = (currentShape <= 4) ?
                new int[][]{{1, 2}, {3, 4}} :
                new int[][]{{5, 8}, {7, 6}};
        applyMirror(pairs);
    }

    private void applyMirror(int[][] pairs) {
        for (int[] pair : pairs) {
            if (currentShape == pair[0]) {
                currentShape = pair[1];
                return;
            } else if (currentShape == pair[1]) {
                currentShape = pair[0];
                return;
            }
        }
    }

    private int findIndex(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    private void updateCurrentShape() {
        if (currentTree instanceof CustomShapeProp tree) {
            int totalShapes = tree.shapes.size;
            currentShape = Math.min(Math.max(currentShape, 1), totalShapes);
        }
    }
}