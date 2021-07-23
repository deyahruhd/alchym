package jard.alchym.mixin.rendering;

import jard.alchym.client.ExtraPlayerDataAccess;
import jard.alchym.helper.MathHelper;
import jard.alchym.helper.MovementHelper;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/***
 *  PlayerAnimMixin$2
 *  Tilts the player model in the direction of their velocity, and dispatches render calls of dev cloaks.
 *
 *  Created by jard at 19:32 on January, 02, 2021.
 ***/
@Mixin (PlayerEntityRenderer.class)
public abstract class PlayerAnimMixin$2 extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerAnimMixin$2 (EntityRenderDispatcher entityRenderDispatcher, PlayerEntityModel<AbstractClientPlayerEntity> entityModel, float f) {
        super (entityRenderDispatcher, entityModel, f);
    }

    @Inject (method = "render", at = @At ("HEAD"))
    public void renderHead(AbstractClientPlayerEntity player, float f, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider
        vertexConsumerProvider, int i, CallbackInfo info) {

        Vec3d previousVel = ((ExtraPlayerDataAccess) player).getPrevVel ();
        Vec3d vel = MathHelper.lerp (previousVel, player.getVelocity (), partialTicks).multiply (1.0, 0.0, 1.0);

        Vec3d look = player.getRotationVec (MinecraftClient.getInstance ().getTickDelta ()).multiply (1.0, 0.0, 1.0);
        Vec3d right = vel.crossProduct (new Vec3d (0.0, 1.0, 0.0)).normalize ();
        Vector3f axis = new Vector3f (right);

        double dot = look.dotProduct (vel.normalize ());

        float angle = (float) Math.tanh (vel.length () * 0.75) * -75.f;

        if (player.isOnGround () && !((ExtraPlayerDataAccess) player).isJumping () &&
                player.isSneaking () &&
                player.getVelocity ().multiply (1.f, 0.f, 1.f).length () > MovementHelper.upsToSpt (320.f)) {
            angle = (float) Math.tanh (vel.length () * 1.75) * -12.5f * (float) Math.abs (right.dotProduct (look));
            angle += 22.5f * (float) dot;
            matrixStack.translate (0.f, -0.3f + (0.1f * Math.abs (dot)), 0.f);
        }

        matrixStack.multiply(new Quaternion (axis, angle, true));
    }

    @Inject (method = "render", at = @At ("RETURN"))
    public void renderReturn(AbstractClientPlayerEntity player, float f, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider
            vertexConsumerProvider, int i, CallbackInfo info) {

        if (player.getUuid ().equals (UUID.fromString ("86dcc579-40cb-4713-85db-0643eabfd1d9")) ||
                (FabricLoader.INSTANCE.isDevelopmentEnvironment () && player == MinecraftClient.getInstance ().player))
            renderCloak (player, partialTicks, matrixStack, vertexConsumerProvider, i);
    }

    private void renderCloak (AbstractClientPlayerEntity player, float partialTicks, MatrixStack stack, VertexConsumerProvider provider, int i) {
        stack.push();

        boolean isTrulySneaking = player.isInSneakingPose() && (player.isOnGround () && ! ((ExtraPlayerDataAccess) player).isJumping ());

        float yaw;
        float bodyYaw = net.minecraft.util.math.MathHelper.lerpAngleDegrees(partialTicks, player.prevBodyYaw, player.bodyYaw);
        float headYaw = net.minecraft.util.math.MathHelper.lerpAngleDegrees(partialTicks, player.prevHeadYaw, player.headYaw);
        float diffYaw = headYaw - bodyYaw;
        if (player.hasVehicle() && player.getVehicle() instanceof LivingEntity) {
            yaw = net.minecraft.util.math.MathHelper.wrapDegrees (diffYaw);
            if (yaw < -85.0F)
                yaw = -85.0F;

            if (yaw >= 85.0F)
                yaw = 85.0F;

            bodyYaw = headYaw - yaw;
            if (yaw * yaw > 2500.0F)
                bodyYaw += yaw * 0.2F;
        }
        double d = net.minecraft.util.math.MathHelper.lerp(partialTicks, player.prevCapeX, player.capeX) - net.minecraft.util.math.MathHelper.lerp(partialTicks, player.prevX, player.getX());
        double e = net.minecraft.util.math.MathHelper.lerp(partialTicks, player.prevCapeY, player.capeY) - net.minecraft.util.math.MathHelper.lerp(partialTicks, player.prevY, player.getY());
        double m = net.minecraft.util.math.MathHelper.lerp(partialTicks, player.prevCapeZ, player.capeZ) - net.minecraft.util.math.MathHelper.lerp(partialTicks, player.prevZ, player.getZ());
        float n = player.prevBodyYaw + (player.bodyYaw - player.prevBodyYaw);

        double o = net.minecraft.util.math.MathHelper.sin(n * 0.017453292F);
        double p = -net.minecraft.util.math.MathHelper.cos(n * 0.017453292F);
        float q = (float)e * 10.0F;
        q = net.minecraft.util.math.MathHelper.clamp(q, -6.0F, 32.0F);
        float r = (float)Math.tanh (2.2 * (d * o + m * p)) * 75.0F;
        r = net.minecraft.util.math.MathHelper.clamp(r, 0.0F, 75.0F);
        float s = (float)(d * p - m * o) * 150.0F;
        s = net.minecraft.util.math.MathHelper.clamp(s, -20.0F, 20.0F);

        float t = net.minecraft.util.math.MathHelper.lerp(partialTicks, player.prevStrideDistance, player.strideDistance);
        q += net.minecraft.util.math.MathHelper.sin(net.minecraft.util.math.MathHelper.lerp(partialTicks, player.prevHorizontalSpeed, player.horizontalSpeed) * 6.0F) * 32.0F * t;
        if (isTrulySneaking) {
            q += 25.0F;
        }

        stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(- s / 2.0F - bodyYaw));
        stack.translate(0.0D, 1.5D - 0.125D - (isTrulySneaking ? 0.15D : 0.0D), - 0.125D + (isTrulySneaking ? 0.03D : 0.0D));
        stack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(6.0F + r / 2.0F + q));
        stack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F - s / 2.0F));

        VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getEntitySolid (new Identifier ("alchym", "textures/jard_cloak.png")));
        ((ExtraPlayerDataAccess) this.getModel ()).getCloak ().render (stack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
        stack.pop ();
    }
}
