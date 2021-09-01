package jard.alchym.client.helper;

import jard.alchym.client.QuakeKnockbackable;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import jard.alchym.helper.MovementHelper;
import jard.alchym.helper.TransmutationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;
import net.minecraft.world.RaycastContext;

/***
 *  ClientRevolverHelper
 *  Helper functions for handling the client-sided revolver attack impulse.
 *
 *  Created by jard at 21:42 on August 23, 2021.
 ***/
public class ClientRevolverHelper {
    @Environment (EnvType.CLIENT)
    public static void handleClientRevolver (ClientPlayerEntity player, ItemStack item, Arm arm, Vec3d eyePos, Vec3d aimDir) {
        // TODO: Projectile info and behavior needs to be passed in from RevolverItem
        /* rocket
        float projectileSpeed = MovementHelper.upsToSpt (945.f);
        float hitscanSpeed    = MovementHelper.upsToSpt (945.f * 2.f);
        float radius = 3.5f;
        double verticalKnockback = MovementHelper.upsToSpt (755.f);
        double horizontalKnockback = MovementHelper.upsToSpt (555.f);
        boolean skim = true;
        boolean icy = false;
        //*/
        // plasma
        float projectileSpeed = MovementHelper.upsToSpt (945.f * 3.f);
        float hitscanSpeed    = MovementHelper.upsToSpt (945.f * 2.0f);
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

        Vec3d initialSpawnPos = aimDir.multiply (hitscanSpeed).add (eyePos);

        // Trace from player eye pos to projectile spawn position
        BlockHitResult cast = player.world.raycast (new RaycastContext (eyePos, initialSpawnPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, player));

        Vec3d spawnPos = cast.getType () != HitResult.Type.MISS ? cast.getPos () : initialSpawnPos;
        Vec3d velocity = aimDir.normalize ().multiply (projectileSpeed);

        if (cast.getType () == HitResult.Type.BLOCK)
            spawnPos = TransmutationHelper.bumpFromSurface (cast, radius);
        if (cast.getType() != HitResult.Type.MISS) {
            Vec3d castPos = TransmutationHelper.bumpFromSurface (cast, 15.0);
            ((QuakeKnockbackable) player).radialKnockback (spawnPos, radius, verticalKnockback, horizontalKnockback, skim, icy);
            for (int i = 0; i < 10; ++ i) {
                double magnitude = 1. / (double) (i + 1);
                Vec3d particleVel = new Vec3d (0.0, 0.005, 0.0)
                        .add (new Vec3d (cast.getSide ().getUnitVector ()).multiply (0.125))
                        .add (new Vec3d (
                                player.getRandom ().nextDouble () - 0.5,
                                player.getRandom ().nextDouble () - 0.5,
                                player.getRandom ().nextDouble () - 0.5).normalize ().crossProduct (velocity)
                                .multiply (Math.random () * 0.15 * magnitude))
                        .add (velocity.multiply (0.10));
                player.world.addParticle (ParticleTypes.SNOWFLAKE, true,
                        castPos.x, castPos.y, castPos.z,
                        particleVel.x, particleVel.y, particleVel.z);
            }
        } else {
            // Calculate the eyespace bullet position
            Vec3d bulletStart = new Vec3d (0.0, 0.165, -0.38);

            Camera camera = MinecraftClient.getInstance ().gameRenderer.getCamera ();
            Matrix4f viewTransform = RenderHelper.getViewProjectMatrix (camera, MinecraftClient.getInstance ().options.fov).peek ().getModel ();
            viewTransform.invert ();
            Matrix4f revolverTransform = RenderHelper.getRevolverTransform (
                    item, player, arm,
                    MinecraftClient.getInstance ().getTickDelta (),
                    player.getHandSwingProgress (MinecraftClient.getInstance ().getTickDelta ())).peek ().getModel ();
            Matrix4f handTransform = RenderHelper.getHandTransform (item, player, arm).peek ().getModel ();

            Vector4f transformedStart = new Vector4f ((float) bulletStart.x, (float) bulletStart.y, (float) bulletStart.z, 1.f);
            transformedStart.transform (handTransform);
            transformedStart.transform (revolverTransform);
            transformedStart.transform (viewTransform);
            bulletStart = new Vec3d (transformedStart.getX (), transformedStart.getY (), transformedStart.getZ ());

            RevolverBulletEntity clientBullet = new RevolverBulletEntity (player.world, spawnPos, bulletStart, velocity);
            ((ClientWorld) player.world).addEntity (player.world.random.nextInt (), clientBullet);
        }
    }
}
