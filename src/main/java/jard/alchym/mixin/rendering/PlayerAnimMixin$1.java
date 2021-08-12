package jard.alchym.mixin.rendering;

import jard.alchym.Alchym;
import jard.alchym.client.ExtraPlayerDataAccess;
import jard.alchym.helper.MovementHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;

/***
 *  PlayerAnimMixin$1
 *  Responsible for lifting the player's arm when holding a Chymical Revolver, ducking their head based on their
 *  interpolated velocity, and storing their dev cloak.
 *
 *  Created by jard at 19:32 on January, 02, 2021.
 ***/
@Mixin (BipedEntityModel.class)
public abstract class PlayerAnimMixin$1 <T extends LivingEntity> extends EntityModel<T> implements ModelWithArms,
        ModelWithHead, ExtraPlayerDataAccess {
    @Shadow
    public ModelPart rightArm;
    @Shadow
    public ModelPart leftArm;
    @Shadow
    public ModelPart rightLeg;
    @Shadow
    public ModelPart leftLeg;
    @Shadow
    public ModelPart head;
    @Shadow
    public ModelPart hat;
    @Shadow
    public boolean sneaking;

    private ModelPart cloak = null;

    public PlayerAnimMixin$1 (EntityRenderDispatcher entityRenderDispatcher, PlayerEntityModel<AbstractClientPlayerEntity> entityModel, float f) {
    }

    @Shadow
    public ModelPart getHead () { return null; }

    @Inject (method = "setAngles", at = @At ("HEAD"))
    public void cancelSneakAnimation (T entity, float float_1, float float_2, float float_3, float float_4, float float_5,
                                     CallbackInfo info) {
        if (entity instanceof PlayerEntity) {
            if (! (entity.isOnGround () && ! ((ExtraPlayerDataAccess) entity).isJumping ()))
                sneaking = false;
        }
    }

    @Inject (method = "setAngles", at = @At ("RETURN"))
    public void setAngles(T entity, float float_1, float float_2, float float_3, float float_4, float float_5,
                          CallbackInfo info) {
        if (entity instanceof PlayerEntity) {
            if (cloak == null)
                cloak = getCloak ();

            PlayerEntity player = (PlayerEntity) entity;

            Vec3d look = player.getRotationVec (MinecraftClient.getInstance ().getTickDelta ()).multiply (1.0, 0.0, 1.0).normalize ();
            Vec3d right = look.crossProduct (new Vec3d (0, 1, 0));
            Vec3d previousVel = ((ExtraPlayerDataAccess) player).getPrevVel ();

            Vec3d vel = jard.alchym.helper.MathHelper.lerp (previousVel, player.getVelocity (), MinecraftClient.getInstance ().getTickDelta ()).multiply (1.0, 0.0, 1.0);


            double motionAngle = Math.tanh (vel.length () * 0.75) * -75.0;

            double dot = look.dotProduct (vel.normalize ());

            if (player.isOnGround () && ! ((ExtraPlayerDataAccess) player).isJumping ()
                    && player.isSneaking ()
                    && player.getVelocity ().multiply (1.f, 0.f, 1.f).length () >= MovementHelper.upsToSpt (320.f)) {

                motionAngle = dot * 22.5;

                double leftDeviation = right.multiply (-1.0).dotProduct (vel.normalize ());
                if (leftDeviation < 0.0)
                    leftDeviation = 0.0;
                double rightDeviation = right.dotProduct (vel.normalize ());
                if (rightDeviation < 0.0)
                    rightDeviation = 0.0;

                float leftPivotZ  = (float) MathHelper.lerp (rightDeviation, -0.2, 2.4);
                float rightPivotZ = (float) MathHelper.lerp (leftDeviation, -0.2, 2.4);

                leftArm.pitch = 0.f;
                leftLeg.pivotY = 15.2f;
                leftLeg.pivotZ = leftPivotZ;
                leftLeg.pitch = (90.f + 22.5f * (float) Math.abs (dot) + (float) Math.sin (float_1 * 1.75) * 0.9f) * (float) Math.PI / 180.f;
                leftLeg.yaw = (8.f + (float) Math.sin (float_1 * 2.0) * 0.3f) * (float) Math.PI / 180.f;
                leftLeg.roll = 22.5f * (float) Math.PI / 180.f;

                rightArm.pitch = 0.f;
                rightLeg.pivotY = 15.2f;
                rightLeg.pivotZ = rightPivotZ;
                rightLeg.pitch = (90.f + 22.5f * (float) Math.abs (dot) + (float) Math.sin (float_1 * 1.25) * 0.9f) * (float) Math.PI / 180.f;
                rightLeg.yaw = -(8.f + (float) Math.sin (float_1 * 1.5) * 0.3f) * (float) Math.PI / 180.f;
                rightLeg.roll = -22.5f * (float) Math.PI / 180.f;
            }

            double angle = dot * motionAngle * Math.PI / 180.0;

            // Set head pitch
            head.pitch += angle;

            ItemStack mainItem = entity.getItemsHand ().iterator ().next ();
            if (mainItem != null && !mainItem.isEmpty () && mainItem.getItem () == Alchym.content ().items.revolver) {
                ModelPart sel = entity.getMainArm () == Arm.LEFT ? leftArm : rightArm;

                // Set arm pitch
                sel.pitch = head.pitch - 90 * 0.017453292F;
                sel.yaw = head.yaw;
            }

            hat.copyTransform (head);
        }
    }

    public ModelPart getCloak () {
        if (cloak == null) {
            cloak = new ModelPart(
                    List.of(new ModelPart.Cuboid(
                            0, 0,
                            -6.0F, 0.0F, -1.0F,
                            12.0F, 22.0F, 1.0F,
                            0.f, 0.f, 0.f,
                            false,
                            26, 23)), new HashMap<>());
        }

        return cloak;
    }
}
