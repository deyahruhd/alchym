package jard.alchym.api.transmutation.revolver;

import jard.alchym.entities.revolver.RevolverBulletEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

/***
 *  RevolverDirectHitFunction
 *  Functional interface to handle entity direct hit revolver effects (knockback impulse, damage, etc).
 *
 *  Created by jard at 19:33 on September 01, 2021.
 ***/
@FunctionalInterface
public interface RevolverDirectHitFunction {
    void apply (RevolverBulletEntity bullet, LivingEntity target, Vec3d velocity, Random random);
}
