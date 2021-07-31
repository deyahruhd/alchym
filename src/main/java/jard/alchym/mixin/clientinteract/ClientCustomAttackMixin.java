package jard.alchym.mixin.clientinteract;

import jard.alchym.client.MinecraftClientDataAccess;
import jard.alchym.items.CustomAttackItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.snooper.SnooperListener;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 *  ClientCustomAttackMixin
 *  Allows {@link CustomAttackItem} implementations to override the client-sided player attack behavior.
 *
 *  Created by jard at 01:54 on July, 28, 2021.
 ***/
@Mixin (MinecraftClient.class)
public abstract class ClientCustomAttackMixin extends ReentrantThreadExecutor<Runnable> implements SnooperListener, WindowEventHandler, MinecraftClientDataAccess {
    @Shadow
    protected int attackCooldown;
    @Shadow
    public ClientPlayerEntity player;

    public ClientCustomAttackMixin (String string) {
        super (string);
    }

    @Inject (method = "doAttack", at = @At ("HEAD"), cancellable = true)
    public void hookAttack (CallbackInfo info) {
        if (attackCooldown <= 0
                && ! player.getMainHandStack ().isEmpty ()
                && player.getMainHandStack ().getItem () instanceof CustomAttackItem
                && ((CustomAttackItem) player.getMainHandStack ().getItem ()).clientAttack (player, player.getMainHandStack (), player.getRotationVecClient ())) {
            attackCooldown = ((CustomAttackItem) player.getMainHandStack ().getItem ()).getAttackCooldown (player.getMainHandStack ());
            player.swingHand (Hand.MAIN_HAND);
            player.resetLastAttackedTicks ();
            info.cancel ();
        }
    }

    @Inject (method = "handleBlockBreaking", at = @At ("HEAD"), cancellable = true)
    public void cancelBlockBreaking (boolean isMining, CallbackInfo info) {
        if (! isMining && attackCooldown > 0
                && ! player.getMainHandStack ().isEmpty ()
                && player.getMainHandStack ().getItem () instanceof CustomAttackItem)
            info.cancel ();
    }

    @Override
    public int getAttackCooldown () {
        return attackCooldown;
    }
}
