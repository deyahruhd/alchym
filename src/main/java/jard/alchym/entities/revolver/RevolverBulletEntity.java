package jard.alchym.entities.revolver;

import io.netty.buffer.Unpooled;
import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.transmutation.revolver.RevolverBehavior;
import jard.alchym.api.transmutation.revolver.RevolverBulletTravelFunction;
import jard.alchym.api.transmutation.revolver.RevolverDirectHitFunction;
import jard.alchym.api.transmutation.revolver.RevolverSplashHitFunction;
import jard.alchym.helper.MathHelper;
import jard.alchym.helper.MovementHelper;
import jard.alchym.helper.RevolverHelper;
import jard.alchym.helper.TransmutationHelper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.ClientSidePacketRegistryImpl;
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

    public final PlayerEntity owner;
    private UUID bulletId;

    private final float swayIntensity;
    public final int seed;

    public final Vec3d clientStartOffset;

    public RevolverBulletEntity (EntityType <Entity> type, World world) {
        super (type, world);

        behavior = RevolverBehavior.NONE;
        radius = 0.f;

        owner = null;
        bulletId = null;

        swayIntensity = 0.f;
        seed = 0;

        clientStartOffset = Vec3d.ZERO;
        noClip = true;

        kill ();
    }

    public RevolverBulletEntity (World world, RevolverBehavior behavior, PlayerEntity owner, UUID bulletId, Vec3d vel, Vec3d spawnPos, float radius) {
        this (world, behavior, owner, bulletId, vel, spawnPos, spawnPos, radius, 0.f);
    }

    public RevolverBulletEntity (World world, RevolverBehavior behavior, PlayerEntity owner, UUID bulletId, Vec3d vel, Vec3d spawnPos, Vec3d clientStartPos, float radius, float clientSway) {
        super (Alchym.content ().entities.revolverBullet, world);

        this.behavior = behavior;
        this.radius = radius;

        this.owner = owner;
        this.bulletId = bulletId;

        seed = world.random.nextInt (100000);
        swayIntensity = clientSway;
        this.clientStartOffset = clientStartPos.subtract (spawnPos);

        noClip = true;
        setBoundingBox (new Box (0.D, 0.D, 0.D, 0.D, 0.D, 0.D));
        setPos (spawnPos.x, spawnPos.y, spawnPos.z);
        setVelocity (vel);

        if (bulletId == null) {
            kill ();
        }
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

    public UUID getBulletId () {
        return bulletId;
    }

    @Override
    public void tick () {
        boolean isOrphaned = ! world.isClient && (owner == null || world.getPlayerByUuid (owner.getUuid ()) == null);

        boolean shouldDoBehavior =
                world.isClient || isOrphaned;

        boolean shouldReplay =
                world.isClient && owner == MinecraftClient.getInstance ().player;

        Vec3d forwardsComponent = getVelocity ().normalize ();
        Vec3d sideComponent = new Vec3d (0, 1, 0).crossProduct (forwardsComponent);
        Vec3d upComponent = sideComponent.crossProduct (forwardsComponent);
        Vec3d clientSway = getClientSway (0.f);
        clientSway = sideComponent.multiply (clientSway.x).add (upComponent.multiply (clientSway.y)).add (forwardsComponent.multiply (clientSway.z));

        behavior.travel ().apply (this, this.getPos ().add (getClientStartOffset (0.f)).subtract (clientSway), random);

        PacketByteBuf data = null;

        // Trace from player eye pos to projectile spawn position
        HitResult cast = TransmutationHelper.raycastEntitiesAndBlocks (owner == null ? this : owner, world, this.getPos (), this.getPos ().add (this.getVelocity ()));
        if (cast.getType() == HitResult.Type.BLOCK) {
            if (shouldDoBehavior) {
                data = PacketByteBufs.create ();
                Vec3d hitPos  = TransmutationHelper.bumpFromSurface ((BlockHitResult) cast, radius);
                List <LivingEntity> entitiesInRange = RevolverHelper.getEntitiesInRange (world, null, hitPos, radius);

                RevolverHelper.playSplash (behavior.splash (),
                        bulletId,
                        owner, world,
                        entitiesInRange,
                        hitPos,
                        getVelocity (),
                        TransmutationHelper.bumpFromSurface ((BlockHitResult) cast, 15.f),
                        new Vec3d (((BlockHitResult) cast).getSide ().getUnitVector ()),
                        radius, data);

                if (shouldReplay)
                    ClientSidePacketRegistryImpl.INSTANCE.sendToServer (AlchymReference.Packets.SERVER_REPLAY.id, data);
            }

            kill ();
        } else if (cast.getType () == HitResult.Type.ENTITY) {
            if (shouldDoBehavior) {
                data = PacketByteBufs.create ();

                LivingEntity target = (LivingEntity) ((EntityHitResult) cast).getEntity ();
                List <LivingEntity> entitiesInRange = RevolverHelper.getEntitiesInRange (world, target, cast.getPos (), radius);

                RevolverHelper.playDirect (
                        behavior.direct (),
                        behavior.splash (),
                        bulletId,
                        owner, world,
                        target, entitiesInRange,
                        cast.getPos (), getVelocity (), radius, data);

                if (shouldReplay)
                    ClientSidePacketRegistryImpl.INSTANCE.sendToServer (AlchymReference.Packets.SERVER_REPLAY.id, data);
            }

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
        bulletId = nbt.getUuid ("BulletID");
    }

    @Override
    protected void writeCustomDataToNbt (NbtCompound nbt) {
        nbt.putUuid ("BulletID", bulletId);
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
        data.writeUuid (owner == null ? new UUID (0, 0) : owner.getUuid ());
        data.writeUuid (bulletId);

        return ServerPlayNetworking.createS2CPacket (SPAWN_PACKET, data);
    }

    @Override
    public boolean shouldRender (double x, double y, double z) {
        return true;
    }
}
