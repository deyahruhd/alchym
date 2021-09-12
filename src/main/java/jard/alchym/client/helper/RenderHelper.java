package jard.alchym.client.helper;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jard.alchym.AlchymReference;
import jard.alchym.client.ExtraPlayerDataAccess;
import jard.alchym.client.MatrixStackAccess;
import jard.alchym.client.MinecraftClientDataAccess;
import jard.alchym.helper.MovementHelper;
import jard.alchym.items.RevolverItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Random;

/***
 *  RenderHelper
 *  Contains various helper methods for graphical rendering and matrix manipulation using linear algebra.
 *
 *  Created by jard at 01:50 on January, 03, 2021.
 ***/
public class RenderHelper {
    public static Matrix4f yShear (float angle) {
        angle += 45.f;

        float oneOverAlpha = (float) Math.tan (angle * 0.017453292F);

        Matrix4f first = new Matrix4f (Vec3f.NEGATIVE_Z.getDegreesQuaternion (- angle));
        Matrix4f second = Matrix4f.scale (1.f / oneOverAlpha, oneOverAlpha, 1.f);
        Matrix4f third = new Matrix4f (Vec3f.NEGATIVE_Z.getDegreesQuaternion (90.f - angle));

        first.multiply (second);
        first.multiply (third);
        first.transpose ();

        return first;
    }

    public static final Matrix4f IDENTITY_MATRIX = new Matrix4f ();
    static {
        IDENTITY_MATRIX.loadIdentity ();
    }

