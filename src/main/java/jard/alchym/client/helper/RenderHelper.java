package jard.alchym.client.helper;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

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
}
