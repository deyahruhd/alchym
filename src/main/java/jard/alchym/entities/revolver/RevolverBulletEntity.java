package jard.alchym.entities.revolver;

import jard.alchym.Alchym;
import jard.alchym.client.QuakeKnockbackable;
import jard.alchym.helper.MovementHelper;
import jard.alchym.helper.TransmutationHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

/***
 *  RevolverBulletEntity
 *  The revolver spellcast projectile entity.
 *
 *  Has unique behavior either on the client side or server side:
 *  - Client: Bullets spawn immediately, desynced from the server. Only applies knockback on the player.
 *            Synchronization steps are taken to make sure the projectile doesn't deviate too far from the server.
 *  - Server: Bullets spawn from a packet originating from the client. Applies knockback on non-player entities, and
 *            applies damage to all entities.
 *
 *  This is intentionally to make client-sided knockback via projectiles feel much smoother, since the Minecraft
 *  server can't be trusted to respond in real time to fire impulses.
 *
 *  Created by jard at 15:33 on August 23, 2021.
 ***/
public class RevolverBulletEntity extends Entity {
    public RevolverBulletEntity (EntityType <Entity> type, World world) {
        super (type, world);

        System.out.println ("Constructing 1");

        kill ();
    }

    public RevolverBulletEntity (World world, Vec3d spawnPos, Vec3d vel) {
        super (Alchym.content ().entities.revolverBullet, world);

        System.out.println ("Constructing 2");

        setPos (spawnPos.x, spawnPos.y, spawnPos.z);
        setVelocity (vel);
    }

    @Override
    public void tick () {
        if (! world.isClient)
            return;

        for (int i = 0; i < 3; ++ i) {
            if (this.age == 1)
                i = 3;

            double subTicks = -(3. - i) / 3.;
            double pseudoSmoothTime = this.getUuid ().hashCode () + this.age + subTicks;
            double piSinTime = Math.PI * Math.sin (pseudoSmoothTime * 0.1);
            piSinTime *= piSinTime;
            Vec3d randomTransverseDir = new Vec3d (
                    Math.cos  (piSinTime * 0.3)  * 0.3,
                    Math.sin  (piSinTime * 0.23) * 0.5,
                    Math.tanh (piSinTime * 0.89) * 0.7).normalize ().crossProduct (this.getVelocity ().normalize ());
            Vec3d particlePos = this.getPos ().add (this.getVelocity ().multiply (subTicks));
            Vec3d particleVel = randomTransverseDir.multiply (0.075)
                    .add (new Vec3d (0.0, 0.005, 0.0));
            world.addParticle (ParticleTypes.LARGE_SMOKE, true,
                    particlePos.x, particlePos.y, particlePos.z,
                    particleVel.x, particleVel.y, particleVel.z);
        }

        // Trace from player eye pos to projectile spawn position
        BlockHitResult cast = world.raycast (new RaycastContext (this.getPos (), this.getPos ().add (this.getVelocity ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, this));

        if (cast.getType () == HitResult.Type.BLOCK) {
            // TODO: Move all this into the bullet object
            float radius = 3.5f;
            double verticalKnockback = MovementHelper.upsToSpt (755.f);
            double horizontalKnockback = MovementHelper.upsToSpt (555.f);
            boolean skim = true;
            boolean icy = false;

            Vec3d hitPos = TransmutationHelper.bumpFromSurface (cast, radius);

            ((QuakeKnockbackable) MinecraftClient.getInstance ().player).radialKnockback (hitPos, radius, verticalKnockback, horizontalKnockback, skim, icy);
            world.addParticle (ParticleTypes.EXPLOSION, hitPos.x, hitPos.y, hitPos.z, 0., 0., 0.);
            kill ();
        }

        this.move (MovementType.SELF, this.getVelocity ());

        super.tick ();
    }

    @Override
    protected void initDataTracker () {
    }

    @Override
    protected void readCustomDataFromNbt (NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt (NbtCompound nbt) {

    }

    @Override
    public Packet <?> createSpawnPacket () {
        return null;
    }
}
