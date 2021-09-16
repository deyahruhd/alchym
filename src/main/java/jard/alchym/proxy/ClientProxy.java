package jard.alchym.proxy;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.client.render.book.PageRenderDispatcher;
import jard.alchym.client.render.entity.RevolverBulletEntityRenderer;
import jard.alchym.client.render.model.ChymicalFlaskBakedModel;
import jard.alchym.client.render.model.SpeedloaderBakedModel;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import jard.alchym.helper.RevolverHelper;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/***
 *  ClientProxy
 *  Client-sided proxy.
 *
 *  Created by jard at 7:02 PM on March 23, 2019.
 ***/
public class ClientProxy extends Proxy {
    private PageRenderDispatcher pageRenderDispatcher = null;

    @Override
    public void onInitialize () {
        pageRenderDispatcher = new PageRenderDispatcher ();

        BlockRenderLayerMap.INSTANCE.putBlock (Alchym.content ().blocks.copperCrucible, RenderLayer.getCutout ());
        BlockRenderLayerMap.INSTANCE.putBlock (Alchym.content ().blocks.alembic, RenderLayer.getCutout ());

        // Chymical flask models
        ModelLoadingRegistry.INSTANCE.registerModelProvider ((resourceManager, consumer) -> {
            consumer.accept (new ModelIdentifier (new Identifier (AlchymReference.MODID,
                    String.format ("%s_base", AlchymReference.Items.CHYMICAL_FLASK.getName ())), "inventory"));

            for (AlchymReference.Materials material : AlchymReference.Materials.values ()) {
                if (material.forms.contains (AlchymReference.Materials.Forms.LIQUID))
                    consumer.accept (new ModelIdentifier (new Identifier (AlchymReference.MODID,
                            String.format ("%s_flask_layer", material.getName ())), "inventory"));
            }
        });

        // Speedloader models
        ModelLoadingRegistry.INSTANCE.registerModelProvider ((resourceManager, consumer) -> {
            consumer.accept (new ModelIdentifier (new Identifier (AlchymReference.MODID,
                    String.format ("empty_%s", AlchymReference.Items.SPEEDLOADER.getName ())), "inventory"));

            consumer.accept (new ModelIdentifier (new Identifier (AlchymReference.MODID,
                    String.format ("%s_base", AlchymReference.Items.SPEEDLOADER.getName ())), "inventory"));
        });

        ModelLoadingRegistry.INSTANCE.registerVariantProvider (rm -> (modelIdentifier, modelProviderContext) -> {
            if (modelIdentifier.getNamespace ().equals (AlchymReference.MODID)) {
                // Chymical flask
                if (modelIdentifier.getPath ().equals (AlchymReference.Items.CHYMICAL_FLASK.getName ())) {
                    return new ChymicalFlaskBakedModel ();
                } else if (modelIdentifier.getPath ().equals (AlchymReference.Items.SPEEDLOADER.getName ())) {
                    return new SpeedloaderBakedModel ();
                }
            }

            return null;
        });

        EntityRendererRegistry.INSTANCE.register (Alchym.content ().entities.revolverBullet, RevolverBulletEntityRenderer::new);

        // Particles
        Alchym.content ().particles.initialize ();

        // Clientbound packets
        Alchym.content ().clientPackets.initialize ();

        // Serverbound packets
        Alchym.content ().serverPackets.initialize ();

        ClientPlayNetworking.registerGlobalReceiver (RevolverBulletEntity.SPAWN_PACKET, ((client, handler, data, responseSender) -> {
            if (client.world == null)
                return;

            Vec3d spawnPos = new Vec3d (data.readDouble (), data.readDouble (), data.readDouble ());
            Vec3d spawnVel = new Vec3d (data.readDouble (), data.readDouble (), data.readDouble ());
            PlayerEntity player = client.world.getPlayerByUuid (data.readUuid ());

            if (player == client.player)
                return;

            float radius = 3.5f;
            float sway = 0.1f;

            RevolverBulletEntity clientBullet = new RevolverBulletEntity (
                    RevolverHelper.getBulletBehavior (true),
                    radius, player, client.world, spawnPos, spawnVel);
            client.execute (() -> {
                assert client.world != null;
                client.world.addEntity (client.world.random.nextInt (), clientBullet);
            });
        }));
    }

    @Override
    public void renderPage (MatrixStack stack, BookPage page, AlchymReference.PageInfo.BookSide side, int bookProgress) {
        assert pageRenderDispatcher != null;

        if (page == null)
            return;

        pageRenderDispatcher.render (stack, page, side, bookProgress);
    }
}
