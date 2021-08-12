package jard.alchym.items;

import io.netty.buffer.Unpooled;
import jard.alchym.AlchymReference;
import net.fabricmc.fabric.impl.networking.ServerSidePacketRegistryImpl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class AlchymicReferenceItem extends Item {
    public AlchymicReferenceItem (Settings settings) { super (settings); }

    @Override
    public TypedActionResult<ItemStack> use (World world, PlayerEntity player, Hand hand) {
        if (! world.isClient) {
            ItemStack book = player.getStackInHand (hand);

            if (! hasBookNbt (book))
                putPage (book, new Identifier (AlchymReference.MODID, "title"));

            PacketByteBuf data = new PacketByteBuf (Unpooled.buffer());

            data.writeIdentifier (new Identifier (book.getNbt ().getString ("CurrentPage")));

            ServerSidePacketRegistryImpl.INSTANCE.sendToPlayer (player, AlchymReference.Packets.OPEN_GUIDEBOOK.id, data);

            // TODO: Play open sound
        }

        return TypedActionResult.success (player.getStackInHand (hand));
    }

    public static boolean hasBookNbt (ItemStack stack) {
        return stack.hasNbt () && stack.getNbt ().contains ("CurrentPage");
    }

    public static void putPage (ItemStack stack, Identifier id) {
        NbtCompound nbt;
        if (stack.hasNbt ())
            nbt = stack.getNbt ();
        else
            nbt = new NbtCompound ();

        nbt.putString ("CurrentPage", id.toString ());

        stack.setNbt (nbt);
    }
}
