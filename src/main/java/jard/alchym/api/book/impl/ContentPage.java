package jard.alchym.api.book.impl;

import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.client.gui.screen.GuidebookScreen;
import jard.alchym.client.gui.widget.AbstractGuidebookWidget;
import jard.alchym.client.gui.widget.GuidebookPageTurnWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.List;

/***
 *  ContentPage
 *  Implementation of {@link BookPage}. Acts as the node for a doubly-linked list of pages that
 *  represent an entry in the guidebook.
 *
 *  Created by jard at 19:37 on December, 26, 2020.
 ***/
public class ContentPage extends BookPage {
    public final LiteralText[] content;

    public final int pagesInSequence;

    public ContentPage (Identifier id, BookPage next, LiteralText [] content, int pagesInSequence) throws IllegalArgumentException {
        super (id, next);

        this.content = content;
        this.pagesInSequence = pagesInSequence;
    }

    @Override
    public BookPage physicalNext () {
        if (forwardlinks == null || forwardlinks.size () == 0)
            return null;

        return forwardlinks.values ().toArray (new BookPage [0]) [0];
    }

    public void appendNavigator (NavigatorPage page) {
        if (physicalNext () != null && physicalNext () instanceof ContentPage) {
            ((ContentPage) physicalNext ()).appendNavigator (page);
        } else if (physicalNext () == null) {
            forwardlinks.put (page.id, page);
        }
    }

    @Override
    @Environment (EnvType.CLIENT)
    public void populateWidgets (GuidebookScreen book, List<AbstractGuidebookWidget> widgets, AlchymReference.PageInfo.BookSide side) {
        BookPage pageToJump = null;
        GuidebookPageTurnWidget.ArrowDirection dir = GuidebookPageTurnWidget.ArrowDirection.FORWARD;

        if (side == AlchymReference.PageInfo.BookSide.LEFT) {
            pageToJump = this.backlink;

            // If the previous page is a ContentPage, then we would land on the right side of the book, so
            // we want to continue one more step
            if (pageToJump instanceof ContentPage)
                pageToJump = ((ContentPage) pageToJump).backlink;

            dir = GuidebookPageTurnWidget.ArrowDirection.BACK;

            // If there are only 2 pages in this content page's chapter, then both page turn widgets would jump to the
            // same NavigatorPage

            // So, we want to discard the left page's button.
            if (pageToJump instanceof NavigatorPage && pagesInSequence <= 2)
                pageToJump = null;
        } else if (side == AlchymReference.PageInfo.BookSide.RIGHT) {
            pageToJump = this.physicalNext ();
            if (pageToJump instanceof NavigatorPage)
                dir = GuidebookPageTurnWidget.ArrowDirection.RETURN;
        }

        if (pageToJump != null) {
            GuidebookPageTurnWidget turnArrow = new GuidebookPageTurnWidget (
                    book,
                    pageToJump,
                    dir,
                    side == AlchymReference.PageInfo.BookSide.LEFT ? 2 : AlchymReference.PageInfo.PAGE_WIDTH - 16 - 2,
                    AlchymReference.PageInfo.PAGE_HEIGHT - 9 - 7, 16, 9, LiteralText.EMPTY);

            widgets.add (turnArrow);
        }
    }
}
