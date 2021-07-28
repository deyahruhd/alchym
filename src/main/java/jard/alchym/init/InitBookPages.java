package jard.alchym.init;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.api.book.BookPageStub;
import jard.alchym.api.book.impl.ContentPage;
import jard.alchym.api.book.impl.EmptyContentPage;
import jard.alchym.api.book.impl.NavigatorPage;
import jard.alchym.api.book.impl.TitlePage;
import jard.alchym.client.helper.BookHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/***
 *  InitBookPages
 *  The initializing module that initializes the pages of the guidebook.
 *
 *  Created by jard at 14:09 on December, 26, 2020.
 ***/
public class InitBookPages {
    protected Map <Identifier, BookPage> pageMap = new HashMap<> ();

    private final Gson GSON = new GsonBuilder ().create ();
    private final Identifier TITLE_ID = new Identifier (AlchymReference.MODID, "title");
    private final Identifier MAIN_ID = new Identifier (AlchymReference.MODID, "main");

    protected final InitAlchym alchym;

    private boolean pageTreeBuilt = false;

    public InitBookPages (InitAlchym alchym) {
        this.alchym = alchym;
    }

    public void lazyInitialize () {
        if (pageTreeBuilt) {
            pageMap.clear ();

            pageTreeBuilt = false;
        }

        _construct (MAIN_ID);

        register (new TitlePage (TITLE_ID, pageMap.get (MAIN_ID), new LiteralText[] {(LiteralText) LiteralText.EMPTY}));

        pageTreeBuilt = true;
    }

    /**
     * Post-order traverses through the page tree, as defined in the Alchym datapack,
     * and constructs every page.
     */
    private void _construct (Identifier root) {
        BookPageStub stub;
        try {
            BufferedReader reader = Alchym.getDataResource (new Identifier (root.getNamespace (), String.format ("book_pages/%s.json", root.getPath ())));
            stub = GSON.fromJson (reader, BookPageStub.class);
            reader.close ();
        } catch (IOException e) {
            throw new RuntimeException (String.format ("IO exception occurred while loading '%s.json'", root.toString ()), e);
        }

        switch (stub.type) {
            case NAVIGATOR:
                // Sanitize input JSON stub
                if (stub.nodes == null)
                    throw new JsonParseException ("Navigator page '" + stub.id + "' must have a 'nodes' field");

                List <Identifier> takenPages = new ArrayList<> ();
                List <Vec3i> takenPositions = new ArrayList<> ();

                for (NavigatorPage.NavigatorNode node : stub.nodes) {
                    // Round to nearest 0.5
                    node.x = Math.round (node.x * 2.0) / 2.0;
                    node.y = Math.round (node.y * 2.0) / 2.0;

                    Vec3i pos = new Vec3i (node.x, node.y, 0);

                    if (takenPages.contains (node.linkTo))
                        throw new JsonParseException ("Node '" + node.linkTo + "' was already parsed before");
                    if (takenPositions.contains (pos)) {
                        int ordinal = takenPositions.indexOf (pos);

                        throw new JsonParseException ("Node '" + node.linkTo + "' tried to occupy position (" + node.x + ", " + node.y + ")," +
                                " which was already taken by node '" + takenPages.get (ordinal)  + "'");
                    }

                    takenPages.add (node.linkTo);
                    takenPositions.add (pos);
                }

                List<BookPage> forwardlinkList = new ArrayList<> ();

                for (NavigatorPage.NavigatorNode node : stub.nodes) {
                    _construct (node.linkTo);
                    forwardlinkList.add (pageMap.get (node.linkTo));
                }

                BookPage[] forwardlinks = forwardlinkList.toArray (new BookPage [0]);

                NavigatorPage navigator = new NavigatorPage (root, stub.nodes, forwardlinks);

                // Append this new navigator to the end of every content page chain
                for (BookPage forwardlink : forwardlinks) {
                    if (forwardlink instanceof ContentPage)
                        ((ContentPage) forwardlink).appendNavigator (navigator);
                }

                register (navigator);
                break;
            case CONTENT:
                register (generateContentPages (stub));
                break;
            default:
                // Should never be reached.
                break;
        }
    }

    private ContentPage [] generateContentPages (BookPageStub stub) {
        // Step 1: Go from raw JSON to word-wrapped text.
        // Each content line in the JSON is parsed, word-wrapped, then added to the content.
        List <LiteralText> content = new ArrayList <> ();
        List <LiteralText> split = Arrays.asList (BookHelper.split (BookHelper.parseString (BookHelper.preprocess (stub.contents [0])),
                MinecraftClient.getInstance ().textRenderer, AlchymReference.PageInfo.PAGE_WIDTH * 2));

        content.addAll (split);

        if (stub.contents.length > 1) {
            for (String s : Arrays.copyOfRange (stub.contents, 1, stub.contents.length)) {
                split = Arrays.asList (BookHelper.split (BookHelper.parseString (BookHelper.preprocess (s)),
                        MinecraftClient.getInstance ().textRenderer, AlchymReference.PageInfo.PAGE_WIDTH * 2));

                LiteralText firstText = (LiteralText) split.get (0).getSiblings ().get (0);
                if (! BookHelper.shouldCullLeadingNewline (firstText))
                    content.add ((LiteralText) LiteralText.EMPTY);

                content.addAll (split);
            }
        }

        // Step 2: Pageify, then convert to ContentPages.
        // This simply iterates over every sub-array of LiteralTexts from the BookHelper#pagify function and uses them
        // as the content for a page, while linking successors and predecessors as needed.
        List <ContentPage> pages = new ArrayList<> ();
        LiteralText [][] pageContents = BookHelper.pageify (content, 23);

        // If the number of pages is odd, make them even with an empty page
        ContentPage successor = (pageContents.length % 2 == 1) ? new EmptyContentPage () : null;

        for (int i = pageContents.length - 1; i > -1; -- i) {
            String idSuffix = (i == 0) ? "" : String.format (".%d", i);
            Identifier subpageId = new Identifier (String.format ("%s%s", stub.id.toString (), idSuffix));

            ContentPage page = new ContentPage (subpageId, successor, pageContents [i], pageContents.length);
            pages.add (page);

            successor = page;
        }

        return pages.toArray (new ContentPage [0]);
    }

    private void register (BookPage ... pages) {
        for (BookPage page : pages) {
            pageMap.put (page.id, page);
        }
    }

    public BookPage get (Identifier id) {
        if (! pageTreeBuilt)
            lazyInitialize ();

        return pageMap.getOrDefault (id, pageMap.get (MAIN_ID));
    }
}
