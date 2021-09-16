package jard.alchym.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.netty.buffer.Unpooled;
import jard.alchym.AlchymReference;
import jard.alchym.api.transmutation.revolver.RevolverBehavior;
import jard.alchym.client.helper.RenderHelper;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import jard.alchym.helper.MathHelper;
import jard.alchym.helper.MovementHelper;
import jard.alchym.helper.RevolverHelper;
import jard.alchym.helper.TransmutationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.networking.ClientSidePacketRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Arm;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;

import java.util.List;

/***
 *  RevolverItem
 *  The Chymical Revolver item class.
 *
 *  Created by jard at 8:33 PM on April 03, 2019.
 ***/
public class RevolverItem extends Item {
    private static final float [][] yRotCoeffs = {
            {
                    0.f,
                    -0.88f,
                    62.91f,
                    5020.33f,
                    -68867.49f,
                    234052.93f
            }, {
            1.71f,
            -10.14f,
            46.27f,
            -89.14f,
            70.89f,
            -19.59f
    }
    };

    private static final float [][] xDisplaceCoeffs = {
            {
                    0.00000f,
                    11.66667f,
                    -396.34986f,
                    1214.58137f,
                    80303.53007f,
                    -756420.97641f
            }, {
            0.16147f,
            -3.96026f,
            20.46216f,
            -41.89782f,
            37.54129f,
            -12.30684f,
    }
    };

    private static final float [][] yDisplaceCoeffs = {
            {
                    0.00000f,
                    0.04975f,
                    -0.03525f,
                    1.95578f,
                    -6.44245f,
                    4.70821f
            }, {
            -11680.97394f,
            64480.86612f,
            -142181.96231f,
            156529.98979f,
            -86032.71637f,
            18884.79672f,
    }
    };

