package jard.alchym.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/***
 *  ItemStackMixin
 *  Mixin adding extra functionality to {@link ItemStack} serialization that supports stack counts larger than 255.
 *
 *  Created by jard at 4:50 PM on February 03, 2019.
 ***/
@Mixin (ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    private int count;
    @Shadow
    public Item getItem () { return null; }
    @Shadow
    private NbtCompound nbt;

    @Inject (method = "writeNbt", at = @At ("HEAD"), cancellable = true)
    public void writeNbt (NbtCompound nbt, CallbackInfoReturnable<NbtCompound> info) {
        if (count > Byte.MAX_VALUE) {
            // Write the amount as an integer instead of a byte
            Identifier identifier = Registry.ITEM.getId (getItem ());
            nbt.putString("id", identifier == null ? "minecraft:air" : identifier.toString ());

            nbt.putByte("Count", (byte) this.count);
            nbt.putInt("FullCount", this.count);
            if (this.nbt != null) {
                nbt.put("nbt", this.nbt);
            }

            info.cancel ();

            info.setReturnValue (nbt);
        }
    }

    @Inject(method = "setNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    private void setNbt (NbtCompound nbt, CallbackInfo info) {
        if (nbt != null && nbt.contains ("FullCount"))
            count = nbt.getInt ("FullCount");
    }
}
