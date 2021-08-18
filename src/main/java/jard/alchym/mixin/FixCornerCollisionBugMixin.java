package jard.alchym.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 *  FixCornerCollisionBugMixin
 *  Fixes a corner-case velocity vanilla bug. This isn't significant in vanilla, but can
 *  easily break mods which can apply large amounts of velocity to the player (e.g. Alchym).
 *
 *  See: https://bugs.mojang.com/browse/MC-159952
 *
 *  Created by jard at 15:37 on August 18, 2021.
 ***/
@Mixin (Entity.class)
public abstract class FixCornerCollisionBugMixin implements Nameable, EntityLike, CommandOutput {
    @Shadow
    protected Vec3d adjustMovementForSneaking (Vec3d movement, MovementType type) { return Vec3d.ZERO; }
    @Shadow
    private Vec3d adjustMovementForCollisions (Vec3d movement) { return Vec3d.ZERO; }
    @Shadow
    public void setVelocity (double x, double y, double z) {}
    @Shadow
    public Vec3d getVelocity() { return Vec3d.ZERO; }

    @Inject (
            method = "move",
            at = @At (
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;setVelocity(DDD)V",
                    shift = At.Shift.BY,
                    by = 1
            )
    )
    public void fixXZCollisionWallBug (MovementType movementType, Vec3d movement, CallbackInfo info) {
        movement = adjustMovementForSneaking (movement, movementType);
        Vec3d clippedMove = adjustMovementForCollisions (movement);
        Vec3d vel = getVelocity ();

        // Execution is currently in the if block that checks if movement.z != vec3d.z
        // If it also turns out that movement.x != vec3d.x, then the X-component of the
        // velocity gets overridden with a non-zero value, even though it should've been clipped
        // by the previous if statement
        if (movement.x != clippedMove.x)
            setVelocity (0.D, vel.y, 0.D);
    }
}
