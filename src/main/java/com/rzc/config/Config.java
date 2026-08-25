package com.rzc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("randomized-zero-cycle.json").toFile();

    public enum Mode {
        Vanilla,
        FullyRandom,
        ExpandedZeroCycle,
        TwelveVanillaNodes,
        ChooseXZ
    }

    // Config Values
    public static Mode mode = Mode.ExpandedZeroCycle;
    public static double randomRingMin = 24.0;
    public static double randomRingMax = 96.0;
    public static double chooseX = 0.0;
    public static double chooseZ = 0.0;
    public static int spawnHeight = 128;
    public static int yOffset = 0; // Baseline offset applied to calculated node Y-level
    public static boolean spawnMsg = true;
    public static boolean deathMsg = true;

    // Node Marker Settings
    public static boolean nodeMarker = true;
    public static String nodeMarkerColor = "FF0000"; // Pure Red Default

    // Cached RGB Floats for Particle Engine (0.0f - 1.0f)
    public static float nodeMarkerRed = 1.0f;
    public static float nodeMarkerGreen = 0.0f;
    public static float nodeMarkerBlue = 0.0f;

    // 10 Towers: Each tower has [7/8 Face Node (Inward toward 0,0), 1/8 Node (R=20 Ray)]
    public static final double[][][] towerCoords = new double[][][] {
        { { 28.0, 20.0 },   { 16.0, 12.0 } },    // Tower 1  (East-Northeast)
        { { 11.0, 33.0 },   { 6.0, 19.0 } },     // Tower 2  (North-Northeast)
        { { -11.0, 33.0 },  { -6.0, 19.0 } },    // Tower 3  (North-Northwest)
        { { -29.0, 28.0 },  { -16.0, 12.0 } },   // Tower 4  (Vanilla Anchor: Back-Left 7/8)
        { { -35.0, 0.0 },   { -20.0, 0.0 } },    // Tower 5  (Vanilla Anchor: West 1/8)
        { { -28.0, -20.0 }, { -16.0, -12.0 } },  // Tower 6  (West-Southwest)
        { { -11.0, -33.0 }, { -6.0, -19.0 } },   // Tower 7  (South-Southwest)
        { { 11.0, -33.0 },  { 6.0, -19.0 } },    // Tower 8  (South-Southeast)
        { { 28.0, -29.0 },  { 16.0, -12.0 } },   // Tower 9  (Vanilla Anchor: Front-Right 7/8)
        { { 35.0, 0.0 },    { 20.0, 0.0 } }      // Tower 10 (Vanilla Anchor: East 1/8)
    };

    private static class ConfigData {
        public Mode mode = Mode.ExpandedZeroCycle;
        public double randomRingMin = 24.0;
        public double randomRingMax = 96.0;
        public double chooseX = 0.0;
        public double chooseZ = 0.0;
        public int spawnHeight = 128;
        public int yOffset = 0;
        public boolean spawnMsg = true;
        public boolean deathMsg = true;
        public boolean nodeMarker = true;
        public String nodeMarkerColor = "FF0000";
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            updateRGBFromHex();
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                mode = data.mode != null ? data.mode : Mode.ExpandedZeroCycle;
                randomRingMin = data.randomRingMin;
                randomRingMax = data.randomRingMax;
                chooseX = data.chooseX;
                chooseZ = data.chooseZ;
                spawnHeight = data.spawnHeight;
                yOffset = data.yOffset;
                spawnMsg = data.spawnMsg;
                deathMsg = data.deathMsg;
                nodeMarker = data.nodeMarker;
                nodeMarkerColor = data.nodeMarkerColor != null ? data.nodeMarkerColor : "FF0000";
                
                updateRGBFromHex();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        updateRGBFromHex();

        ConfigData data = new ConfigData();
        data.mode = mode;
        data.randomRingMin = randomRingMin;
        data.randomRingMax = randomRingMax;
        data.chooseX = chooseX;
        data.chooseZ = chooseZ;
        data.spawnHeight = spawnHeight;
        data.yOffset = yOffset;
        data.spawnMsg = spawnMsg;
        data.deathMsg = deathMsg;
        data.nodeMarker = nodeMarker;
        data.nodeMarkerColor = nodeMarkerColor;

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateRGBFromHex() {
        if (nodeMarkerColor == null || nodeMarkerColor.isEmpty()) {
            nodeMarkerColor = "FF0000";
        }

        String hex = nodeMarkerColor.replace("#", "").trim();
        
        if (hex.length() == 6) {
            try {
                int r = Integer.parseInt(hex.substring(0, 2), 16);
                int g = Integer.parseInt(hex.substring(2, 4), 16);
                int b = Integer.parseInt(hex.substring(4, 6), 16);

                nodeMarkerRed = r / 255.0f;
                nodeMarkerGreen = g / 255.0f;
                nodeMarkerBlue = b / 255.0f;
                return;
            } catch (NumberFormatException ignored) {}
        }

        nodeMarkerRed = 1.0f;
        nodeMarkerGreen = 0.0f;
        nodeMarkerBlue = 0.0f;
    }
}