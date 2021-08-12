package jard.alchym.items;

import jard.alchym.AlchymReference;
import jard.alchym.helper.TransmutationHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/***
 *  PhilosophersStoneItem
 *  The philosopher's stone item.
 *
 *  Created by jard at 9:02 PM on December 30, 2018.
 ***/
public class PhilosophersStoneItem extends TransmutableReagentItem {
    public final long minCharge, maxCharge;

    // Kinda ugly hack to retain the ItemStack for getUnitCharge
    private static ItemStack heldStack = null;

    public PhilosophersStoneItem (Settings settings, AlchymReference.PhilosophersStoneCharges charge) {
        super (settings);

        minCharge = charge.min;
        maxCharge = charge.max;
    }

    @Override
    public boolean isReagent() {
        return true;
    }

    @Override
    public long getUnitCharge() {
        long charge = 0;

        if (heldStack != null) {
            charge = getStoneCharge (heldStack);
            heldStack = null;
        }
        return charge;
    }

    @Override
    public AlchymReference.Reagents getReagentType() {
        return AlchymReference.Reagents.PHILOSOPHERS_STONE;
    }

    @Override
    public TypedActionResult<ItemStack> use (World world, PlayerEntity player, Hand hand) {
        if (!world.isClient && isPhilosophersStone (player.getStackInHand (hand))) {
            PhilosophersStoneItem.setHeldStack (player.getStackInHand (hand));

            // Delegate to superclass use method
            return super.use (world, player, hand);
        }

        return new TypedActionResult(ActionResult.PASS, player.getStackInHand (hand));
    }

    public static long getStoneCharge (ItemStack stack) {
        long charge = 0L;

        if (stack != null && ! stack.isEmpty() && stack.getItem () instanceof PhilosophersStoneItem) {
            if (stack.hasNbt () && stack.getNbt ().contains (AlchymReference.MODID) &&
                    stack.getNbt ().getCompound (AlchymReference.MODID).contains ("stone_charge")) {
                charge = stack.getNbt ().getCompound (AlchymReference.MODID).getLong ("stone_charge");
            } else {
                NbtCompound nbt = new NbtCompound ();
                charge = ((PhilosophersStoneItem) stack.getItem ()).minCharge;
                nbt.putLong ("stone_charge", charge);
                stack.setNbt (nbt);
            }
        }

        return charge;
    }

    public static void setHeldStack (ItemStack stack) {
        heldStack = stack;
    }

    private boolean isPhilosophersStone (ItemStack stack) {
        return TransmutationHelper.isReagent (stack) && stack.getItem() instanceof PhilosophersStoneItem;
    }
}
