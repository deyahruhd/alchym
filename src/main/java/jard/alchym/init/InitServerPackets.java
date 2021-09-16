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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
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

import java.util.List;
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

        PACKET_BEHAVIOR.put (AlchymReference.Packets.REVOLVER_ACTION.id,
                (packetContext, data) -> {
                    Vec3d eyePos = new Vec3d (data.readDouble (), data.readDouble (), data.readDouble ());
                    Vec3d aimDir = new Vec3d (data.readDouble (), data.readDouble (), data.readDouble ());
                    PlayerEntity player = packetContext.getPlayer ();

                    // rocket
                    float projectileSpeed = MovementHelper.upsToSpt (945.f);
                    float hitscanSpeed    = MovementHelper.upsToSpt (945.f * 2.f);
                    float radius = 3.5f;
                    boolean firesBullet = true;
                    //*/
                    /* plasma
                    float projectileSpeed = MovementHelper.upsToSpt (945.f * 3.f);
                    float hitscanSpeed    = MovementHelper.upsToSpt (945.f * 2.0f);
                    float radius = 0.5f;
                    boolean firesBullet = true;
                    //*/
                    /* lightning
                    float projectileSpeed = 1.f;
                    float hitscanSpeed = 24.f;
                    float radius = 0.f;
                    boolean firesBullet = false;
                    //*/

                    RevolverBehavior behavior = RevolverHelper.getBulletBehavior (false);

                    Vec3d initialSpawnPos = aimDir.normalize ().multiply (hitscanSpeed).add (eyePos);

                    // Trace from player eye pos to projectile spawn position
                    HitResult cast = TransmutationHelper.raycastEntitiesAndBlocks (player, player.world, eyePos, initialSpawnPos);

                    Vec3d spawnPos = cast.getType () != HitResult.Type.MISS ? cast.getPos () : initialSpawnPos;
                    Vec3d velocity = aimDir.normalize ().multiply (projectileSpeed);

                    if (cast.getType () == HitResult.Type.BLOCK) {
                        spawnPos = TransmutationHelper.bumpFromSurface ((BlockHitResult) cast, radius);
                        Vec3d splashPos = spawnPos;
                        Vec3d normal = new Vec3d (((BlockHitResult) cast).getSide ().getUnitVector ());

                        List <LivingEntity> affectedEntities = player.world.getEntitiesByType (
                                TypeFilter.instanceOf (LivingEntity.class),
                                new Box (spawnPos.subtract (2. * radius, 2. * radius, 2. * radius), spawnPos.add (2. * radius, 2. * radius, 2. * radius)),
                                livingEntity -> livingEntity.squaredDistanceTo (splashPos) <= (4. * radius * radius));

                        behavior.splash ().apply (player, player.world, radius, velocity, spawnPos, normal, spawnPos, player.getRandom (), affectedEntities.toArray (new LivingEntity[0]));
                    } else if (cast.getType () == HitResult.Type.ENTITY) {
                        LivingEntity target = (LivingEntity) ((EntityHitResult) cast).getEntity ();

                        List <LivingEntity> splashEntities = player.world.getEntitiesByType (
                                TypeFilter.instanceOf (LivingEntity.class),
                                new Box (spawnPos.subtract (2. * radius, 2. * radius, 2. * radius), spawnPos.add (2. * radius, 2. * radius, 2. * radius)),
                                livingEntity -> livingEntity.squaredDistanceTo (cast.getPos ()) <= (4. * radius * radius) && livingEntity != target );

                        behavior.direct ().apply (player, target, cast.getPos (), velocity, player.getRandom ());
                        behavior.splash ().apply (player, player.world, radius, velocity, spawnPos, velocity, spawnPos, player.getRandom (), splashEntities.toArray (new LivingEntity [0]));
                    } else if (firesBullet) {
                        RevolverBulletEntity serverBullet = new RevolverBulletEntity (behavior, radius, player, player.world, spawnPos, spawnPos, 0.f, velocity);
                        packetContext.getTaskQueue ().execute (() -> {
                            player.world.spawnEntity (serverBullet);
                        });
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
