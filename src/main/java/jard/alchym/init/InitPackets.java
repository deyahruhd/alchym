package jard.alchym.init;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.client.gui.screen.GuidebookScreen;
import jard.alchym.items.AlchymicReferenceItem;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class InitPackets {
    private final Map<Identifier, PacketConsumer> PACKET_BEHAVIOR = new HashMap<> ();

    protected final InitAlchym alchym;

    InitPackets (InitAlchym alchym) {
        this.alchym = alchym;

        PACKET_BEHAVIOR.put (AlchymReference.Packets.OPEN_GUIDEBOOK.id,
                (packetContext, data) -> {
                    BookPage pageToOpen = alchym.pages.get (data.readIdentifier ());

                    GuidebookScreen screen = new GuidebookScreen (pageToOpen, new LiteralText (""));

                    net.minecraft.client.MinecraftClient.getInstance ().setScreen (screen);
                });

        PACKET_BEHAVIOR.put (AlchymReference.Packets.SYNC_GUIDEBOOK.id,
                (packetContext, data) -> {
                    ItemStack book = packetContext.getPlayer ().getStackInHand (Hand.MAIN_HAND);

                    if (! (book.getItem () instanceof AlchymicReferenceItem))
                        book = packetContext.getPlayer ().getStackInHand (Hand.OFF_HAND);

                    // Repeat the check
                    if (! (book.getItem () instanceof AlchymicReferenceItem))
                        return;

                    AlchymicReferenceItem.putPage (book, data.readIdentifier ());
                });
    }

    public void initialize () {
        Stream<AlchymReference.Packets> serverboundPackets = Stream.of (AlchymReference.Packets.values ()).filter (AlchymReference.Packets::isServerbound);
        Stream<AlchymReference.Packets>	clientboundPackets = Stream.of (AlchymReference.Packets.values ()).filter (AlchymReference.Packets::isClientbound);

        serverboundPackets.forEach (
                (packet) -> {
                    PacketConsumer action = PACKET_BEHAVIOR.get (packet.id);
                    ServerSidePacketRegistry.INSTANCE.register (packet.id,
                            (packetContext, packetByteBuf) -> {
                                final PacketByteBuf data = new PacketByteBuf (packetByteBuf.copy ());
                                packetContext.getTaskQueue ().execute (() -> action.accept (packetContext, data));
                            });
                });
        clientboundPackets.forEach (
                (packet) -> {
                    PacketConsumer action = PACKET_BEHAVIOR.get (packet.id);
                    Alchym.getProxy ().registerPacket (packet, action);
                });
    }
}
