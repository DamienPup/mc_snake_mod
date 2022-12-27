package com.gmail.s154095g.dp_snake;

import net.minecraft.util.Identifier;

public final class Utils {
    private Utils(){
        //static class
    }

    public static Identifier identifier(String id){
        return new Identifier(SnakeMod.MOD_ID, id);
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
