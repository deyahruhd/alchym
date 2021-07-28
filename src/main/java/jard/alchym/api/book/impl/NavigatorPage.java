package jard.alchym.api.book.impl;

import com.google.gson.annotations.SerializedName;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.client.gui.screen.GuidebookScreen;
import jard.alchym.client.gui.widget.AbstractGuidebookWidget;
import jard.alchym.client.gui.widget.GuidebookNavigatorWidget;
import jard.alchym.client.gui.widget.GuidebookPageTurnWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

import java.util.List;

/***
 *  NavigatorPage
 *  Implementation of {@link BookPage}. Represents a quasi-infinite draggable field which spans
 *  the entire GUI. Interactive elements shall populate the field; each one corresponds to exactly one
 *  of the forward-links used to initialize the page.
 *
 *  The list of {@link BookPage}s this {@code NavigatorPage} links to, as well as the relative positioning of their
 *  corresponding elements, is defined by the JSON file pointed to by this {@code NavigatorPage}'s {@code id}.
 *
 *  Created by jard at 21:14 on December, 22, 2020.
 ***/
public class NavigatorPage extends BookPage {
    private NavigatorCenter center;

    private NavigatorNode [] nodes;

    public NavigatorPage (Identifier id, NavigatorNode [] nodes, BookPage... forwardlinks) throws IllegalArgumentException {
        super (id, forwardlinks);

        center = new NavigatorCenter (0, 0);
        this.nodes = nodes;
    }

    @Override
    public BookPage physicalNext () {
        // NavigatorPages do not have a successor page since they occupy both sides of the GUI.
        return this;
    }

    @Override
    @Environment (EnvType.CLIENT)
    public void populateWidgets (GuidebookScreen book, List<AbstractGuidebookWidget> widgets, AlchymReference.PageInfo.BookSide side) {
        widgets.add (new GuidebookNavigatorWidget (
                book,
                center,
                nodes,
                side,
                2, 6,
                AlchymReference.PageInfo.PAGE_WIDTH, 149, LiteralText.EMPTY));

        if (side == AlchymReference.PageInfo.BookSide.RIGHT && backlink instanceof NavigatorPage)
            widgets.add (new GuidebookPageTurnWidget (
                    book,
                    backlink,
                    GuidebookPageTurnWidget.ArrowDirection.RETURN,
                    AlchymReference.PageInfo.PAGE_WIDTH - 16 - 2,
                    AlchymReference.PageInfo.PAGE_HEIGHT - 9 - 7, 16, 9, LiteralText.EMPTY));
    }

    public static class NavigatorCenter {
        public float x, y;

        NavigatorCenter (float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class NavigatorNode {
        public enum Type {
            CURIO,
            MINOR,
            MAJOR,
            CAPSTONE
        }

        @SerializedName (value = "link-to")
        public final Identifier linkTo = null;
        public final Type type = null;

        public final Identifier icon = null;
        public final Identifier [] unlocks = null;

        public boolean hidden;

        public double x;
        public double y;
    }
}
