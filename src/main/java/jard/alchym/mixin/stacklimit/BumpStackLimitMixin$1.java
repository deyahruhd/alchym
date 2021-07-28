package jard.alchym.mixin.stacklimit;

import jard.alchym.AlchymReference;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/***
 *  BumpStackLimitMixin$1
 *  Increases the hard coded stack size used when the client syncs its creative inventory to the server.
 *
 *  Created by jard at 02:34 on January, 29, 2021.
 ***/
@Mixin (ServerPlayNetworkHandler.class)
public abstract class BumpStackLimitMixin$1 implements ServerPlayPacketListener {
    @ModifyConstant (method = "onCreativeInventoryAction", constant = @Constant (intValue = 64))
    public int bumpLimit (int val) {
        return AlchymReference.ITEM_STACK_LIMIT;
    }
}
