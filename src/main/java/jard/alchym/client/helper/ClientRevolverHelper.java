package jard.alchym.client.helper;

import jard.alchym.client.QuakeKnockbackable;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import jard.alchym.helper.MovementHelper;
import jard.alchym.helper.TransmutationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/***
 *  ClientRevolverHelper
 *  Helper functions for handling the client-sided revolver attack impulse.
 *
 *  Created by jard at 21:42 on August 23, 2021.
 ***/
public class ClientRevolverHelper {
    @Environment (EnvType.CLIENT)
    public static void handleClientRevolver (PlayerEntity player, Vec3d eyePos, Vec3d aimDir) {
        // TODO: Projectile info and behavior needs to be passed in from RevolverItem
        // rocket
        float projectileSpeed = MovementHelper.upsToSpt (865.f);
        float radius = 2.5f;
        double verticalKnockback = MovementHelper.upsToSpt (755.f);
        double horizontalKnockback = MovementHelper.upsToSpt (555.f);
        boolean skim = true;
        boolean icy = false;
        //*/
        /* plasma
        float projectileSpeed = MovementHelper.upsToSpt (975.f * 2.f);
        float radius = 1.0f;
        double verticalKnockback = MovementHelper.upsToSpt (149.29f);
        double horizontalKnockback = MovementHelper.upsToSpt (48.75f);
        boolean skim = false;
        boolean icy = true;
        //*/
        /* lightning
        float projectileSpeed = MovementHelper.upsToSpt (975.f * 15.3f);
        float radius = 15.3f;
        double verticalKnockback = MovementHelper.upsToSpt (149.29f);
        double horizontalKnockback = MovementHelper.upsToSpt (97.5f);
        boolean skim = false;
        boolean icy = false;
        //*/

        Vec3d initialSpawnPos = aimDir.multiply (projectileSpeed * 2.f).add (eyePos);

        // Trace from player eye pos to projectile spawn position
        BlockHitResult cast = player.world.raycast (new RaycastContext (eyePos, initialSpawnPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, player));

        Vec3d spawnPos = cast.getType () != HitResult.Type.MISS ? cast.getPos () : initialSpawnPos;

        if (cast.getType () == HitResult.Type.BLOCK)
            spawnPos = TransmutationHelper.bumpFromSurface (cast, radius);
        if (cast.getType() != HitResult.Type.MISS) {
            ((QuakeKnockbackable) player).radialKnockback (spawnPos, radius, verticalKnockback, horizontalKnockback, skim, icy);
            player.world.addParticle (ParticleTypes.EXPLOSION, spawnPos.x, spawnPos.y, spawnPos.z, 0., 0., 0.);
        } else {
            RevolverBulletEntity clientBullet = new RevolverBulletEntity (player.world, spawnPos, aimDir.normalize ().multiply (projectileSpeed));
            ((ClientWorld) player.world).addEntity (player.world.random.nextInt (), clientBullet);
        }
    }
}
