package omaloon.content;

import mindustry.content.*;
import mindustry.type.*;
import omaloon.type.liquid.*;

import static arc.graphics.Color.*;

public class OlLiquids {
    public static Liquid
            glacium, tiredGlacium,

    end;

    public static void load(){
        glacium = new CrystalLiquid("glacium", valueOf("5e929d")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            colorFrom = valueOf("5e929d");
            colorTo = valueOf("3e6067");

            canStayOn.add(Liquids.water);
        }};

        tiredGlacium = new Liquid("tired-glacium", valueOf("456c74")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            canStayOn.add(Liquids.water);
        }};

    }

}
