package jard.alchym.init;

import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/***
 *  InitAbstractPackets
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 02:56 on September 14, 2021.
 ***/
public abstract class InitAbstractPackets {
    protected final Map <Identifier, PacketConsumer> PACKET_BEHAVIOR = new HashMap <> ();
    protected final InitAlchym alchym;

    InitAbstractPackets (InitAlchym alchym) {
        this.alchym = alchym;
    }

    public abstract void initialize ();
}
