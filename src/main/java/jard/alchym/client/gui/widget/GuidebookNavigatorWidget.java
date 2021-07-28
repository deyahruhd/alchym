package jard.alchym.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.impl.NavigatorPage;
import jard.alchym.client.gui.screen.GuidebookScreen;
import jard.alchym.client.helper.BookHelper;
import jard.alchym.client.helper.RenderHelper;
import jard.alchym.client.render.book.impl.NavigatorPageRenderer;
import jard.alchym.helper.MathHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.List;

/***
 *  GuidebookNavigatorWidget
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 00:36 on January, 16, 2021.
 ***/
public class GuidebookNavigatorWidget extends AbstractGuidebookWidget {
    private static final Identifier ELEMENTS = new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.navigator.elements.png");

    private static final Vec3i OFFSET = new Vec3i (8, 9, 0);

    private static final int HORIZONTAL_NODE_SPACING = 52;
    private static final int VERTICAL_NODE_SPACING = 48;

    private final NavigatorPage.NavigatorCenter center;
    private final NavigatorPage.NavigatorNode [] nodes;
    private final int horizontalOffset;
    private final int stencilBit;

    public GuidebookNavigatorWidget (GuidebookScreen book, NavigatorPage.NavigatorCenter center, NavigatorPage.NavigatorNode [] nodes, AlchymReference.PageInfo.BookSide side, int i, int j, int k, int l, Text text) {
        super (book, i, j, k, l, text);

        this.center = center;
        this.nodes = nodes;
        horizontalOffset = (side == AlchymReference.PageInfo.BookSide.LEFT) ? (k - 4) : 0;
        stencilBit = (side == AlchymReference.PageInfo.BookSide.LEFT) ? 0x01 : 0x02;
    }

    @Override
    public void onPress () {

    }

    @Override
    protected void onDrag (double x, double y, double deltaX, double deltaY) {
        this.center.x += deltaX;
        this.center.y += deltaY;
    }

    @Override
    public boolean mouseClicked (double d, double e, int i) {
        for (NavigatorPage.NavigatorNode node : nodes) {
            int nodePosX = this.x + (int) (node.x * HORIZONTAL_NODE_SPACING) + OFFSET.getX () + (int) center.x + horizontalOffset;
            int nodePosY = this.y + (int) (node.y * VERTICAL_NODE_SPACING) + OFFSET.getY () + (int) center.y;

            if (MathHelper.inRange ((float) d, (float) nodePosX + 4.f, (float) nodePosX + 28.f) &&
                    MathHelper.inRange ((float) e, (float) nodePosY + 4.f, (float) nodePosY + 28.f)) {

                book.jumpToPage (Alchym.content ().pages.get (node.linkTo));

                // Cancel the mouse click event
                return false;
            }
        }

        return true;
    }

    @Override
    public void renderButton(MatrixStack stack, int i, int j, float f) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glEnable (GL11.GL_STENCIL_TEST);

        GlStateManager.colorMask (false, false, false, false);
        GlStateManager.stencilMask (stencilBit);
        GlStateManager.stencilFunc (GL11.GL_ALWAYS, stencilBit, stencilBit);
        GlStateManager.stencilOp (GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        textures.bindTexture (new Identifier ("missingno"));

        // Draw a non-textured quad onto the stencil buffer
        drawTexture (stack, this.x, this.y, 0, 0, 106, 149);

        GlStateManager.colorMask (true, true, true, true);
        GlStateManager.stencilMask (0x0);
        GlStateManager.stencilFunc (GL11.GL_EQUAL, stencilBit, stencilBit);
        GlStateManager.stencilOp (GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

        // Draw the nodes

        for (NavigatorPage.NavigatorNode node : nodes) {
            int nodeAbsPosX = (int) (node.x * HORIZONTAL_NODE_SPACING) + OFFSET.getX () + (int) center.x + horizontalOffset;
            int nodeAbsPosY = (int) (node.y * VERTICAL_NODE_SPACING) + OFFSET.getY () + (int) center.y;

            int texXShift = (node.type.ordinal () & (0x00000002)) >> 1;
            int texYShift = (node.type.ordinal () & (0x00000001));

            ItemStack icon = new ItemStack (Registry.ITEM.get (node.icon));

            textures.bindTexture (ELEMENTS);

            drawTexture (stack, this.x + nodeAbsPosX, this.y + nodeAbsPosY,
                    32 * texXShift, 32 * texYShift,
                    32, 32, 64, 64);

            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance ().getBufferBuilders ().getEntityVertexConsumers ();

            RenderHelper.renderGuiItem (stack, icon, this.x + nodeAbsPosX + 8, this.y + nodeAbsPosY + 8, itemRenderer, textures);
        }

        GlStateManager.clear (GL11.GL_STENCIL_BUFFER_BIT, false);

        GL11.glDisable (GL11.GL_STENCIL_TEST);
    }

    @Override
    public boolean addTooltip (List<Text> tooltip, double transformX, double transformY, int mouseX, int mouseY) {
        for (NavigatorPage.NavigatorNode node : nodes) {
            int nodePosX = (int) (node.x * HORIZONTAL_NODE_SPACING) + OFFSET.getX () + (int) center.x + horizontalOffset;
            int nodePosY = (int) (node.y * VERTICAL_NODE_SPACING) + OFFSET.getY () + (int) center.y;

            if (MathHelper.inRange ((float) transformX, (float) nodePosX + 4.f, (float) nodePosX + 32.f) &&
                    MathHelper.inRange ((float) transformY, (float) nodePosY + 8.f, (float) nodePosY + 36.f)) {
                tooltip.add (new TranslatableText ("tooltip." + node.linkTo.getNamespace () + "." + node.linkTo.getPath ()));
                return true;
            }
        }
        return false;
    }
}
