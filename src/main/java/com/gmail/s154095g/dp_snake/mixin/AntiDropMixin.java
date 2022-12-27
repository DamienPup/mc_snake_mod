package com.gmail.s154095g.dp_snake.mixin;

import com.gmail.s154095g.dp_snake.item.SnakeController;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class AntiDropMixin {
    @Shadow public abstract void setPickupDelay(int pickupDelay);

    @Shadow public abstract ItemStack getStack();

    @Inject(method="tick", at=@At("HEAD"))
    void preventSnakeControllerDrops(CallbackInfo ci){
        ItemStack stack = getStack();
        if (!stack.isEmpty() && stack.getItem() instanceof SnakeController)
            setPickupDelay(0);
    }
}
