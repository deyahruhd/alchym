package jard.alchym.mixin.stacklimit;

import jard.alchym.AlchymReference;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/***
 *  BumpStackLimitMixin$2
 *  Increases the hard coded stack size used when two item entities attempt to merge.
 *
 *  Created by jard at 02:34 on January, 29, 2021.
 ***/
@Mixin (ItemEntity.class)
public abstract class BumpStackLimitMixin$2 extends Entity {
    public BumpStackLimitMixin$2 (EntityType<?> entityType, World world) {
        super (entityType, world);
    }

    @ModifyConstant (
            method = "merge(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V",
            constant = @Constant (intValue = 64))
    private static int bumpLimit (int val) {
        return AlchymReference.ITEM_STACK_LIMIT;
    }
}
