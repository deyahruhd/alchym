package jard.alchym.client.helper;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jard.alchym.AlchymReference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
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
    private static final Identifier LIGHTNING_TEXTURE = new Identifier (AlchymReference.MODID, "textures/lightning.png");

    public static void renderLightning (MatrixStack stack, Vec3d start, Vec3d end, float tickDelta) {
        boolean alternate = tickDelta > 0.85;

        Vec3i color = alternate ? new Vec3i (255, 135, 190) : new Vec3i (255, 225, 255);

        random.setSeed ((int) ((MinecraftClient.getInstance ().player.age) / 2.f) + (int) (tickDelta * 2.f));

        VertexConsumerProvider.Immediate outlineProvider = MinecraftClient.getInstance ().getBufferBuilders ().getEffectVertexConsumers ();

        VertexConsumer consumer = outlineProvider.getBuffer (RenderLayer.getEnergySwirl (LIGHTNING_TEXTURE, 0.f, 0.f));
        Matrix4f modelMatrix = stack.peek ().getModel ();
        Matrix3f normalMatrix = stack.peek ().getNormal ();
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
            Vec3d toEye = eye.subtract (currentPoint);
            Vec3d cross = toEye.crossProduct (displacement).normalize ().multiply (0.5);
            Vec3d prevPoint = currentPoint;
            currentPoint = currentPoint.add (displacement);

            Vec3d corner1 = currentPoint.add (cross);
            Vec3d corner2 = prevPoint.add (cross);
            Vec3d corner3 = prevPoint.subtract (cross);
            Vec3d corner4 = currentPoint.subtract (cross);

            float xShift = ((float) MinecraftClient.getInstance ().player.age + tickDelta) / 15.f;

            if (alternate) {
                xShift = - xShift;
            }

            float u1 = textureOrigin.x + (xShift % 2.f);
            float u2 = textureOrigin.x + (textureSize.x * (float) (step / minStep)) + (xShift % 2.f);
            float v1 = textureOrigin.y;
            float v2 = textureOrigin.y + textureSize.y;

            double flipChance = random.nextDouble ();
            if (alternate) {
                v1 = v2;
                v2 = textureOrigin.y;
            }

            consumer.vertex (modelMatrix, (float) corner1.x, (float) corner1.y, (float) corner1.z)
                    .color (color.getX (), color.getY (), color.getZ (), 255)
                    .texture (u1, v1)
                    .overlay (OverlayTexture.DEFAULT_UV)
                    .light (15)
                    .normal (0.f, 0.f, 0.f)
                    .next ();
            consumer.vertex (modelMatrix, (float) corner2.x, (float) corner2.y, (float) corner2.z)
                    .color (color.getX (), color.getY (), color.getZ (), 255)
                    .texture (u2, v1)
                    .overlay (OverlayTexture.DEFAULT_UV)
                    .light (15)
                    .normal (0.f, 0.f, 0.f)
                    .next ();
            consumer.vertex (modelMatrix, (float) corner3.x, (float) corner3.y, (float) corner3.z)
                    .color (color.getX (), color.getY (), color.getZ (), 255)
                    .texture (u2, v2)
                    .overlay (OverlayTexture.DEFAULT_UV)
                    .light (15)
                    .normal (0.f, 0.f, 0.f)
                    .next ();
            consumer.vertex (modelMatrix, (float) corner4.x, (float) corner4.y, (float) corner4.z)
                    .color (color.getX (), color.getY (), color.getZ (), 255)
                    .texture (u1, v2)
                    .overlay (OverlayTexture.DEFAULT_UV)
                    .light (15)
                    .normal (0.f, 0.f, 0.f)
                    .next ();

            dir = end.subtract (currentPoint);
            iteration ++;
        }
    }
}
