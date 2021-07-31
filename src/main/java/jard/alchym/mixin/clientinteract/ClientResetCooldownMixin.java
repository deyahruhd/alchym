package jard.alchym.mixin.clientinteract;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/***
 *  ClientResetCooldownMixin
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 02:56 on July, 28, 2021.
 ***/
@Mixin (MinecraftClient.class)
public abstract class ClientResetCooldownMixin {
    @Shadow
    protected int attackCooldown;
    @ModifyConstant (method = "tick", constant = @Constant (intValue = 10000))
    public int revertClientResettingCooldownForWhateverReason (int old) {
        if (attackCooldown > 0)
            attackCooldown --;

        return attackCooldown;
    }
}
