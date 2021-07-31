package jard.alchym.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.client.gui.screen.GuidebookScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/***
 *  GuidebookPageTurnWidget
 *  Page-turning widget, which jumps to a specific page when clicked.
 *
 *  Created by jard at 14:58 on January, 13, 2021.
 ***/

@Environment (EnvType.CLIENT)
public class GuidebookPageTurnWidget extends AbstractGuidebookWidget {
    private static final Identifier ARROWS = new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.content.arrows.png");

    public enum ArrowDirection {
        FORWARD,
        BACK,
        RETURN
    }

    private final BookPage pageToJump;

    private ArrowDirection dir;

    public GuidebookPageTurnWidget (GuidebookScreen book, BookPage pageToJump, ArrowDirection dir, int i, int j, int k, int l, Text text) {
        super (book, i, j, k, l, text);

        this.pageToJump = pageToJump;
        this.dir = dir;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int i, int j, float f) {
        RenderSystem.color4f (1.0F, 1.0F, 1.0F, 1.0F);

        textures.bindTexture (ARROWS);

        matrixStack.push ();
        matrixStack.translate (0.0, 0.0, 4.0);

        drawTexture (matrixStack, this.x, this.y, 0, this.dir.ordinal () * 9, 16, 9, 32, 32);

        matrixStack.pop ();
    }

    @Override
    public void onPress () {
        this.book.jumpToPage (pageToJump);
    }

    @Override
    public boolean addTooltip (List<Text> tooltip, double transformX, double transformY, int mouseX, int mouseY) {
        return false;
    }
}
