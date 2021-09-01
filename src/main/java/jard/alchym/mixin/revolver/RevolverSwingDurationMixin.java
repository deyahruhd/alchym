package jard.alchym.mixin.revolver;

import jard.alchym.items.RevolverItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/***
 *  RevolverSwingDurationMixin
 *  Replaces the hardcoded 6-tick swing duration with a custom swing duration defined by instances of
 *  {@link jard.alchym.items.RevolverItem}.
 *
 *  Created by jard at 20:09 on June, 07, 2021.
 ***/
@Mixin (LivingEntity.class)
public abstract class RevolverSwingDurationMixin extends Entity {
    public RevolverSwingDurationMixin (EntityType<?> entityType, World world) {
        super (entityType, world);
    }

    @Shadow
    public ItemStack getMainHandStack () {
        return null;
    }

    @ModifyConstant (method = "getHandSwingDuration", constant = @Constant (intValue = 6))
    public int replaceDefaultSwingDuration (int old) {
        ItemStack stack = getMainHandStack ();

        if (! stack.isEmpty () && stack.getItem () instanceof RevolverItem) {
            return ((RevolverItem) stack.getItem ()).getSwingDuration (stack);
        }

        return old;
    }
}
