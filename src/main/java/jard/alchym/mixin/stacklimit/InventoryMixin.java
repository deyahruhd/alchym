package jard.alchym.mixin.stacklimit;

import jard.alchym.AlchymReference;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

/***
 *  InventoryMixin
 *  Increases the maximum stack size in any {@link Inventory} to an arbitrary byte-limited value as defined in
 *  {@code AlchymReference}.
 *
 *  Created by jard at 02:09 on January, 29, 2021.
 ***/

@Mixin (Inventory.class)
public interface InventoryMixin extends Inventory {
    @Override
    default int getMaxCountPerStack () {
        return AlchymReference.ITEM_STACK_LIMIT;
    }
}
