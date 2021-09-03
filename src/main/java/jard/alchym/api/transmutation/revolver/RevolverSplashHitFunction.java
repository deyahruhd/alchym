package jard.alchym.api.transmutation.revolver;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

/***
 *  RevolverSplashHitFunction
 *  Functional interface to handle multi-entity splash hit revolver effects (knockback impulse, damage, etc).
 *
 *  Created by jard at 19:34 on September 01, 2021.
 ***/
@FunctionalInterface
public interface RevolverSplashHitFunction {
    void apply (Vec3d vel, Vec3d hitPos, Vec3d hitNormal, Vec3d visualPos, World world, Random random, LivingEntity ... entities);
}
