package com.gmail.s154095g.dp_snake.mixin;

import com.gmail.s154095g.dp_snake.SnakeMod;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class EndGameOnDeathMixin {

    @Inject(method = "onDeath", at=@At("HEAD"))
    void endGame(DamageSource damageSource, CallbackInfo ci){
        SnakeMod.removeGame(((ServerPlayerEntity)(Object)this));
    }
}