    private static final float [][] zDisplaceCoeffs = {
            {
                    0.00000f,
                    -0.85714f,
                    1221.58730f,
                    -35133.76942f,
                    364180.82968f,
                    -1296567.36169f
            }, {
            0.10325f,
            6.01059f,
            -29.02407f,
            49.73467f,
            -36.96276f,
            10.13832f
    }
    };
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public RevolverItem (Settings settings) {
        super (settings);

        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.667, EntityAttributeModifier.Operation.ADDITION));

        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers (equipmentSlot);
    }

    public int getSwingDuration (ItemStack stack) {
        return 20; // rocket
        //return 5; // plasma
        //return 3; // lightning
    }

    public int getAttackCooldown (ItemStack stack) {
        return 16; // rocket
        //return 2; // plasma
        //return 1; // lightning
    }

    public boolean autoUse (ItemStack stack) {
        return false; // rocket
        //return true; // plasma
        //return true; // lightning
    }

    @Environment (EnvType.CLIENT)
    public Matrix4f getAnimMatrix (ItemStack stack, Arm arm, float progress) {
        progress = (float) Math.pow (progress, 1.3);

        float handedness = arm == Arm.RIGHT ? 1.f : -1.f;

        float recoil = net.minecraft.util.math.MathHelper.clamp (getSwingDuration (stack) / 18.f, 0.1f, 1.f);
        float squareRecoil = recoil * recoil;

        float progPower = progress * progress * progress;

        float globalFactor = (1.f - progPower) / (1.f + 27.f * progPower * progPower * progPower) * squareRecoil;

        float xRot      = MathHelper.quinticSpline (progress * 1.28f  * 1.25f,   0.123f, yRotCoeffs) * globalFactor * recoil;
        float xDisplace = MathHelper.quinticSpline (progress * 0.666f * 1.333f,  0.054f, xDisplaceCoeffs) * globalFactor;
        float yDisplace = MathHelper.quinticSpline (progress * 0.9f   * 1.1667f, 0.863f, yDisplaceCoeffs) * globalFactor;
        float zDisplace = MathHelper.quinticSpline (progress * 1.25f,            0.09f, zDisplaceCoeffs) * globalFactor / recoil;

        Matrix4f out = RenderHelper.IDENTITY_MATRIX.copy ();
        out.multiply (Vec3f.POSITIVE_Z.getDegreesQuaternion (xDisplace * 115.f * handedness));
        out.multiply (Vec3f.POSITIVE_X.getDegreesQuaternion (xRot * 21.f));
        out.multiply (Vec3f.POSITIVE_Y.getDegreesQuaternion (xDisplace * -44.f * handedness));
        out.multiply (Matrix4f.translate (xDisplace * -0.17f * handedness, yDisplace * 0.8f, zDisplace * 0.87f - 0.07f));

        return out;
    }

    @Environment (EnvType.CLIENT)
    public boolean clientAttack (PlayerEntity player, ItemStack item, Vec3d eyePos, Vec3d aimDir) {
        // rocket
        float projectileSpeed = MovementHelper.upsToSpt (945.f);
        float hitscanSpeed    = MovementHelper.upsToSpt (945.f * 2.f);
        float radius = 3.5f;
        float sway = 0.1f;
        boolean firesBullet = true;
        //*/
        /* plasma
        float projectileSpeed = MovementHelper.upsToSpt (945.f * 3.f);
        float hitscanSpeed    = MovementHelper.upsToSpt (945.f * 2.0f);
        float radius = 0.5f;
        float sway = 0.05f;
        boolean firesBullet = true;
        //*/
        /* lightning
        float projectileSpeed = 1.f;
        float hitscanSpeed    = 24.f;
        float radius = 0.f;
        float sway = 0.f;
        boolean firesBullet = false;
        //*/

        RevolverBehavior behavior = RevolverHelper.getBulletBehavior (true);

        Vec3d initialSpawnPos = aimDir.multiply (hitscanSpeed).add (eyePos);

        // Trace from player eye pos to projectile spawn position
        HitResult cast = TransmutationHelper.raycastEntitiesAndBlocks (player, player.world, eyePos, initialSpawnPos);

        Vec3d spawnPos = cast.getType () != HitResult.Type.MISS ? cast.getPos () : initialSpawnPos;
        Vec3d velocity = aimDir.normalize ().multiply (projectileSpeed);

        if (cast.getType() == HitResult.Type.BLOCK) {
            spawnPos = TransmutationHelper.bumpFromSurface ((BlockHitResult) cast, radius);
            Vec3d splashPos = spawnPos;
            Vec3d visualPos = TransmutationHelper.bumpFromSurface ((BlockHitResult) cast, 15.0);
            Vec3d normal = new Vec3d (((BlockHitResult) cast).getSide ().getUnitVector ());

            List <LivingEntity> affectedEntities = player.world.getEntitiesByType (
                    TypeFilter.instanceOf (LivingEntity.class),
                    new Box (spawnPos.subtract (2. * radius, 2. * radius, 2. * radius), spawnPos.add (2. * radius, 2. * radius, 2. * radius)),
                    livingEntity -> {
                        boolean condition = livingEntity.squaredDistanceTo (splashPos) <= (4. * radius * radius);
                        if (player.world.isClient)
                            condition = condition && livingEntity == player;

                        return condition;
                    });

            behavior.splash ().apply (player, player.world, radius, velocity, spawnPos, normal, visualPos, player.getRandom (), affectedEntities.toArray (new LivingEntity [0]));
        } else if (cast.getType () == HitResult.Type.ENTITY) {
            LivingEntity target = (LivingEntity) ((EntityHitResult) cast).getEntity ();

            List <LivingEntity> splashEntities = player.world.getEntitiesByType (
                    TypeFilter.instanceOf (LivingEntity.class),
                    new Box (spawnPos.subtract (2. * radius, 2. * radius, 2. * radius), spawnPos.add (2. * radius, 2. * radius, 2. * radius)),
                    livingEntity -> {
                        boolean condition = livingEntity.squaredDistanceTo (cast.getPos ()) <= (4. * radius * radius) && livingEntity != target;
                        if (player.world.isClient)
                            condition = condition && livingEntity == player;

                        return condition;
                    });

            behavior.direct ().apply (player, target, cast.getPos (), velocity, player.getRandom ());
            behavior.splash ().apply (player, player.world, radius, velocity, spawnPos, velocity, spawnPos, player.getRandom (), splashEntities.toArray (new LivingEntity [0]));
        } else if (firesBullet) {
            // Calculate the client-side bullet position
            Vec3d bulletStart = new Vec3d (0., 0.165, - 0.38);

            Camera camera = MinecraftClient.getInstance ().gameRenderer.getCamera ();
            if (! camera.isThirdPerson ()) {
                Matrix4f viewTransform = RenderHelper.getViewProjectMatrix (camera, MinecraftClient.getInstance ().options.fov).peek ().getModel ();
                viewTransform.invert ();
                Matrix4f revolverTransform = RenderHelper.getRevolverTransform (
                        item, player, player.getMainArm (),
                        MinecraftClient.getInstance ().getTickDelta (),
                        player.getHandSwingProgress (MinecraftClient.getInstance ().getTickDelta ())).peek ().getModel ();
                Matrix4f handTransform = RenderHelper.getHandTransform (item, (ClientPlayerEntity) player, player.getMainArm ()).peek ().getModel ();

                Vector4f transformedStart = new Vector4f ((float) bulletStart.x, (float) bulletStart.y, (float) bulletStart.z, 1.f);
                transformedStart.transform (handTransform);
                transformedStart.transform (revolverTransform);
                transformedStart.transform (viewTransform);
                bulletStart = new Vec3d (transformedStart.getX (), transformedStart.getY (), transformedStart.getZ ());
            } else {
                bulletStart = new Vec3d (0., 1.25, -0.21);
                Matrix4f thirdPersonTransform = RenderHelper.getThirdPersonRevolverTransform (player, player.getMainArm ()).peek ().getModel ();

                Vector4f transformedStart = new Vector4f ((float) bulletStart.x, (float) bulletStart.y, (float) bulletStart.z, 1.f);
                transformedStart.transform (thirdPersonTransform);
                bulletStart = new Vec3d (transformedStart.getX (), transformedStart.getY (), transformedStart.getZ ());
            }
            RevolverBulletEntity clientBullet = new RevolverBulletEntity (behavior, radius, player, player.world, spawnPos, bulletStart, sway, velocity);
            ((ClientWorld) player.world).addEntity (player.world.random.nextInt (), clientBullet);
        }

        PacketByteBuf data = new PacketByteBuf (Unpooled.buffer());
        data.writeDouble (eyePos.x);
        data.writeDouble (eyePos.y);
        data.writeDouble (eyePos.z);
        data.writeDouble (aimDir.x);
        data.writeDouble (aimDir.y);
        data.writeDouble (aimDir.z);

        ClientSidePacketRegistryImpl.INSTANCE.sendToServer (AlchymReference.Packets.REVOLVER_ACTION.id, data);
        return true;
    }
}
