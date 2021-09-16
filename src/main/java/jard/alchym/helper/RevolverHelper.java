package jard.alchym.helper;

import jard.alchym.Alchym;
import jard.alchym.api.transmutation.revolver.RevolverBehavior;
import jard.alchym.api.transmutation.revolver.RevolverBulletTravelFunction;
import jard.alchym.api.transmutation.revolver.RevolverDirectHitFunction;
import jard.alchym.api.transmutation.revolver.RevolverSplashHitFunction;
import jard.alchym.client.QuakeKnockbackable;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

/***
 *  RevolverHelper
 *  Contains various helper methods relating to revolver transmutation (e.g. client <=> server and server <=> client revolver actions)
 *
 *  Created by jard at 13:38 on September 16, 2021.
 ***/
public class RevolverHelper {
    public static RevolverBehavior getBulletBehavior (boolean isClient) {
        // rocket
        double verticalKnockback = MovementHelper.upsToSpt (755.f);
        double horizontalKnockback = MovementHelper.upsToSpt (555.f);
        boolean skim = true;
        boolean icy = false;
        //*/
        /* plasma
        double verticalKnockback = MovementHelper.upsToSpt (149.29f);
        double horizontalKnockback = MovementHelper.upsToSpt (48.75f);
        boolean skim = false;
        boolean icy = true;
        //*/

        RevolverDirectHitFunction direct = isClient ?
                (player, target, hitPos, vel, random) -> {} :
                (player, target, hitPos, vel, random) -> {
                    Vec3d knockback = vel.normalize ().multiply (horizontalKnockback, verticalKnockback, horizontalKnockback);

                    Vec3d preVel = target.getVelocity ();
                    target.damage (DamageSource.explosion (player), 20.0f);
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
                        float dist = (float) dir.length ();
                        float scale = MathHelper.softcorePotential (dist / radius, 10.f, 1.f);

                        if (! (target instanceof PlayerEntity)) {
                            dir = dir.normalize ().multiply (horizontalKnockback * scale, verticalKnockback * scale, horizontalKnockback * scale);
                            target.addVelocity (dir.x * 2.f, dir.y * 2.f, dir.z * 2.f);
                        }

                        if (dist <= 2. * radius) {
                            Vec3d prevVel = target.getVelocity ();
                            target.damage (DamageSource.explosion (player), 16.f * scale);
                            target.setVelocity (prevVel);
                            target.timeUntilRegen = 0;
                        }
                    }
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
}
