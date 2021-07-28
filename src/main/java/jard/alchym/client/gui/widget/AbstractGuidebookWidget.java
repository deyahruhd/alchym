package jard.alchym.client.gui.widget;

import jard.alchym.client.gui.screen.GuidebookScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.text.Text;

import java.util.List;

/***
 *  AbstractGuidebookWidget
 *  The abstract widget class used solely by the Alchymic Reference.
 *  All widgets that are to be used in the guidebook GUI must be subclasses of this class.
 *
 *  Created by jard at 23:27 on January, 14, 2021.
 ***/
public abstract class AbstractGuidebookWidget extends AbstractPressableButtonWidget {
    protected final GuidebookScreen book;

    protected final TextureManager textures;
    protected final TextRenderer textRenderer;
    protected final ItemRenderer itemRenderer;

    public AbstractGuidebookWidget (GuidebookScreen book, int i, int j, int k, int l, Text text) {
        super (i, j, k, l, text);

        this.book = book;

        textures = MinecraftClient.getInstance ().getTextureManager ();
        textRenderer = MinecraftClient.getInstance ().textRenderer;
        itemRenderer = MinecraftClient.getInstance ().getItemRenderer ();
    }

    public abstract boolean addTooltip (List<Text> tooltip, double transformX, double transformY, int mouseX, int mouseY);
}