    public static void renderGuiItem (MatrixStack stack, ItemStack itemStack,
                                  int i, int j,
                                  ItemRenderer renderer, TextureManager textureManager) {
        BakedModel bakedModel = renderer.getHeldItemModel(itemStack, null, null, 0);

        stack.push ();
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        stack.translate (i, j, 2.0F);
        stack.translate (8.0F, 8.0F, 0.0F);
        stack.scale (1.0F, -1.0F, 1.0F);
        stack.scale (16.0F, 16.0F, 16.0F);

        RenderSystem.applyModelViewMatrix();
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        boolean bl = !bakedModel.isSideLit();
        if (bl) {
            DiffuseLighting.disableGuiDepthLighting();
        }

        renderer.renderItem (itemStack, ModelTransformation.Mode.GUI, false, stack, immediate, 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
        immediate.draw();
        RenderSystem.enableDepthTest();
        if (bl) {
            DiffuseLighting.enableGuiDepthLighting();
        }

        stack.pop ();
        RenderSystem.applyModelViewMatrix();
    }

    public static MatrixStack getViewProjectMatrix (Camera camera, double fov) {
        MatrixStack toEyeSpace = new MatrixStack ();
        toEyeSpace.multiply (Vec3f.POSITIVE_X.getDegreesQuaternion (camera.getPitch ()));
        toEyeSpace.multiply (Vec3f.POSITIVE_Y.getDegreesQuaternion (camera.getYaw () + 180.f));
        toEyeSpace.translate (-camera.getPos ().x, -camera.getPos ().y, -camera.getPos ().z);

        return toEyeSpace;
    }

    public static MatrixStack getRevolverTransform (ItemStack item, PlayerEntity player, Arm arm, float tickDelta, float swingProgress) {
        MatrixStack stack = new MatrixStack ();

        if (item.getItem () instanceof RevolverItem) {
            if (((RevolverItem) item.getItem ()).autoUse (item)) {
                int fireRate = ((RevolverItem) item.getItem ()).getAttackCooldown (item);
                float smoothTime = (float) player.age + tickDelta;
                float sideRecoil = Math.min (smoothTime % (float) fireRate, 1.f);

                float smoothSway = ((MinecraftClientDataAccess) MinecraftClient.getInstance ()).getSwayProgress (tickDelta);
                float swaySquared = smoothSway * smoothSway;

                if (! MinecraftClient.getInstance ().options.keyAttack.isPressed ()) {
                    swaySquared = smoothSway;
                    sideRecoil = 0.f;
                }

                stack.translate (
                        0.004 * Math.sin (smoothTime * 0.03) * Math.sin (2. * Math.PI * sideRecoil),
                        0.004 * Math.sin (2. * Math.PI * sideRecoil),
                        0.0);
                stack.translate (
                        0.035 * Math.sin (smoothTime * 0.12) * swaySquared,
                        0.01 * Math.sin (smoothTime * 0.08) * swaySquared - 0.08 * swaySquared,
                        0.02 * Math.sin (smoothTime * 0.16) * swaySquared + 0.04 * swaySquared);
                stack.multiply (Vec3f.POSITIVE_Y.getDegreesQuaternion (- 2.f * (float) (Math.sin (smoothTime * 0.12) * swaySquared)));
            }

            ((MatrixStackAccess) stack).multiply (((RevolverItem) item.getItem ()).getAnimMatrix (item, arm, swingProgress));
        }

        return stack;
    }

    public static MatrixStack getHandTransform (ItemStack item, ClientPlayerEntity player, Arm arm) {
        MatrixStack stack = new MatrixStack ();

        float tickDelta = MinecraftClient.getInstance ().getTickDelta ();
        float h = MathHelper.lerp (tickDelta, player.lastRenderPitch, player.renderPitch);
        float i = MathHelper.lerp (tickDelta, player.lastRenderYaw, player.renderYaw);
        stack.multiply (Vec3f.POSITIVE_X.getDegreesQuaternion ((player.getPitch (tickDelta) - h) * 0.1f));
        stack.multiply (Vec3f.POSITIVE_Y.getDegreesQuaternion ((player.getYaw (tickDelta) - i) * 0.1f));
        MinecraftClient.getInstance ().getItemRenderer ().getHeldItemModel (item, player.world, player, 0)
                .getTransformation ()
                .getTransformation (arm == Arm.RIGHT ? ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND : ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND)
                .apply (arm == Arm.LEFT, stack);
        stack.translate(-0.5 * 1.12 * (arm == Arm.LEFT ? 1. : -1.), -0.5 * 1.12, -0.5 * 1.12);
        return stack;
    }

    public static MatrixStack getThirdPersonRevolverTransform (PlayerEntity player, Arm arm) {
        MatrixStack stack = new MatrixStack ();

        PlayerEntityRenderer renderer = (PlayerEntityRenderer) (Object) MinecraftClient.getInstance ().getEntityRenderDispatcher ().getRenderer (player);
        PlayerEntityModel <AbstractClientPlayerEntity> model = renderer.getModel ();
        ModelPart modelArm = arm == Arm.LEFT ? model.leftArm : model.rightArm;

        stack.translate (player.getX (), player.getY (), player.getZ ());
        ((MatrixStackAccess) stack).multiply (RenderHelper.getPlayerLeanTransform (player, 1.f).peek ().getModel ());
        stack.multiply (Vec3f.POSITIVE_Y.getDegreesQuaternion (180.f - player.bodyYaw));
        stack.scale(-1.0F, -1.0F, 1.0F);
        stack.translate(0.0D, -1.5010000467300415D, 0.0D);
        modelArm.rotate (stack);

        return stack;
    }

    public static MatrixStack getPlayerLeanTransform (PlayerEntity player, double partialTicks) {
        MatrixStack stack = new MatrixStack ();

        Vec3d previousVel = ((ExtraPlayerDataAccess) player).getPrevVel ();
        Vec3d vel = jard.alchym.helper.MathHelper.lerp (previousVel, player.getVelocity (), partialTicks).multiply (1.0, 0.0, 1.0);

        Vec3d look = player.getRotationVec (MinecraftClient.getInstance ().getTickDelta ()).multiply (1.0, 0.0, 1.0);
        Vec3d right = vel.crossProduct (new Vec3d (0.0, 1.0, 0.0)).normalize ();
        Vec3f axis = new Vec3f (right);

        double dot = look.dotProduct (vel.normalize ());

        float angle = (float) Math.tanh (vel.length () * 0.75) * -75.f;

        if (player.isOnGround () && !((ExtraPlayerDataAccess) player).isJumping () &&
                player.isSneaking () &&
                player.getVelocity ().multiply (1.f, 0.f, 1.f).length () > MovementHelper.upsToSpt (320.f)) {
            angle = (float) Math.tanh (vel.length () * 1.75) * -12.5f * (float) Math.abs (right.dotProduct (look));
            angle += 22.5f * (float) dot;
            stack.translate (0.f, -0.3f + (0.1f * Math.abs (dot)), 0.f);
        }

        stack.multiply (new Quaternion (axis, angle, true));

        return stack;
    }

    private static final double[] LIGHTNING_STEPS = {
            0.25,
            0.5,
            0.75,
            1.,
            2.5
    };

    private static final ArrayList <Pair <Vec2f, Vec2f>> LIGHTNING_QUAD_INFO = new ArrayList <> ();
    static {
        LIGHTNING_QUAD_INFO.add (
                new Pair (new Vec2f (0.f / 32.f, 0.f / 32.f), new Vec2f (2.f / 32.f, 8.f / 32.f)));
        LIGHTNING_QUAD_INFO.add (
                new Pair (new Vec2f (2.f / 32.f, 0.f / 32.f), new Vec2f (4.f / 32.f, 8.f / 32.f)));
        LIGHTNING_QUAD_INFO.add (
                new Pair (new Vec2f (6.f / 32.f, 0.f / 32.f), new Vec2f (6.f / 32.f, 8.f / 32.f)));
        LIGHTNING_QUAD_INFO.add (
                new Pair (new Vec2f (12.f / 32.f, 0.f / 32.f), new Vec2f (8.f / 32.f, 8.f / 32.f)));
        LIGHTNING_QUAD_INFO.add (
                new Pair (new Vec2f (0.f / 32.f, 16.f / 32.f), new Vec2f (20.f / 32.f, 8.f / 32.f)));
    };

    private static final Random random = new Random ();
    private static final Identifier LIGHTNING_TEXTURE = new Identifier (AlchymReference.MODID, "textures/bullet/lightning.png");

    public static void axisAlignedQuad (VertexConsumer consumer,
                                             MatrixStack stack, Vec3d startPoint, Vec3d axis, Vec3d eye, float quadWidth,
                                             Vec3i color, int light,
                                             float originU, float originV, float u, float v) {
        Vec3d toEye = eye.subtract (startPoint);
        Vec3d cross = toEye.crossProduct (axis).normalize ().multiply (quadWidth / 2.f);
        Vec3d endPoint = startPoint.add (axis);

        Vec3d corner1 = startPoint.add (cross);
        Vec3d corner2 = endPoint.add (cross);
        Vec3d corner3 = endPoint.subtract (cross);
        Vec3d corner4 = startPoint.subtract (cross);

        Matrix4f modelMatrix = stack.peek ().getModel ();

        float u1 = originU;
        float u2 = originU + u;
        float v1 = originV;
        float v2 = originV + v;

        consumer.vertex (modelMatrix, (float) corner1.x, (float) corner1.y, (float) corner1.z)
                .color (color.getX (), color.getY (), color.getZ (), 255)
                .texture (u1, v1)
                .overlay (OverlayTexture.DEFAULT_UV)
                .light (light)
                .normal (0.f, 0.f, 0.f)
                .next ();
        consumer.vertex (modelMatrix, (float) corner2.x, (float) corner2.y, (float) corner2.z)
                .color (color.getX (), color.getY (), color.getZ (), 255)
                .texture (u2, v1)
                .overlay (OverlayTexture.DEFAULT_UV)
                .light (light)
                .normal (0.f, 0.f, 0.f)
                .next ();
        consumer.vertex (modelMatrix, (float) corner3.x, (float) corner3.y, (float) corner3.z)
                .color (color.getX (), color.getY (), color.getZ (), 255)
                .texture (u2, v2)
                .overlay (OverlayTexture.DEFAULT_UV)
                .light (light)
                .normal (0.f, 0.f, 0.f)
                .next ();
        consumer.vertex (modelMatrix, (float) corner4.x, (float) corner4.y, (float) corner4.z)
                .color (color.getX (), color.getY (), color.getZ (), 255)
                .texture (u1, v2)
                .overlay (OverlayTexture.DEFAULT_UV)
                .light (light)
                .normal (0.f, 0.f, 0.f)
                .next ();
    }

    public static void renderLightning (MatrixStack stack, Vec3d start, Vec3d end, float tickDelta) {
        boolean alternate = tickDelta > 0.85;

        Vec3i color = alternate ? new Vec3i (255, 135, 190) : new Vec3i (255, 225, 255);

        random.setSeed ((int) ((MinecraftClient.getInstance ().player.age) / 2.f) + (int) (tickDelta * 2.f));

        VertexConsumerProvider.Immediate outlineProvider = MinecraftClient.getInstance ().getBufferBuilders ().getEffectVertexConsumers ();

        VertexConsumer consumer = outlineProvider.getBuffer (RenderLayer.getEnergySwirl (LIGHTNING_TEXTURE, 0.f, 0.f));
        Matrix4f modelMatrix = stack.peek ().getModel ();
        Matrix4f inverted = modelMatrix.copy ();
        inverted.invert ();

        Vector4f transformedEye = new Vector4f (0.f, 0.f, 0.f, 1.f);
        transformedEye.transform (inverted);
        Vec3d eye = new Vec3d (transformedEye.getX (), transformedEye.getY (), transformedEye.getZ ());

        Vec3d currentPoint = start;
        Vec3d dir = end.subtract (currentPoint);

        int iteration = 0;

        for (; dir.length () > LIGHTNING_STEPS [0]; ) {
            double indexD = random.nextDouble ();
            indexD *= indexD;
            int index = (int) Math.floor (indexD * 5.);
            if (iteration == 0)
                index = 4;

            if (LIGHTNING_STEPS [index] > dir.length ()) {
                for (index = 0; index < 4; ++ index) {
                    if (LIGHTNING_STEPS [index] > dir.length ())
                        break;
                }
            }

            Pair <Vec2f, Vec2f> quadInfo = LIGHTNING_QUAD_INFO.get (index);
            Vec2f textureOrigin = quadInfo.getLeft ();
            Vec2f textureSize   = quadInfo.getRight ();

            double minStep = LIGHTNING_STEPS [index];
            double step = Math.min (minStep, dir.length ());
            double stepCross = random.nextDouble () * step / LIGHTNING_STEPS [4] * 1.25;
            if (iteration == 0)
                stepCross /= 3.;
            double stepInDir = Math.sqrt (step * step - stepCross * stepCross);

            Vec3d displacement = new Vec3d (random.nextDouble () - 0.5, random.nextDouble () - 0.5, random.nextDouble () - 0.5)
                    .crossProduct (dir).normalize ().multiply (stepCross)
                    .add (dir.normalize ().multiply (stepInDir));
            Vec3d prevPoint = currentPoint;
            currentPoint = currentPoint.add (displacement);

            float xShift = ((float) MinecraftClient.getInstance ().player.age + tickDelta) / 15.f;

            if (alternate) {
                xShift = - xShift;
            }

            axisAlignedQuad (consumer,
                    stack, prevPoint, displacement, eye, 0.5f,
                    color, 15,
                    textureOrigin.x + (xShift % 2.f), textureOrigin.y,
                    textureSize.x * (float) (step / minStep), textureSize.y);

            dir = end.subtract (currentPoint);
            iteration ++;
        }
    }
}
