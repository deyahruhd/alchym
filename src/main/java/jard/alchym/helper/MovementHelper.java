package jard.alchym.helper;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/***
 *  MovementHelper
 *
 *  Quake movement helper functions.
 *
 *  Created by jard at 02:09 on June, 14, 2021.
 ***/
public class MovementHelper {
    public static void playerAccelerate (ClientPlayerEntity player, Vec3d wishdir, float wishspeed, float accel) {
        float currentspeed = (float) player.getVelocity ().dotProduct (wishdir);

        float addspeed = wishspeed - currentspeed;
        if (addspeed <= 0.f) {
            return;
        }
        float accelspeed = accel * wishspeed;
        if (accelspeed > addspeed) {
            accelspeed = addspeed;
        }

        wishdir = wishdir.multiply (accelspeed);

        player.addVelocity (wishdir.x, wishdir.y, wishdir.z);
    }

    public static void playerFriction (ClientPlayerEntity player, float friction, float stopSpeed) {
        if (friction <= 0.f)
            return;

        Vec3d vel = player.getVelocity ();

        double speed = vel.multiply (1.f, 0.f, 1.f).length ();

        if (speed <= 0.f)
            return;

        double control = speed < stopSpeed ? stopSpeed : speed;
        double drop = control * friction;

        double newspeed = speed - drop;
        newspeed = newspeed < 0 ? 0 : newspeed;

        newspeed /= speed;

        Vec3d scaledVelocity = vel.multiply (newspeed);

        player.setVelocity (new Vec3d (scaledVelocity.x, vel.y, scaledVelocity.z));
    }

    public static Vec3d getWishDir (double yaw, Vec3d movementIn) {
        Vec3d forwardsComponent = new Vec3d (-Math.sin (yaw * Math.PI / 180.0), 0, Math.cos (yaw * Math.PI / 180.0));
        Vec3d sideComponent = new Vec3d (0, 1, 0).crossProduct (forwardsComponent);

        return forwardsComponent.multiply (movementIn.z).add (sideComponent.multiply (movementIn.x)).normalize ();
    }

    // Quake units per second to Minecraft speed per tick
    // Quake walk speed = 320/s corresponds with Minecraft sprint speed = 5.612/s
    public static float upsToSpt (float ups) {
        return ups * 5.612f / 320.f / 20.0f;
    }
}
