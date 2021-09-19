package jard.alchym.helper;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.transmutation.revolver.RevolverBehavior;
import jard.alchym.api.transmutation.revolver.RevolverBulletTravelFunction;
import jard.alchym.api.transmutation.revolver.RevolverDirectHitFunction;
import jard.alchym.api.transmutation.revolver.RevolverSplashHitFunction;
import jard.alchym.client.QuakeKnockbackable;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.impl.networking.ClientSidePacketRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/***
 *  RevolverHelper
 *  Contains various helper methods relating to revolver transmutation (e.g. client <=> server and server <=> client revolver actions)
 *
 *  Created by jard at 13:38 on September 16, 2021.
 ***/
public class RevolverHelper {
    public static RevolverBehavior getBulletBehavior (boolean isClient) {
        // rocket
        float directDamage = 20.f;
        float splashDamage = 13.f;
        double verticalKnockback = MovementHelper.upsToSpt (755.f);
        double horizontalKnockback = MovementHelper.upsToSpt (555.f);
        boolean skim = true;
        boolean icy = false;
        //*/
        /* plasma
        float directDamage = 3.f;
        float splashDamage = 2.f;
        double verticalKnockback = MovementHelper.upsToSpt (149.29f);
        double horizontalKnockback = MovementHelper.upsToSpt (48.75f);
        boolean skim = false;
        boolean icy = true;
        //*/
        /* lightning
        double verticalKnockback = MovementHelper.upsToSpt (77.5f);
        double horizontalKnockback = MovementHelper.upsToSpt (125.5f);
        boolean skim = false;
        boolean icy = true;
        //*/
        RevolverDirectHitFunction direct = isClient ?
                (player, target, hitPos, vel, random) -> {} :
                (player, target, hitPos, vel, random) -> {
                    Vec3d knockback = target.getPos ()
                            .add (0., target.getEyeHeight (target.getPose ()) / 2., 0.)
                            .subtract (MovementHelper.getKnockbackTo (target, hitPos)).normalize ();

                    if (! target.isOnGround ()) {
                        knockback = vel.normalize ().multiply (0.5, 1., 0.5);
                    }

                    knockback = knockback.multiply (2. * horizontalKnockback, 2. * verticalKnockback, 2. * horizontalKnockback);

                    Vec3d preVel = target.getVelocity ();
                    target.damage (DamageSource.explosion (player), directDamage);
                    target.setVelocity (preVel);
                    target.addVelocity (knockback.x, knockback.y, knockback.z);
                    target.timeUntilRegen = 0;
                };

        RevolverSplashHitFunction splash = isClient ?
                (player, world, radius, vel, hitPos, hitNormal, visualPos, random, targets) -> {
                    if (world.isClient && targets.length == 1) {
                        ClientPlayerEntity target = (ClientPlayerEntity) targets[0];
                        ((QuakeKnockbackable) target).radialKnockback (hitPos, radius, verticalKnockback, horizontalKnockback, skim, icy);
                    }

                    // rocket
                    world.addParticle (ParticleTypes.EXPLOSION, true, visualPos.x, visualPos.y, visualPos.z, 0., 0., 0.);
                    world.playSound (hitPos.x, hitPos.y, hitPos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 8.f, 0.66f, false);
                    //*/
                    /* plasma
                    for (int i = 0; i < 10; ++ i) {
                        double magnitude = 1. / (double) (i + 1);
                        Vec3d particleVel = new Vec3d (0.0, 0.005, 0.0)
                                .add (hitNormal.multiply (0.125))
                                .add (new Vec3d (
                                        random.nextDouble () - 0.5,
                                        random.nextDouble () - 0.5,
                                        random.nextDouble () - 0.5).normalize ().crossProduct (vel)
                                        .multiply (random.nextDouble () * 0.15 * magnitude))
                                .add (vel.multiply (0.10));
                        world.addParticle (ParticleTypes.SNOWFLAKE, true,
                                visualPos.x, visualPos.y, visualPos.z,
                                particleVel.x, particleVel.y, particleVel.z);
                    }
                    world.playSound (hitPos.x, hitPos.y, hitPos.z, SoundEvents.BLOCK_SMALL_AMETHYST_BUD_BREAK, SoundCategory.MASTER, 4.f, 1.f + (float) random.nextDouble () * 0.05f, false);
                    world.playSound (hitPos.x, hitPos.y, hitPos.z, SoundEvents.BLOCK_POWDER_SNOW_HIT, SoundCategory.MASTER, 2.f, 0.66f + (float) random.nextDouble () * 0.33f, false);
                    //*/
                } :
                (player, world, radius, vel, hitPos, hitNormal, visualPos, random, targets) -> {
                    for (LivingEntity target : targets) {
                        Vec3d dir = target.getPos ().add (0.0, target.getEyeHeight (target.getPose ()), 0.0).subtract (hitPos);
                        float dist = (float) MovementHelper.getKnockbackTo (target, hitPos).subtract (hitPos).length ();
                        float scale = MathHelper.softcorePotential (dist / radius, 10.f, 1.f);

                        if (! (target instanceof PlayerEntity)) {
                            dir = dir.normalize ().multiply (2. * horizontalKnockback * scale, 2. * verticalKnockback * scale, 2. * horizontalKnockback * scale);
                            target.addVelocity (dir.x, dir.y, dir.z);
                        }

                        if (dist <= 2. * radius) {
                            Vec3d prevVel = target.getVelocity ();
                            target.damage (DamageSource.explosion (player), splashDamage * scale);
                            target.setVelocity (prevVel);
                            target.timeUntilRegen = 0;
                        }
                    }
                    //*/
                };

        RevolverBulletTravelFunction travel = isClient ?
                (bullet, pos, random) -> {
                    Vec3d velocity = bullet.getVelocity ();

                    // rocket
                    if (bullet.age > 3) {
                        final int count = Math.min (bullet.age - 3, 4);

                        for (int i = 0; i < count; ++ i) {
                            double subTick = (double) i / (double) count;
                            Vec3d particlePos = pos.add (velocity.multiply (subTick - 0.9 + (subTick * subTick) * 0.5));
                            Vec3d particleVel = new Vec3d (random.nextDouble () - 0.5,
                                    random.nextDouble () - 0.5,
                                    random.nextDouble () - 0.5)
                                    .normalize ()
                                    .crossProduct (velocity)
                                    .multiply (Math.random () * 0.015)
                                    .add (velocity.multiply (0.55));
                            bullet.world.addParticle (Alchym.content ().particles.fireTrail, true,
                                    particlePos.x, particlePos.y, particlePos.z,
                                    particleVel.x, particleVel.y, particleVel.z);
                        }
                    }
                    //*/
                    /* plasma
                    if (random.nextDouble () < 0.5) {
                        Vec3d particlePos = pos.add (velocity.multiply (Math.random () * 0.35));
                        Vec3d particleVel = new Vec3d (0.0, 0.015, 0.0)
                                .add (new Vec3d (random.nextDouble () - 0.5, random.nextDouble () - 0.5, random.nextDouble () - 0.5).normalize ().crossProduct (velocity).multiply (Math.random () * 0.015));
                        bullet.world.addParticle (ParticleTypes.SNOWFLAKE, true,
                                particlePos.x, particlePos.y, particlePos.z,
                                particleVel.x, particleVel.y, particleVel.z);
                    }
                    //*/
                } : (bullet, pos, random) -> {};

        return new RevolverBehavior (direct, splash, travel);
    }

