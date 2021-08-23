package jard.alchym.mixin.rendering;

import jard.alchym.client.MatrixStackAccess;
import jard.alchym.client.MinecraftClientDataAccess;
import jard.alchym.items.CustomAttackItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 *  ItemUseAnimMixin
 *  Responsible for overriding the item use animation whenever the client player is holding
 *  an {@link jard.alchym.items.CustomAttackItem}.
 *
 *  Created by jard at 01:02 on June, 07, 2021.
 ***/
@Mixin (HeldItemRenderer.class)
public abstract class ItemUseAnimMixin {
    private static float swayProgress;
    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @Shadow
    public void renderItem (LivingEntity livingEntity, ItemStack itemStack, ModelTransformation.Mode mode, boolean bl, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {}

    @Shadow
    private void applyEquipOffset (MatrixStack matrixStack, Arm arm, float f) {}

    @Inject (
            method = "renderFirstPersonItem",
            at = @At (
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V",
                    ordinal = 12),
            cancellable = true)
    public void hookFirstPersonItem (AbstractClientPlayerEntity player, float tickDelta, float g, Hand hand, float swingProgress, ItemStack heldItem, float equipProgress, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int j, CallbackInfo info) {
        Arm arm = (hand == Hand.MAIN_HAND) ? player.getMainArm () : player.getMainArm ().getOpposite ();

        if (heldItem.getItem () instanceof CustomAttackItem) {
            applyEquipOffset (matrixStack, arm, equipProgress);

            if (((CustomAttackItem) heldItem.getItem ()).autoUse (heldItem)) {
                int fireRate = ((CustomAttackItem) heldItem.getItem ()).getAttackCooldown (heldItem);
                float smoothTime = (float) player.age + tickDelta;
                float sideRecoil = Math.min (smoothTime % (float) fireRate, 1.f);

                float smoothSway = ((MinecraftClientDataAccess) MinecraftClient.getInstance ()).getSwayProgress (tickDelta);
                float swaySquared = smoothSway * smoothSway;

                if (! MinecraftClient.getInstance ().options.keyAttack.isPressed ()) {
                    swaySquared = smoothSway;
                    sideRecoil = 0.f;
                }

                matrixStack.translate (
                        0.004 * Math.sin (smoothTime * 0.03) * Math.sin (2. * Math.PI * sideRecoil),
                        0.004 * Math.sin (2. * Math.PI * sideRecoil),
                        0.0);
                matrixStack.translate (
                        0.035 * Math.sin (smoothTime * 0.12) * swaySquared,
                        0.01 * Math.sin (smoothTime * 0.08) * swaySquared - 0.08 * swaySquared,
                        0.02 * Math.sin (smoothTime * 0.16) * swaySquared + 0.04 * swaySquared);
                matrixStack.multiply (Vec3f.POSITIVE_Y.getDegreesQuaternion (- 2.f * (float) (Math.sin (smoothTime * 0.12) * swaySquared)));
            }

            ((MatrixStackAccess) matrixStack).multiply (((CustomAttackItem) heldItem.getItem ()).getAnimMatrix (heldItem, arm, swingProgress));

            /* TODO: This needs to be put somewhere better
            float projectileSpeed = MovementHelper.upsToSpt (975.f * 15.3f);

            Camera camera = MinecraftClient.getInstance ().gameRenderer.getCamera ();
            Vec3d eyePos = camera.getPos ();
            Vec3d initialSpawnPos = player.getRotationVecClient ().multiply (projectileSpeed * 2.f).add (eyePos);

            // Trace from player eye pos to projectile spawn position
            BlockHitResult cast = player.world.raycast (new RaycastContext (eyePos, initialSpawnPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, player));

            if (cast.getType () != HitResult.Type.MISS) {
                Vec3d start = new Vec3d (0.0, 0.165, -0.08);
                Vec3d end = cast.getPos ();

                Vector4f transformedStart = new Vector4f ((float) start.x, (float) start.y, (float) start.z, 1.f);
                MatrixStack modelTransform = new MatrixStack ();
                itemRenderer.getHeldItemModel (heldItem, player.world, player, 0).getTransformation ()
                        .getTransformation (arm == Arm.RIGHT ? ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND : ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND)
                        .apply (arm == Arm.LEFT, modelTransform);
                transformedStart.transform (modelTransform.peek ().getModel ());
                start = new Vec3d (transformedStart.getX (), transformedStart.getY (), transformedStart.getZ ());

                Vector4f transformedEnd = new Vector4f ((float) end.x, (float) end.y, (float) end.z, 1.f);
                MatrixStack viewMatrix = RenderHelper.getViewProjectMatrix (camera, MinecraftClient.getInstance ().options.fov);
                Matrix4f invertedBobbing = matrixStack.peek ().getModel ().copy ();
                invertedBobbing.invert ();
                transformedEnd.transform (viewMatrix.peek ().getModel ());
                transformedEnd.transform (invertedBobbing);
                end = new Vec3d (transformedEnd.getX (), transformedEnd.getY (), transformedEnd.getZ ());

                RenderHelper.renderLightning (matrixStack, start, end, tickDelta);
            }
            */

            this.renderItem (player, heldItem, arm == Arm.RIGHT ? ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND : ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND, arm == Arm.LEFT, matrixStack, vertexConsumerProvider, j);

            matrixStack.pop ();
            info.cancel ();
        }
    }
}
