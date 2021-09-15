package jard.alchym.init;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.client.gui.screen.GuidebookScreen;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/***
 *  InitClientPackets
 *  The initializing module that initializes all server-to-client packets.
 *
 *  Created by jard at 02:56 on September 14, 2021.
 ***/
public class InitClientPackets extends InitAbstractPackets {
    InitClientPackets (InitAlchym alchym) {
        super (alchym);

        PACKET_BEHAVIOR.put (AlchymReference.Packets.OPEN_GUIDEBOOK.id,
                (packetContext, data) -> {
                    BookPage pageToOpen = alchym.pages.get (data.readIdentifier ());

                    GuidebookScreen screen = new GuidebookScreen (pageToOpen, new LiteralText (""));

                    net.minecraft.client.MinecraftClient.getInstance ().setScreen (screen);
                });
    }

    @Override
    public void initialize () {
        System.out.println ("Initializing client packets");
        Stream <AlchymReference.Packets> clientboundPackets = Stream.of (AlchymReference.Packets.values ()).filter (AlchymReference.Packets::isClientbound);

        clientboundPackets.forEach (
                (packet) -> {
                    PacketConsumer action = PACKET_BEHAVIOR.get (packet.id);
                    ClientSidePacketRegistry.INSTANCE.register (packet.id,
                            (packetContext, packetByteBuf) -> {
                                final PacketByteBuf data = new PacketByteBuf (packetByteBuf.copy ());
                                packetContext.getTaskQueue ().execute (() -> action.accept (packetContext, data));
                            });
                });
    }
}
