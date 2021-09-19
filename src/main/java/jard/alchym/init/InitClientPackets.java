package jard.alchym.init;

import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.api.transmutation.revolver.RevolverBehavior;
import jard.alchym.client.gui.screen.GuidebookScreen;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import jard.alchym.helper.MovementHelper;
import jard.alchym.helper.RevolverHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.impl.networking.ServerSidePacketRegistryImpl;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.UUID;
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

        PACKET_BEHAVIOR.put (AlchymReference.Packets.CLIENT_REPLAY.id,
                (packetContext, data) -> {
                    // rocket
                    float projectileSpeed = MovementHelper.upsToSpt (945.f);
                    float radius = 3.5f;
                    boolean firesBullet = true;
                    //*/
                    /* plasma
                    float projectileSpeed = MovementHelper.upsToSpt (945.f * 3.f);
                    float radius = 0.5f;
                    boolean firesBullet = true;
                    //*/
                    /* lightning
                    float projectileSpeed = 1.f;
                    float radius = 0.f;
                    boolean firesBullet = false;
                    //*/

                    PlayerEntity player = packetContext.getPlayer ();
                    ServerWorld world = (ServerWorld) player.world;
                    RevolverBehavior behavior = RevolverHelper.getBulletBehavior (world.isClient);

                    AlchymReference.RevolverAction action = data.readEnumConstant (AlchymReference.RevolverAction.class);
                    UUID bulletId = data.readBoolean () ? data.readUuid () : null;
                    Vec3d spawnPos = new Vec3d (data.readDouble (), data.readDouble (), data.readDouble ());
                    Vec3d velocity = new Vec3d (data.readDouble (), data.readDouble (), data.readDouble ())
                            .normalize ().multiply (projectileSpeed);

                    switch (action) {
                        case SPLASH -> {
                            List <RevolverBulletEntity> bullets = (List <RevolverBulletEntity>) world.getEntitiesByType (
                                    TypeFilter.instanceOf (RevolverBulletEntity.class),
                                    bullet -> bullet.getBulletId ().equals (bulletId));
                            if (bullets.size () != 1)
                                return;
                            if (bullets.get (0).owner != player)
                                return;

                            Vec3d normal = new Vec3d (data.readDouble (), data.readDouble (), data.readDouble ());

                            List<LivingEntity> entitiesInRange = RevolverHelper.validateEntitiesInRange (world, data, spawnPos, radius);

                            RevolverHelper.playSplash (
                                    behavior.splash (),
                                    bulletId,
                                    player, world,
                                    entitiesInRange,
                                    spawnPos,
                                    velocity,
                                    spawnPos,
                                    normal,
                                    radius, data);
                        }
                        case DIRECT -> {
                            List <RevolverBulletEntity> bullets = (List <RevolverBulletEntity>) world.getEntitiesByType (
                                    TypeFilter.instanceOf (RevolverBulletEntity.class),
                                    bullet -> bullet.getBulletId ().equals (bulletId));
                            if (bullets.size () != 1)
                                return;
                            if (bullets.get (0).owner != player)
                                return;

                            LivingEntity target = (LivingEntity) world.getEntity (data.readUuid ());

                            if (target == null)
                                return;

                            List<LivingEntity> entitiesInRange = RevolverHelper.validateEntitiesInRange (world, data, spawnPos, radius);

                            RevolverHelper.playDirect (
                                    behavior.direct (),
                                    behavior.splash (),
                                    bulletId,
                                    player, world,
                                    target, entitiesInRange,
                                    spawnPos, velocity, radius, data);
                        }
                        case BULLET -> {
                            // No-op
                        }
                    }
                });
    }

    @Override
    public void initialize () {
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