    public static List<LivingEntity> getEntitiesInRange (World world, @Nullable LivingEntity target, Vec3d hitPos, float radius) {
        return world.getEntitiesByType (
                TypeFilter.instanceOf (LivingEntity.class),
                new Box (hitPos.subtract (2. * radius, 2. * radius, 2. * radius), hitPos.add (2. * radius, 2. * radius, 2. * radius)),
                livingEntity -> MovementHelper.getKnockbackTo (livingEntity, hitPos).subtract (hitPos).lengthSquared () <= (4. * radius * radius) && livingEntity != target);
    }

    public static List<LivingEntity> validateEntitiesInRange (ServerWorld world, PacketByteBuf data, Vec3d hitPos, float radius) {
        List<LivingEntity> entitiesInRange = new ArrayList <> ();
        final int count = data.readInt ();
        for (int i = 0; i < count; ++ i) {
            Entity entity = world.getEntity (data.readUuid ());
            if (entity instanceof LivingEntity
                    // Double check this, as the client could select all loaded entities
                    && MovementHelper.getKnockbackTo (entity, hitPos).subtract (hitPos).lengthSquared () <= (4. * radius * radius))
                entitiesInRange.add ((LivingEntity) entity);
        }

        return entitiesInRange;
    }

    public static void playSplash (RevolverSplashHitFunction splash, @Nullable UUID bulletId, @Nullable PlayerEntity player, World world, List<LivingEntity> entitiesInRange, Vec3d splashPos, Vec3d velocity, Vec3d visualPos, Vec3d normal, float radius, @Nullable PacketByteBuf data) {
        List <LivingEntity> splashedEntities = entitiesInRange.stream().filter (entity -> {
            if (entity.world.isClient)
                return entity == MinecraftClient.getInstance ().player;
            else
                return true;
        }).toList ();

        splash.apply (player, world, radius, velocity, splashPos, normal, visualPos, world.getRandom (), splashedEntities.toArray (new LivingEntity [0]));

        if (data != null) {
            data.clear ();
            // write data for replay
            data.writeEnumConstant (AlchymReference.RevolverAction.SPLASH);
            if (bulletId == null)
                data.writeBoolean (false);
            else {
                data.writeBoolean (true);
                data.writeUuid (bulletId);
            }
            // splash position
            data.writeDouble (splashPos.x);
            data.writeDouble (splashPos.y);
            data.writeDouble (splashPos.z);
            // player velocity (normalized on the receiving side)
            data.writeDouble (velocity.x);
            data.writeDouble (velocity.y);
            data.writeDouble (velocity.z);
            // block normal
            data.writeDouble (normal.x);
            data.writeDouble (normal.y);
            data.writeDouble (normal.z);
            // number of entities to splash
            data.writeInt (entitiesInRange.size ());
            for (Entity e : entitiesInRange) {
                data.writeUuid (e.getUuid ());
            }
        }
    }

