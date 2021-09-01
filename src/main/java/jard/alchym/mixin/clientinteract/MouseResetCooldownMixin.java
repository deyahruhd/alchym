package jard.alchym.mixin.clientinteract;

import jard.alchym.client.MinecraftClientDataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/***
 *  MouseResetCooldownMixin
 *  Reverts the mouse resetting attack cooldown whenever it isn't being captured by the screen
 *
 *  Created by jard at 02:56 on July, 28, 2021.
 ***/
@Mixin (Mouse.class)
public class MouseResetCooldownMixin {
    @Shadow
    @Final
    public MinecraftClient client;

    @ModifyConstant (method = "lockCursor", constant = @Constant (intValue = 10000))
    public int revertMouseResettingCooldownForWhateverReason (int old) {
        return ((MinecraftClientDataAccess) client).getAttackCooldown ();
    }
}
