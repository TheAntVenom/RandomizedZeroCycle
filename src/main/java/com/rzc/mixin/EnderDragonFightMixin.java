package com.rzc.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {

    @Shadow
    private ServerWorld world;

    @Inject(method = "createDragon", at = @At("RETURN"))
    private void rzc$onDragonSpawn(CallbackInfoReturnable<EnderDragonEntity> cir) {
        // Spawn message broadcast is handled dynamically in EnderDragonHoldingPatternMixin 
        // to guarantee accurate, real-time node target coordinates on spawn.
    }
}