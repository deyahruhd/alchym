package jard.alchym.mixin;

import com.mojang.authlib.GameProfile;
import jard.alchym.items.CustomAttackItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 *  ItemAttackCooldownMixin
 *  Allows {@link jard.alchym.items.CustomAttackItem} implementations to override the attack cooldown
 *  call.
 *  Created by jard at 00:35 on June, 11, 2021.
 ***/
@Mixin (PlayerEntity.class)
public abstract class ItemAttackCooldownMixin extends LivingEntity {
    protected ItemAttackCooldownMixin (EntityType<? extends LivingEntity> entityType, World world) {
        super (entityType, world);
    }

    @Inject (
            method = "resetLastAttackedTicks",
            at = @At ("HEAD"),
            cancellable = true
    )
    public void cancelAttackCooldown (CallbackInfo info) {
        ItemStack stack = getMainHandStack ();

        if (stack.getItem () instanceof CustomAttackItem && ((CustomAttackItem) stack.getItem ()).getAttackCooldown (stack) > 0)
            info.cancel ();
    }
}
