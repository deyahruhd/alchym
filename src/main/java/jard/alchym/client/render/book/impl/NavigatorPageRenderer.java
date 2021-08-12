package jard.alchym.client.render.book.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.api.book.impl.ContentPage;
import jard.alchym.api.book.impl.NavigatorPage;
import jard.alchym.client.gui.screen.GuidebookScreen;
import jard.alchym.client.render.book.PageRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

/***
 *  NavigatorPageRenderer
 *  Implementation of {@link PageRenderer} for the {@link NavigatorPage} class.
 *
 *  Created by jard at 02:18 on January, 03, 2021.
 ***/
public class NavigatorPageRenderer extends PageRenderer <NavigatorPage> {
    private static final Identifier [] BORDERS = {
            new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.navigator.borders.1.png"),
            //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.navigator.borders.2.png"),
            //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.navigator.borders.3.png"),
            //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.navigator.borders.4.png"),
            //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.navigator.borders.5.png"),
            //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.navigator.borders.6.png"),
            //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.navigator.borders.7.png"),
    };

    @Override
    protected void render (MatrixStack stack, BookPage page, AlchymReference.PageInfo.BookSide side, int bookProgress,
                           TextureManager textures, TextRenderer font, ItemRenderer itemRenderer) {
        stack.push ();
        stack.scale (2, 2, 2);

        RenderSystem.setShaderTexture(0, BORDERS [bookProgress]);

        int x = 0, u = 0, height = 165;
        if (side == AlchymReference.PageInfo.BookSide.RIGHT) {
            x = 2;
            u = 120;

            if (page instanceof NavigatorPage && !page.id.equals (new Identifier (AlchymReference.MODID, "main"))) {
                height = 145;
            }
        }

        drawTexture (stack, x - 6, -10, u, 0, 120, height);

        stack.pop ();
    }
}
