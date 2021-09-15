package jard.alchym.proxy;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.api.transmutation.revolver.RevolverBulletTravelFunction;
import jard.alchym.api.transmutation.revolver.RevolverDirectHitFunction;
import jard.alchym.api.transmutation.revolver.RevolverSplashHitFunction;
import jard.alchym.helper.MathHelper;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import java.util.stream.Stream;

/***
 *  ServerProxy
 *  Server-sided proxy.
 *
 *  Created by jard at 9:30 PM on March 23, 2019.
 ***/
public class ServerProxy extends Proxy {
    @Override
    public void onInitialize () {
        // Serverbound packets
        Alchym.content ().serverPackets.initialize ();
    }

    @Override
    public void renderPage (MatrixStack stack, BookPage page, AlchymReference.PageInfo.BookSide side, int bookProgress) {
        // No-op
    }
}
