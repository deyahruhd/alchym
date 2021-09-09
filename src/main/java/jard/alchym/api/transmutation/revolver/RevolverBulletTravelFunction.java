package jard.alchym.api.transmutation.revolver;

import jard.alchym.entities.revolver.RevolverBulletEntity;
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
    void apply (RevolverBulletEntity bullet, Vec3d pos, Random random);
}
