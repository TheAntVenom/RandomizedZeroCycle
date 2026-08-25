package com.rzc.util;

public class DragonState {
    public static int lastTargetX = 0;
    public static int lastTargetY = 0;
    public static int lastTargetZ = 0;
    public static boolean hasTarget = false;

    public static boolean isTargetValid() {
        return hasTarget;
    }

    public static void reset() {
        hasTarget = false;
        lastTargetX = 0;
        lastTargetY = 0;
        lastTargetZ = 0;
    }
}