package jard.alchym.mixin.rendering;

import jard.alchym.client.MatrixStackAccess;
import jard.alchym.items.CustomAttackItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
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
    public void hookFirstPersonItem (AbstractClientPlayerEntity player, float f, float g, Hand hand, float swingProgress, ItemStack heldItem, float equipProgress, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int j, CallbackInfo info) {
        Arm arm = (hand == Hand.MAIN_HAND) ? player.getMainArm () : player.getMainArm ().getOpposite ();

        if (heldItem.getItem () instanceof CustomAttackItem) {
            applyEquipOffset (matrixStack, arm, equipProgress);

            ((MatrixStackAccess) matrixStack).multiply (((CustomAttackItem) heldItem.getItem ()).getAnimMatrix (heldItem, arm, swingProgress));

            this.renderItem (player, heldItem, arm == Arm.RIGHT ? ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND : ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND, arm == Arm.LEFT, matrixStack, vertexConsumerProvider, j);

            matrixStack.pop ();
            info.cancel ();
        }
    }
}
