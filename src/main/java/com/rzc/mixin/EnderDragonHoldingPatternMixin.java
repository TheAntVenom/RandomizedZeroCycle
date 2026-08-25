package com.rzc.mixin;

import com.rzc.config.Config;
import com.rzc.util.DragonState;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(HoldingPatternPhase.class)
public abstract class EnderDragonHoldingPatternMixin extends AbstractPhase {

    @Shadow private Vec3d target;

    @Unique
    private static final Random RZC_RANDOM = new Random();

    @Unique
    private static final String SPAWN_TAG = "rzc_spawn_initialized";

    public EnderDragonHoldingPatternMixin(EnderDragonEntity dragon) {
        super(dragon);
    }

    @Inject(method = "beginPhase", at = @At("HEAD"))
    private void rzc$onBeginHoldingPattern(CallbackInfo ci) {
        // ONLY manipulate physical dragon placement if the dragon literally just spawned
        if (this.dragon != null && this.dragon.age < 5 && !this.dragon.getScoreboardTags().contains(SPAWN_TAG)) {
            double spawnY = (double) Config.spawnHeight;
            this.dragon.refreshPositionAndAngles(this.dragon.getX(), spawnY, this.dragon.getZ(), this.dragon.yaw, 0.0F);
            this.dragon.prevY = spawnY;
            this.dragon.lastRenderY = spawnY;
            this.dragon.setVelocity(0.0D, 0.0D, 0.0D);
            this.dragon.velocityDirty = true;
        }
    }

    @Inject(method = "method_6842", at = @At("TAIL"))
    private void rzc$onGenerateTargetNode(CallbackInfo ci) {
        if (this.target == null || this.dragon == null) return;

        // --- SUBSEQUENT MID-FIGHT NODES ---
        if (this.dragon.getScoreboardTags().contains(SPAWN_TAG)) {
            // Apply yOffset to all regular pathfinding nodes throughout the fight
            // without touching X, Z, or physical entity positions.
            if (Config.yOffset != 0) {
                double shiftedY = Math.max(55.0D, Math.min(this.target.getY(), 85.0D) + Config.yOffset);
                this.target = new Vec3d(this.target.getX(), shiftedY, this.target.getZ());
            }
            return;
        }

        // --- INITIAL SPAWN SETUP ONLY ---
        this.dragon.getScoreboardTags().add(SPAWN_TAG);

        if (this.dragon.age < 20) {
            double spawnY = (double) Config.spawnHeight;
            this.dragon.updatePosition(this.dragon.getX(), spawnY, this.dragon.getZ());
        }

        double vanillaNodeY = Math.max(55.0D, Math.min(this.target.getY(), 85.0D) + Config.yOffset);
        boolean isOneEighth = false;

        if (Config.mode != Config.Mode.Vanilla) {
            switch (Config.mode) {
                case FullyRandom: {
                    double angle = RZC_RANDOM.nextDouble() * Math.PI * 2.0;
                    double dist = Config.randomRingMin + (RZC_RANDOM.nextDouble() * (Config.randomRingMax - Config.randomRingMin));
                    int targetX = (int) Math.round(Math.cos(angle) * dist);
                    int targetZ = (int) Math.round(Math.sin(angle) * dist);
                    this.target = new Vec3d(targetX, vanillaNodeY, targetZ);
                    break;
                }

                case ExpandedZeroCycle: {
                    if (Config.towerCoords != null && Config.towerCoords.length > 0) {
                        int towerIdx = RZC_RANDOM.nextInt(Config.towerCoords.length);
                        double[][] tower = Config.towerCoords[towerIdx];

                        if (RZC_RANDOM.nextFloat() < 0.875f) {
                            // 87.5% chance: 7/8 Primary Outer Node
                            this.target = new Vec3d(tower[0][0], vanillaNodeY, tower[0][1]);
                        } else {
                            // 12.5% chance: 1/8 Secondary Inner Node
                            this.target = new Vec3d(tower[1][0], vanillaNodeY, tower[1][1]);
                            isOneEighth = true;
                        }
                    }
                    break;
                }

                case TwelveVanillaNodes: {
                    int nodeIndex = RZC_RANDOM.nextInt(12);
                    double angle = (nodeIndex / 12.0) * Math.PI * 2.0;
                    double x = Math.cos(angle) * 60.0;
                    double z = Math.sin(angle) * 60.0;
                    this.target = new Vec3d(x, vanillaNodeY, z);
                    break;
                }

                case ChooseXZ: {
                    this.target = new Vec3d(Config.chooseX, vanillaNodeY, Config.chooseZ);
                    break;
                }

                default:
                    break;
            }
        } else {
            this.target = new Vec3d(this.target.getX(), vanillaNodeY, this.target.getZ());
        }

        // Save active spawn coordinates to DragonState runtime holder
        DragonState.lastTargetX = (int) Math.round(this.target.getX());
        DragonState.lastTargetY = (int) Math.round(this.target.getY());
        DragonState.lastTargetZ = (int) Math.round(this.target.getZ());
        DragonState.hasTarget = true;

        // Output message in compact [RZC] format for spawnMsg
        if (Config.spawnMsg && this.dragon.getEntityWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) this.dragon.getEntityWorld();

            LiteralText prefix = new LiteralText("[");
            prefix.formatted(Formatting.DARK_GRAY);

            LiteralText tag = new LiteralText("RZC");
            tag.formatted(Formatting.DARK_AQUA, Formatting.BOLD);

            LiteralText closeBracket = new LiteralText("] ");
            closeBracket.formatted(Formatting.DARK_GRAY);

            LiteralText coords = new LiteralText(String.format("%dx, %dy, %dz", DragonState.lastTargetX, DragonState.lastTargetY, DragonState.lastTargetZ));
            coords.formatted(Formatting.GREEN);

            prefix.append(tag).append(closeBracket).append(coords);

            if (isOneEighth) {
                LiteralText fractionTag = new LiteralText(" (1/8)");
                fractionTag.formatted(Formatting.DARK_GRAY);
                prefix.append(fractionTag);
            }

            for (ServerPlayerEntity player : world.getPlayers()) {
                player.sendMessage(prefix, false);
            }
        }
    }
}