    public static void playDirect (RevolverDirectHitFunction direct, RevolverSplashHitFunction splash, @Nullable UUID bulletId, @Nullable PlayerEntity player, World world, LivingEntity target, List<LivingEntity> entitiesInRange, Vec3d hitPos, Vec3d velocity, float radius, @Nullable PacketByteBuf data) {
        List <LivingEntity> splashedEntities = entitiesInRange.stream().filter (entity -> {
            if (entity.world.isClient)
                return entity == MinecraftClient.getInstance ().player;
            else
                return true;
        }).toList ();

        direct.apply (player, target, hitPos, velocity, world.getRandom ());
        splash.apply (player, world, radius, velocity, hitPos, velocity, hitPos, world.getRandom (), splashedEntities.toArray (new LivingEntity[0]));

        if (data != null) {
            data.clear ();
            // write data for server replay
            data.writeEnumConstant (AlchymReference.RevolverAction.DIRECT);
            // write bullet Id
            if (bulletId == null)
                data.writeBoolean (false);
            else {
                data.writeBoolean (true);
                data.writeUuid (bulletId);
            }
            // splash position
            data.writeDouble (hitPos.x);
            data.writeDouble (hitPos.y);
            data.writeDouble (hitPos.z);
            // player aim direction (to calculate velocity)
            data.writeDouble (velocity.x);
            data.writeDouble (velocity.y);
            data.writeDouble (velocity.z);
            // directed entity
            data.writeUuid (target.getUuid ());
            // number of entities to splash
            data.writeInt (entitiesInRange.size ());
            for (Entity e : entitiesInRange) {
                data.writeUuid (e.getUuid ());
            }
        }
    }
}
