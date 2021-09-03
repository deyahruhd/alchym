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


    public final Vec3d clientStartOffset;

    public RevolverBulletEntity (EntityType <Entity> type, World world) {
        super (type, world);

        direct = (vel, w, random, target) -> {};
        splash = (vel, hitPos, hitNormal, visualPos, w, random, targets) -> {};
        travel = (bullet, vel, w, random) -> {};
        radius = 0.f;

        clientStartOffset = Vec3d.ZERO;

        kill ();
    }

    public RevolverBulletEntity (RevolverDirectHitFunction direct, RevolverSplashHitFunction splash, RevolverBulletTravelFunction travel, float radius, World world, Vec3d spawnPos, Vec3d clientStartPos, Vec3d vel) {
        super (Alchym.content ().entities.revolverBullet, world);

        this.direct = direct;
        this.splash = splash;
        this.travel = travel;
        this.radius = radius;

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

        travel.apply (this.getPos ().add (getClientStartOffset (MinecraftClient.getInstance ().getTickDelta ())), this.getVelocity (), this.world, random);

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

            splash.apply (getVelocity (), hitPos, normal, visualPos, world, random, affectedEntities.toArray (new LivingEntity [0]));

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
