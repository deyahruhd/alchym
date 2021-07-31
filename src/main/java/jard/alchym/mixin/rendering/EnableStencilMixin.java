package jard.alchym.mixin.rendering;

import com.mojang.blaze3d.platform.FramebufferInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.TextureUtil;
import org.lwjgl.opengl.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

/***
 *  EnableStencilMixin
 *  Replaces the default 32-bit depth buffer in Minecraft's FBO with a 24-bit depth 8-bit stencil buffer. This
 *  effectively enables stencil testing.
 *
 *  File created by jard at 02:33 on January, 17, 2021.
 ***/

@Mixin(Framebuffer.class)
public abstract class EnableStencilMixin {
    @Shadow
    private int colorAttachment;

    @Shadow
    private int depthAttachment;

    @Shadow
    @Final
    public boolean useDepthAttachment;

    @Shadow
    public int fbo;

    @Shadow
    public void setTexFilter (int i) {}

    @Shadow
    public void checkFramebufferStatus() {}

    @Shadow
    public void clear(boolean bl) {}

    @Shadow
    public void endRead() {}

    @Inject (
            method = "initFbo",
            at = @At (
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/TextureUtil;generateId()I",
                    ordinal = 0),
            cancellable = true)
    public void initAttachments (int width, int height, boolean clear, CallbackInfo info) {
        info.cancel ();

        this.colorAttachment = TextureUtil.generateId();

        if (this.useDepthAttachment) {
            // Initialize a depth-stencil buffer instead
            this.depthAttachment = TextureUtil.generateId();
            GlStateManager.bindTexture(this.depthAttachment);
            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, 0);
            GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, ARBFramebufferObject.GL_DEPTH24_STENCIL8,
                    width, height, 0,
                    ARBFramebufferObject.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8, null);
        }

        this.setTexFilter (GL11.GL_NEAREST);
        GlStateManager.bindTexture (this.colorAttachment);
        GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null); // 6408 5121
        GlStateManager.bindFramebuffer(FramebufferInfo.FRAME_BUFFER, this.fbo);
        GlStateManager.framebufferTexture2D(FramebufferInfo.FRAME_BUFFER, FramebufferInfo.COLOR_ATTACHMENT, 3553, this.colorAttachment, 0);
        if (this.useDepthAttachment) {
            GlStateManager.framebufferTexture2D(FramebufferInfo.FRAME_BUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, 3553, this.depthAttachment, 0);
        }

        finishInitializing (clear);
    }

    private void finishInitializing (boolean clear) {
        this.checkFramebufferStatus ();
        this.clear (clear);
        this.endRead ();
    }
}