package jard.alchym.init;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.api.transmutation.revolver.RevolverBehavior;
import jard.alchym.api.transmutation.revolver.RevolverBulletTravelFunction;
import jard.alchym.api.transmutation.revolver.RevolverDirectHitFunction;
import jard.alchym.api.transmutation.revolver.RevolverSplashHitFunction;
import jard.alchym.client.gui.screen.GuidebookScreen;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import jard.alchym.helper.MovementHelper;
import jard.alchym.helper.RevolverHelper;
import jard.alchym.helper.TransmutationHelper;
import jard.alchym.items.AlchymicReferenceItem;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.impl.networking.ServerSidePacketRegistryImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/***
 *  InitServerPackets
 *  The initializing module that initializes all client-to-server packets.
 *
 *  Created by jard at 02:56 on September 14, 2021.
 ***/
public class InitServerPackets extends InitAbstractPackets {
    InitServerPackets (InitAlchym alchym) {
        super (alchym);

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

        PACKET_BEHAVIOR.put (AlchymReference.Packets.SERVER_REPLAY.id,
                (packetContext, data) -> {
                    if (packetContext.getPlayer ().isSpectator ())
                        return;

                    // rocket
                    float projectileSpeed = MovementHelper.upsToSpt (945.f);
                    float hitscanSpeed    = MovementHelper.upsToSpt (945.f * 2.f);
                    float radius = 3.5f;
                    float sway = 0.1f;
                    boolean firesBullet = true;
                    //*/
                    /* plasma
                    float projectileSpeed = MovementHelper.upsToSpt (945.f * 3.f);
                    float hitscanSpeed    = MovementHelper.upsToSpt (945.f * 2.0f);
                    float radius = 0.5f;
                    float sway = 0.05f;
                    boolean firesBullet = true;
                    //*/
                    /* lightning
                    float projectileSpeed = 1.f;
                    float hitscanSpeed    = 32.f;
                    float radius = 0.f;
                    float sway = 0.f;
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

                            if (bulletId == null)
                                for (PlayerEntity client : world.getPlayers (client -> client != player)) {
                                    ServerSidePacketRegistryImpl.INSTANCE.sendToPlayer (client, AlchymReference.Packets.CLIENT_REPLAY.id, data);
                                }
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

                            if (bulletId == null)
                                for (PlayerEntity client : world.getPlayers (client -> client != player)) {
                                    ServerSidePacketRegistryImpl.INSTANCE.sendToPlayer (client, AlchymReference.Packets.CLIENT_REPLAY.id, data);
                                }
                        }
                        case BULLET -> {
                            if (! firesBullet)
                                return;

                            RevolverBulletEntity serverBullet = new RevolverBulletEntity (
                                    player.world,
                                    behavior,
                                    player, bulletId,
                                    velocity, spawnPos, radius);
                            packetContext.getTaskQueue ().execute (() -> player.world.spawnEntity (serverBullet));
                        }
                    }
                });
    }

    @Override
    public void initialize () {
        Stream<AlchymReference.Packets> serverboundPackets = Stream.of (AlchymReference.Packets.values ()).filter (AlchymReference.Packets::isServerbound);

        serverboundPackets.forEach (
                (packet) -> {
                    PacketConsumer action = PACKET_BEHAVIOR.get (packet.id);
                    ServerSidePacketRegistry.INSTANCE.register (packet.id,
                            (packetContext, packetByteBuf) -> {
                                final PacketByteBuf data = new PacketByteBuf (packetByteBuf.copy ());
                                packetContext.getTaskQueue ().execute (() -> action.accept (packetContext, data));
                            });
                });
    }
}
