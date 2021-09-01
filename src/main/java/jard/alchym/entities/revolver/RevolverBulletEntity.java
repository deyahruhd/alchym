package jard.alchym.entities.revolver;

import jard.alchym.Alchym;
import jard.alchym.client.QuakeKnockbackable;
import jard.alchym.helper.MathHelper;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
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
    public final Vec3d clientStartOffset;

    public RevolverBulletEntity (EntityType <Entity> type, World world) {
        super (type, world);

        clientStartOffset = Vec3d.ZERO;
        kill ();
    }

    public RevolverBulletEntity (World world, Vec3d spawnPos, Vec3d clientStartPos, Vec3d vel) {
        super (Alchym.content ().entities.revolverBullet, world);

        this.clientStartOffset = clientStartPos.subtract (spawnPos);
        setBoundingBox (new Box (0.D, 0.D, 0.D, 0.D, 0.D, 0.D));
        setPos (spawnPos.x, spawnPos.y, spawnPos.z);
        setVelocity (vel);
    }

    public Vec3d getClientStartOffset (float tickDelta) {
        float smoothAge = (float) age + tickDelta;
        return MathHelper.lerp (clientStartOffset, Vec3d.ZERO, net.minecraft.util.math.MathHelper.clamp (Math.tanh (smoothAge / 3.f), 0.f, 1.f));
    }

    @Override
    public void tick () {
        if (! world.isClient)
            return;

        if (random.nextDouble () < 0.5) {
            Vec3d particlePos = this.getPos ().add (this.getVelocity ().multiply (Math.random () * 0.35)).add (getClientStartOffset (MinecraftClient.getInstance ().getTickDelta ()));
            Vec3d particleVel = new Vec3d (0.0, 0.015, 0.0)
                    .add (new Vec3d (random.nextDouble () - 0.5, random.nextDouble () - 0.5, random.nextDouble () - 0.5).normalize ().crossProduct (this.getVelocity ()).multiply (Math.random () * 0.015));
            world.addParticle (ParticleTypes.SNOWFLAKE, true,
                    particlePos.x, particlePos.y, particlePos.z,
                    particleVel.x, particleVel.y, particleVel.z);
        }

        // Trace from player eye pos to projectile spawn position
        BlockHitResult cast = world.raycast (new RaycastContext (this.getPos (), this.getPos ().add (this.getVelocity ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, this));

        if (cast.getType () == HitResult.Type.BLOCK) {
            // TODO: Move all this into the bullet object
            /*
            float radius = 3.5f;
            double verticalKnockback = MovementHelper.upsToSpt (755.f);
            double horizontalKnockback = MovementHelper.upsToSpt (555.f);
            boolean skim = true;
            boolean icy = false;*/
            float radius = 1.0f;
            double verticalKnockback = MovementHelper.upsToSpt (149.29f);
            double horizontalKnockback = MovementHelper.upsToSpt (48.75f);
            boolean skim = false;
            boolean icy = true;

            Vec3d castPos = TransmutationHelper.bumpFromSurface (cast, 15.f);
            Vec3d hitPos  = TransmutationHelper.bumpFromSurface (cast, radius);

            for (int i = 0; i < 10; ++ i) {
                double magnitude = 1. / (double) (i + 1);
                Vec3d particleVel = new Vec3d (0.0, 0.005, 0.0)
                        .add (new Vec3d (cast.getSide ().getUnitVector ()).multiply (0.125))
                        .add (new Vec3d (
                                random.nextDouble () - 0.5,
                                random.nextDouble () - 0.5,
                                random.nextDouble () - 0.5).normalize ().crossProduct (this.getVelocity ())
                                .multiply (Math.random () * 0.15 * magnitude))
                        .add (this.getVelocity ().multiply (0.10));
                world.addParticle (ParticleTypes.SNOWFLAKE, true,
                        castPos.x, castPos.y, castPos.z,
                        particleVel.x, particleVel.y, particleVel.z);
            }

            ((QuakeKnockbackable) MinecraftClient.getInstance ().player).radialKnockback (hitPos, radius, verticalKnockback, horizontalKnockback, skim, icy);
            kill ();
        }

        Vec3d preVel = this.getVelocity ();
        this.move (MovementType.SELF, this.getVelocity ());
        this.setVelocity (preVel);

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

    @Override
    public boolean shouldRender (double x, double y, double z) {
        return true;
    }
}
