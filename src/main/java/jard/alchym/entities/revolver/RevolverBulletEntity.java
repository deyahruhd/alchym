package jard.alchym.entities.revolver;

import jard.alchym.Alchym;
import jard.alchym.api.transmutation.revolver.RevolverBulletTravelFunction;
import jard.alchym.api.transmutation.revolver.RevolverDirectHitFunction;
import jard.alchym.api.transmutation.revolver.RevolverSplashHitFunction;
import jard.alchym.client.QuakeKnockbackable;
import jard.alchym.helper.MathHelper;
import jard.alchym.helper.MovementHelper;
import jard.alchym.helper.TransmutationHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.TypeFilter;
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
    private final RevolverDirectHitFunction direct;
    private final RevolverSplashHitFunction splash;
    private final RevolverBulletTravelFunction travel;
    private final float radius;
    private final float swayIntensity;
    public final int seed;

    public final Vec3d clientStartOffset;

    public RevolverBulletEntity (EntityType <Entity> type, World world) {
        super (type, world);

        direct = (bullet, target, vel, random) -> {};
        splash = (w, hitPos, hitNormal, visualPos, random, targets) -> {};
        travel = (bullet, pos, random) -> {};
        radius = 0.f;
        swayIntensity = 0.f;
        seed = 0;

        clientStartOffset = Vec3d.ZERO;
        noClip = true;

        kill ();
    }

    public RevolverBulletEntity (RevolverDirectHitFunction direct, RevolverSplashHitFunction splash, RevolverBulletTravelFunction travel, float radius, PlayerEntity originator, World world, Vec3d spawnPos, Vec3d clientStartPos, float clientSway, Vec3d vel) {
        super (Alchym.content ().entities.revolverBullet, world);

        this.direct = direct;
        this.splash = splash;
        this.travel = travel;
        this.radius = radius;
        seed = originator.getRandom ().nextInt (100000);
        swayIntensity = clientSway;

        this.clientStartOffset = clientStartPos.subtract (spawnPos);

        noClip = true;
        setBoundingBox (new Box (0.D, 0.D, 0.D, 0.D, 0.D, 0.D));
        setPos (spawnPos.x, spawnPos.y, spawnPos.z);
        setVelocity (vel);
    }

    public Vec3d getClientStartOffset (float tickDelta) {
        float smoothAge = (float) age + tickDelta;
        return MathHelper.lerp (clientStartOffset, Vec3d.ZERO, net.minecraft.util.math.MathHelper.clamp (Math.tanh (smoothAge / 20.f), 0.f, 1.f));
    }

    public Vec3d getClientSway (float tickDelta) {
        if (swayIntensity < 0.01)
            return Vec3d.ZERO;

        float smoothAge = (float) age + tickDelta;
        float swayAmount = (float) net.minecraft.util.math.MathHelper.clamp (Math.tanh (smoothAge / 15.f), 0.f, 1.f);
        swayAmount *= swayAmount;

        return new Vec3d (
                (0.8 * Math.sin (seed + smoothAge / 12.) * swayAmount * swayIntensity + 0.005 * Math.sin (smoothAge * 8.f)),
                (0.5 * Math.cos (seed + smoothAge / 8.)  * swayAmount * swayIntensity + 0.005 * Math.sin (smoothAge * 6.f)),
                0.005 * Math.sin (smoothAge * 4.f));
    }

    @Override
    public void tick () {
        if (! world.isClient)
            return;

        Vec3d forwardsComponent = getVelocity ().normalize ();
        Vec3d sideComponent = new Vec3d (0, 1, 0).crossProduct (forwardsComponent);
        Vec3d upComponent = sideComponent.crossProduct (forwardsComponent);
        Vec3d clientSway = getClientSway (0.f);
        clientSway = sideComponent.multiply (clientSway.x).add (upComponent.multiply (clientSway.y)).add (forwardsComponent.multiply (clientSway.z));

        travel.apply (this, this.getPos ().add (getClientStartOffset (0.f)).subtract (clientSway), random);

        // Trace from player eye pos to projectile spawn position
        BlockHitResult cast = world.raycast (new RaycastContext (this.getPos (), this.getPos ().add (this.getVelocity ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, this));

        if (cast.getType () == HitResult.Type.BLOCK) {
            Vec3d visualPos = TransmutationHelper.bumpFromSurface (cast, 15.f);
            Vec3d hitPos  = TransmutationHelper.bumpFromSurface (cast, radius);
            Vec3d normal = new Vec3d (cast.getSide ().getUnitVector ());

            List <LivingEntity> affectedEntities = world.getEntitiesByType (
                    TypeFilter.instanceOf (LivingEntity.class),
                    new Box (hitPos.subtract (2. * radius, 2. * radius, 2. * radius), hitPos.add (2. * radius, 2. * radius, 2. * radius)),
                    livingEntity -> {
                        boolean condition = livingEntity.squaredDistanceTo (hitPos) <= (4. * radius * radius);
                        if (world.isClient)
                            condition = condition && livingEntity == MinecraftClient.getInstance ().player;

                        return condition;
                    });

            splash.apply (this.world, hitPos, normal, visualPos, random, affectedEntities.toArray (new LivingEntity [0]));

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
