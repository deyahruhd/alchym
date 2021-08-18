package jard.alchym.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import jard.alchym.client.QuakeKnockbackable;
import jard.alchym.client.helper.RenderHelper;
import jard.alchym.helper.MathHelper;
import jard.alchym.helper.MovementHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.RaycastContext;

/***
 *  RevolverItem
 *  The Chymical Revolver item class.
 *
 *  Created by jard at 8:33 PM on April 03, 2019.
 ***/
public class RevolverItem extends Item implements CustomAttackItem {
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



    @Override
    public Matrix4f getAnimMatrix (ItemStack stack, Arm arm, float progress) {
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

    @Override
    public int getSwingDuration (ItemStack stack) {
        //return 20; // rocket
        return 5; // plasma
        //return 3; // lightning
    }

    @Override
    public int getAttackCooldown (ItemStack stack) {
        //return 17; // rocket
        return 2; // plasma
        //return 1; // lightning
    }

    @Override
    public boolean autoUse (ItemStack stack) {
        //return false; // rocket
        return true; // plasma
        //return true; // lightning
    }

    @Environment (EnvType.CLIENT)
    @Override
    public boolean clientAttack (PlayerEntity player, ItemStack stack, Vec3d aimDir) {
        /* rocket
        float projectileSpeed = MovementHelper.upsToSpt (925.f) * 1.5f;
        float radius = 2.5f;
        double verticalKnockback = MovementHelper.upsToSpt (615.f);
        double horizontalKnockback = MovementHelper.upsToSpt (555.f);
        boolean skim = true;
        boolean icy = false;
        //*/
        // plasma
        float projectileSpeed = MovementHelper.upsToSpt (975.f * 2.f);
        float radius = 1.0f;
        double verticalKnockback = MovementHelper.upsToSpt (149.29f);
        double horizontalKnockback = MovementHelper.upsToSpt (48.75f);
        boolean skim = false;
        boolean icy = true;
        //*/
        /* lightning
        float projectileSpeed = MovementHelper.upsToSpt (975.f * 15.3f);
        float radius = 15.3f;
        double verticalKnockback = MovementHelper.upsToSpt (149.29f);
        double horizontalKnockback = MovementHelper.upsToSpt (97.5f);
        boolean skim = false;
        boolean icy = false;
        //*/

        Vec3d eyePos = player.getPos ().add (0.0, player.getStandingEyeHeight (), 0.0);
        Vec3d initialSpawnPos = aimDir.multiply (projectileSpeed * 2.f).add (eyePos);

        // Trace from player eye pos to projectile spawn position
        BlockHitResult cast = player.world.raycast (new RaycastContext (eyePos, initialSpawnPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, player));

        Vec3d spawnPos = cast.getType () != HitResult.Type.MISS ? cast.getPos () : initialSpawnPos;

        if (cast.getType () == HitResult.Type.BLOCK) {
            double len = net.minecraft.util.math.MathHelper.clamp (0.34 - 0.04 * radius, 0.1, 0.3);

            Vec3d normal = new Vec3d (cast.getSide ().getUnitVector ()).multiply (len);
            spawnPos = spawnPos.add (normal);
        }

        ParticleEffect effect = ParticleTypes.ELECTRIC_SPARK;

        if (cast.getType() != HitResult.Type.MISS) {
        //if (cast.getType () == HitResult.Type.BLOCK && player.world.getBlockState (cast.getBlockPos ()).getBlock () == Blocks.COPPER_BLOCK) {
            ((QuakeKnockbackable) player).radialKnockback (spawnPos, radius, verticalKnockback, horizontalKnockback, skim, icy);
            effect = ParticleTypes.EXPLOSION;
        }

        player.world.addParticle (effect, spawnPos.x, spawnPos.y, spawnPos.z, 0.f, 0.f, 0.f);

        return true;
    }
}
