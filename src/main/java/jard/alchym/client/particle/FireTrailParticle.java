package jard.alchym.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/***
 *  FireTrailParticle
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 02:45 on September 10, 2021.
 ***/
@Environment (EnvType.CLIENT)
public class FireTrailParticle extends AnimatedParticle {
    protected FireTrailParticle (ClientWorld world, Vec3d pos, Vec3d vel, SpriteProvider spriteProvider) {
        super (world, pos.x, pos.y, pos.z, spriteProvider, 0);

        this.setVelocity (vel.x, vel.y, vel.z);
        this.angle = (float) (random.nextGaussian () * Math.PI);
        this.prevAngle = angle;
        this.maxAge = 11 + random.nextInt (13);
        this.scale = 0.18f + random.nextFloat () * 0.04f;
        this.setTargetColor (15916745);
        this.setSpriteForAge (spriteProvider);
    }

    @Override
    public void tick() {
        super.tick ();

        this.scale = MathHelper.lerp ((float) age / (float) maxAge, 0.12f, 0.55f);
        this.setVelocity (velocityX * 0.98, velocityY * 0.98, velocityZ * 0.98);
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleFactory <DefaultParticleType> {
        private final SpriteProvider sprites;

        public DefaultFactory (SpriteProvider sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle (DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new FireTrailParticle (world, new Vec3d (x, y, z), new Vec3d (velocityX, velocityY, velocityZ), sprites);
        }
    }
}
