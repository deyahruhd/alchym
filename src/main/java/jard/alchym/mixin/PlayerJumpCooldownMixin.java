package jard.alchym.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/***
 *  PlayerJumpCooldownMixin
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 13:26 on June, 24, 2021.
 ***/
@Mixin (LivingEntity.class)
public abstract class PlayerJumpCooldownMixin extends Entity {
    public PlayerJumpCooldownMixin (EntityType<?> entityType, World world) {
        super (entityType, world);
    }

    @ModifyConstant (method = "tickMovement", constant = @Constant (intValue = 10))
    public int replaceJumpCooldown (int old) {
        return 5;
    }
}
