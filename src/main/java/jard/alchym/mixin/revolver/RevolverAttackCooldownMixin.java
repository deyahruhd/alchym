package jard.alchym.mixin.revolver;

import jard.alchym.items.RevolverItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 *  RevolverAttackCooldownMixin
 *  Allows the {@link jard.alchym.items.RevolverItem} to override the attack cooldown call.
 *
 *  Created by jard at 00:35 on June, 11, 2021.
 ***/
@Mixin (PlayerEntity.class)
public abstract class RevolverAttackCooldownMixin extends LivingEntity {
    protected RevolverAttackCooldownMixin (EntityType<? extends LivingEntity> entityType, World world) {
        super (entityType, world);
    }

    @Inject (
            method = "resetLastAttackedTicks",
            at = @At ("HEAD"),
            cancellable = true
    )
    public void cancelAttackCooldown (CallbackInfo info) {
        ItemStack stack = getMainHandStack ();

        if (stack.getItem () instanceof RevolverItem && ((RevolverItem) stack.getItem ()).getAttackCooldown (stack) > 0)
            info.cancel ();
    }
}
