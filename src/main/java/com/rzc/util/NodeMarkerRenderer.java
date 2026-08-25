package com.rzc.util;

import com.rzc.config.Config;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class NodeMarkerRenderer {

    public static void tickMarker(ServerWorld world, Vec3d rawTarget) {
        if (rawTarget == null) return;

        double nodeX = Math.floor(rawTarget.getX());
        double nodeZ = Math.floor(rawTarget.getZ());

        double minY = 40.0;
        double maxY = 255.0; // Extends to world height
        double stepSize = 1.0;

        // Force reload of RGB values from Hex just in case
        float red = Config.nodeMarkerRed;
        float green = Config.nodeMarkerGreen;
        float blue = Config.nodeMarkerBlue;

        // CRITICAL FIX: If red is 0, MC 1.16 DustParticleEffect reverts to default ambient blue/red.
        // Using 0.001f prevents the engine fallback while keeping colors rich.
        float r = (red <= 0.0f) ? 0.001f : red;
        float g = green;
        float b = blue;

        // Scale 1.0f renders true vibrant colors without texture overlap washout
        DustParticleEffect dustEffect = new DustParticleEffect(r, g, b, 1.0f);

        for (double currentY = minY; currentY <= maxY; currentY += stepSize) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                world.spawnParticles(
                    player,
                    dustEffect,
                    true,
                    nodeX, currentY, nodeZ,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
                );
            }
        }
    }
}