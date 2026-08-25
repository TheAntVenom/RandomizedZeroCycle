package com.rzc.mixin;

import com.rzc.config.Config;
import com.rzc.util.DragonState;
import com.rzc.util.NodeMarkerRenderer;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin {

    @Unique
    private boolean rzc$hasSentDeathMsg = false;

    // Ticks every tick while the dragon is active to keep the particle line continuously visible
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void rzc$renderContinuousMarker(CallbackInfo ci) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;

        if (Config.nodeMarker && !dragon.getEntityWorld().isClient && dragon.isAlive() && DragonState.isTargetValid()) {
            Vec3d activeTarget = new Vec3d(DragonState.lastTargetX, DragonState.lastTargetY, DragonState.lastTargetZ);
            NodeMarkerRenderer.tickMarker((ServerWorld) dragon.getEntityWorld(), activeTarget);
        }
    }

    // Clears the target marker as soon as the dragon enters its death sequence animation
    @Inject(method = "updatePostDeath", at = @At("HEAD"))
    private void rzc$clearMarkerOnDeathSequence(CallbackInfo ci) {
        DragonState.hasTarget = false;
    }

    // Fires instantly on the final lethal blow tick
    @Inject(method = "parentDamage", at = @At("RETURN"))
    private void rzc$onParentDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;

        // Reset state if health is restored above 0 (e.g. reset/re-spawn)
        if (dragon.getHealth() > 0.0F) {
            this.rzc$hasSentDeathMsg = false;
            return;
        }

        // Fires instantly on the killing blow tick when health drops to 0
        if (dragon.getHealth() <= 0.0F && !this.rzc$hasSentDeathMsg && Config.deathMsg && dragon.getEntityWorld() instanceof ServerWorld) {
            this.rzc$hasSentDeathMsg = true;

            int nodeX = DragonState.lastTargetX;
            int nodeY = DragonState.lastTargetY;
            int nodeZ = DragonState.lastTargetZ;

            DragonState.hasTarget = false;

            ServerWorld world = (ServerWorld) dragon.getEntityWorld();

            // Prefix: [RZC] - 
            LiteralText prefix = new LiteralText("[");
            prefix.formatted(Formatting.DARK_GRAY);

            LiteralText tag = new LiteralText("RZC");
            tag.formatted(Formatting.DARK_AQUA, Formatting.BOLD);

            LiteralText closeBracket = new LiteralText("] - ");
            closeBracket.formatted(Formatting.DARK_GRAY);

            prefix.append(tag).append(closeBracket);

            // Mode
            LiteralText modeVal = new LiteralText(Config.mode.name());
            modeVal.formatted(Formatting.GREEN);
            prefix.append(modeVal);

            // Node coordinates from cached state
            LiteralText nodeLabel = new LiteralText(" - Node: ");
            nodeLabel.formatted(Formatting.WHITE);

            LiteralText nodeVal = new LiteralText(String.format("%dx,%dy,%dz", nodeX, nodeY, nodeZ));
            nodeVal.formatted(Formatting.GREEN);

            prefix.append(nodeLabel).append(nodeVal);

            // Spawn Height
            LiteralText spawnLabel = new LiteralText(" - ");
            spawnLabel.formatted(Formatting.WHITE);

            LiteralText spawnVal = new LiteralText(Config.spawnHeight + "y Spawn");
            spawnVal.formatted(Formatting.YELLOW);

            prefix.append(spawnLabel).append(spawnVal);

            // Broadcast instantly to players on final hit
            for (ServerPlayerEntity player : world.getPlayers()) {
                player.sendMessage(prefix, false);
            }
        }
    }
}