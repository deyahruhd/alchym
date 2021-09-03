package jard.alchym.api.transmutation.revolver;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

/***
 *  RevolverTravelFunction
 *  Functional interface to handle bullet travel effects (particles, etc).
 *
 *  Created by jard at 19:38 on September 01, 2021.
 ***/
@FunctionalInterface
public interface RevolverBulletTravelFunction {
    void apply (Vec3d pos, Vec3d velocity, World world, Random random);
}
