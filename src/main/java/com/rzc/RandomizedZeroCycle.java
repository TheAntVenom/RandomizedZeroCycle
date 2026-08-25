package com.rzc;

import com.rzc.config.Config;
import net.fabricmc.api.ModInitializer;

public class RandomizedZeroCycle implements ModInitializer {
    @Override
    public void onInitialize() {
        Config.load();
        System.out.println("[RZC] Mod initialized successfully.");
    }
}