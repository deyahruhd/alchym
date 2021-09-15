package jard.alchym.proxy;

import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.api.transmutation.revolver.RevolverBulletTravelFunction;
import jard.alchym.api.transmutation.revolver.RevolverDirectHitFunction;
import jard.alchym.api.transmutation.revolver.RevolverSplashHitFunction;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.client.util.math.MatrixStack;

/***
 *  Proxy
 *  An abstract proxy class designed to cleanly separate the two logical sides that code can run on. Code that must run
 *  on both logical sides should be placed in the Alchym class, while code that is eventually delegated to a side
 *  should be placed as methods within this class and overrided within ServerProxy or ClientProxy
 *
 *  This prevents abuse of the proxying system to call code on both logical sides.
 *
 *  Created by jard at 6:50 PM on March 23, 2019.
 ***/
public abstract class Proxy {
    public abstract void onInitialize ();

    public abstract void renderPage (MatrixStack stack, BookPage page, AlchymReference.PageInfo.BookSide side, int bookProgress);
}
