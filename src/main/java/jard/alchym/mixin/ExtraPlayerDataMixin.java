package jard.alchym.mixin;

import jard.alchym.client.ExtraPlayerDataAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Stack;

/***
 *  ExtraPlayerDataMixin
 *  Adds extra transient data to each PlayerEntity object (previous velocity, grapple)
 *
 *  Created by jard at 19:32 on January, 02, 2021.
 ***/
@Mixin (PlayerEntity.class)
public abstract class ExtraPlayerDataMixin extends LivingEntity implements ExtraPlayerDataAccess {
    Vec3d previousVel = Vec3d.ZERO;
    Stack<Vec3d> grappleLinks = new Stack<> ();
    double grappleLength = 0.0;

    protected ExtraPlayerDataMixin (EntityType<? extends LivingEntity> entityType, World world) {
        super (entityType, world);
    }

    @Inject (method = "tickMovement", at = @At ("HEAD"))
    private void tickMovement (CallbackInfo info) {
        previousVel = getVelocity ();
    }

    public Vec3d getPrevVel () {
        return previousVel;
    }

    public boolean isJumping () {
        return jumping;
    }

    public Stack<Vec3d> getGrapple () {
        return grappleLinks;
    }

    public double getGrappleLength () {
        return grappleLength;
    }
}
