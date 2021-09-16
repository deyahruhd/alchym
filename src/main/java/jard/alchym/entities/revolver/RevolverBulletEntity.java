package jard.alchym.entities.revolver;

import io.netty.buffer.Unpooled;
import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.transmutation.revolver.RevolverBehavior;
import jard.alchym.api.transmutation.revolver.RevolverBulletTravelFunction;
import jard.alchym.api.transmutation.revolver.RevolverDirectHitFunction;
import jard.alchym.api.transmutation.revolver.RevolverSplashHitFunction;
import jard.alchym.helper.MathHelper;
import jard.alchym.helper.TransmutationHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/***
 *  RevolverBulletEntity
 *  The revolver spellcast projectile entity.
 *
 *  Created by jard at 15:33 on August 23, 2021.
 ***/
public class RevolverBulletEntity extends Entity {
    public static final Identifier SPAWN_PACKET = new Identifier (AlchymReference.MODID, "spawn_revolver_bullet");
    private final RevolverBehavior behavior;
    private final float radius;

    public final PlayerEntity originator;

    private final float swayIntensity;
    public final int seed;

    public final Vec3d clientStartOffset;

    public RevolverBulletEntity (EntityType <Entity> type, World world) {
        super (type, world);

        behavior = RevolverBehavior.NONE;
        radius = 0.f;

        originator = null;

        swayIntensity = 0.f;
        seed = 0;

        clientStartOffset = Vec3d.ZERO;
        noClip = true;

        kill ();
    }

    public RevolverBulletEntity (RevolverBehavior behavior, float radius, PlayerEntity originator, World world, Vec3d serverPos, Vec3d vel) {
        this (behavior, radius, originator, world, serverPos, serverPos, 0.f, vel);
    }

    public RevolverBulletEntity (RevolverBehavior behavior, float radius, PlayerEntity originator, World world, Vec3d spawnPos, Vec3d clientStartPos, float clientSway, Vec3d vel) {
        super (Alchym.content ().entities.revolverBullet, world);

        this.behavior = behavior;
        this.radius = radius;

        this.originator = originator;

        seed = world.random.nextInt (100000);
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
        if (originator == null) {
            kill ();
            return;
        }

        Vec3d forwardsComponent = getVelocity ().normalize ();
        Vec3d sideComponent = new Vec3d (0, 1, 0).crossProduct (forwardsComponent);
        Vec3d upComponent = sideComponent.crossProduct (forwardsComponent);
        Vec3d clientSway = getClientSway (0.f);
        clientSway = sideComponent.multiply (clientSway.x).add (upComponent.multiply (clientSway.y)).add (forwardsComponent.multiply (clientSway.z));

        behavior.travel ().apply (this, this.getPos ().add (getClientStartOffset (0.f)).subtract (clientSway), random);

        // Trace from player eye pos to projectile spawn position
        HitResult cast = TransmutationHelper.raycastEntitiesAndBlocks (originator, world, this.getPos (), this.getPos ().add (this.getVelocity ()));
        if (cast.getType() == HitResult.Type.BLOCK) {
            Vec3d visualPos = TransmutationHelper.bumpFromSurface ((BlockHitResult) cast, 15.f);
            Vec3d hitPos  = TransmutationHelper.bumpFromSurface ((BlockHitResult) cast, radius);
            Vec3d normal = new Vec3d (((BlockHitResult) cast).getSide ().getUnitVector ());

            List <LivingEntity> affectedEntities = world.getEntitiesByType (
                    TypeFilter.instanceOf (LivingEntity.class),
                    new Box (hitPos.subtract (2. * radius, 2. * radius, 2. * radius), hitPos.add (2. * radius, 2. * radius, 2. * radius)),
                    livingEntity -> {
                        boolean condition = livingEntity.squaredDistanceTo (hitPos) <= (4. * radius * radius);
                        if (world.isClient)
                            condition = condition && livingEntity == MinecraftClient.getInstance ().player;

                        return condition;
                    });

            behavior.splash ().apply (originator, this.world, radius, getVelocity (), hitPos, normal, visualPos, random, affectedEntities.toArray (new LivingEntity [0]));
            kill ();
        } else if (cast.getType () == HitResult.Type.ENTITY) {
            LivingEntity target = (LivingEntity) ((EntityHitResult) cast).getEntity ();

            List <LivingEntity> splashEntities = originator.world.getEntitiesByType (
                    TypeFilter.instanceOf (LivingEntity.class),
                    new Box (cast.getPos ().subtract (2. * radius, 2. * radius, 2. * radius), cast.getPos ().add (2. * radius, 2. * radius, 2. * radius)),
                    livingEntity -> {
                        boolean condition = livingEntity.squaredDistanceTo (cast.getPos ()) <= (4. * radius * radius) && livingEntity != target;
                        if (world.isClient)
                            condition = condition && livingEntity == MinecraftClient.getInstance ().player;

                        return condition;
                    });

            behavior.direct ().apply (originator, target, cast.getPos (), getVelocity (), random);
            behavior.splash ().apply (originator, world, radius, getVelocity (), cast.getPos (), getVelocity (), cast.getPos (), random, splashEntities.toArray (new LivingEntity [0]));
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
        PacketByteBuf data = new PacketByteBuf (Unpooled.buffer());
        data.writeDouble (getX ());
        data.writeDouble (getY ());
        data.writeDouble (getZ ());
        data.writeDouble (getVelocity ().x);
        data.writeDouble (getVelocity ().y);
        data.writeDouble (getVelocity ().z);
        data.writeUuid (originator == null ? new UUID (0, 0) : originator.getUuid ());

        return ServerPlayNetworking.createS2CPacket (SPAWN_PACKET, data);
    }

    @Override
    public boolean shouldRender (double x, double y, double z) {
        return true;
    }
}
