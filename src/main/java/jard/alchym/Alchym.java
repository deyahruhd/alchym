package jard.alchym;

import jard.alchym.init.InitAlchym;
import jard.alchym.proxy.ClientProxy;
import jard.alchym.proxy.Proxy;
import jard.alchym.proxy.ServerProxy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/***
 *  Alchym
 *  Main mod initializer.
 *
 *  Created by jard at 12:21 AM on ‎December ‎19, ‎2018.
 ***/
public class Alchym implements ModInitializer {
	private static final InitAlchym alchymContent = new InitAlchym ();
	private static final Proxy proxy;
	private static final ModContainer container;
	static {
		proxy = FabricLoader.getInstance ().getEnvironmentType () == EnvType.CLIENT ? new ClientProxy () : new ServerProxy ();
		container = FabricLoader.getInstance ().getModContainer (AlchymReference.MODID).get ();
	}

	@Override
	public void onInitialize () {
		alchymContent.initialize ();

		// Must be done in mod initialization. Minecraft block/fluid/item objects do not exist during static init
		AlchymReference.AdditionalMaterials.initExistingSpecies ();

		proxy.onInitialize ();
	}

	public static Item getPhilosophersStone () {
		return alchymContent.getPhilosophersStone ();
	}

	public static InitAlchym content () {
		return alchymContent;
	}
	public static Proxy getProxy () { return proxy; }

	public static BufferedReader getDataResource (Identifier id) throws IOException {
		String assetsPath = String.format ("data/%s/%s", id.getNamespace (), id.getPath ());
		Path path = container.getPath (assetsPath);

		return Files.newBufferedReader (path);
	}
}
