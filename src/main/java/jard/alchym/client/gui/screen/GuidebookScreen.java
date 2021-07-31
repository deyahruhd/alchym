package jard.alchym.client.gui.screen;

import io.netty.buffer.Unpooled;
import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.book.BookPage;
import jard.alchym.client.MatrixStackAccess;
import jard.alchym.client.gui.widget.AbstractGuidebookWidget;
import jard.alchym.client.helper.BookHelper;
import jard.alchym.client.helper.RenderHelper;
import net.fabricmc.fabric.impl.networking.ClientSidePacketRegistryImpl;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuidebookScreen extends Screen {
    private static final Identifier [] BOOK_TEXTURE = {
        new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.1.png"),
        //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.2.png"),
        //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.3.png"),
        new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.4.png"),
        //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.5.png"),
        //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.6.png"),
        //new Identifier (AlchymReference.MODID, "textures/gui/alchymic_reference.7.png"),
    };

    private static final List<Pair <Vec2f, Vec2f>> PAGE_COORDINATES          = new ArrayList<> ();
    private static final List<Pair <Matrix4f, Matrix4f>> PAGE_SHEARS         = new ArrayList<> ();
    private static final List<Pair <Matrix4f, Matrix4f>> PAGE_SHEAR_INVERSES = new ArrayList<> ();

    static {
        // #1
        addPageTransform (new Vec2f (77.f, 60.f), new Vec2f (343.f, 34.f), RenderHelper.yShear (2.35f), RenderHelper.IDENTITY_MATRIX);
        // #2
        //addPageTransform (???);
        // #3
        //addPageTransform (???);
        // #4
        addPageTransform (new Vec2f (72.f, 32.f), new Vec2f (350.f, 32.f), RenderHelper.IDENTITY_MATRIX, RenderHelper.IDENTITY_MATRIX);
        // #5
        //addPageTransform (???);
        // #6
        //addPageTransform (???);
        // #7
        //addPageTransform (???);
    }

    private static void addPageTransform (Vec2f coordLeft, Vec2f coordRight, Matrix4f shearLeft, Matrix4f shearRight) {
        Matrix4f inverseLeft = shearLeft.copy ();
        inverseLeft.invert ();
        Matrix4f inverseRight = shearRight.copy ();
        inverseRight.invert ();
        PAGE_COORDINATES.add (new Pair <> (coordLeft, coordRight));
        PAGE_SHEARS.add (new Pair <> (shearLeft, shearRight));
        PAGE_SHEAR_INVERSES.add (new Pair <> (inverseLeft, inverseRight));
    }

    private List <AbstractGuidebookWidget> leftPageWidgets  = new CopyOnWriteArrayList<> ();
    private List <AbstractGuidebookWidget> rightPageWidgets = new CopyOnWriteArrayList<> ();

    private int bookProgress;
    private BookPage currentPage;

    private Vec2f previousMouse;

    public GuidebookScreen (BookPage page, Text text) {
        super (text);

        currentPage = page;
        bookProgress = 0;
    }

    public void init () {
        leftPageWidgets.clear ();
        rightPageWidgets.clear ();

        BookPage left  = currentPage;
        BookPage right = currentPage.physicalNext ();

        left.populateWidgets (this, leftPageWidgets, AlchymReference.PageInfo.BookSide.LEFT);
        right.populateWidgets (this, rightPageWidgets, AlchymReference.PageInfo.BookSide.RIGHT);
    }

    public void jumpToPage (BookPage page) {
        currentPage = page;
        bookProgress = 0;

        init ();
        syncPage ();
    }

    public void render (MatrixStack stack, int i, int j, float f) {
        this.renderBackground (stack);

        this.client.getTextureManager ().bindTexture (BOOK_TEXTURE [bookProgress]);
        drawTexture(stack, (int) (((float) this.width - 320.f) / 2.f), 8, 0, 0, 320, 208, 512, 512);

        Pair <Vec2f, Vec2f>       pageCoords = PAGE_COORDINATES.get (bookProgress);
        Pair <Matrix4f, Matrix4f> pageShears = PAGE_SHEARS.get (bookProgress);

        stack.scale (0.5f, 0.5f, 0.5f);

        // Left page
        stack.push ();
        stack.translate (this.width - 320 + pageCoords.getLeft ().x, pageCoords.getLeft ().y, 0.f);
        ((MatrixStackAccess) stack).multiply (pageShears.getLeft ());

        stack.scale (2.f, 2.f, 2.f);

        for (AbstractGuidebookWidget widget : leftPageWidgets) {
            widget.renderButton (stack, i, j, f);
        }

        stack.scale (0.5f, 0.5f, 0.5f);
        stack.translate (0.f, 16.f, 0.f);

        Alchym.getProxy ().renderPage (stack, currentPage, AlchymReference.PageInfo.BookSide.LEFT, bookProgress);

        stack.pop ();

        // Right page
        stack.push ();
        stack.translate (this.width - 320 + pageCoords.getRight ().x, pageCoords.getRight ().y, 0.f);
        ((MatrixStackAccess) stack).multiply (pageShears.getRight ());

        stack.scale (2.f, 2.f, 2.f);

        for (AbstractGuidebookWidget widget : rightPageWidgets) {
            widget.renderButton (stack, i, j, f);
        }

        stack.scale (0.5f, 0.5f, 0.5f);
        stack.translate (0.f, 16.f, 0.f);

        Alchym.getProxy ().renderPage (stack, currentPage.physicalNext (), AlchymReference.PageInfo.BookSide.RIGHT, bookProgress);

        stack.pop ();

        stack.push ();
        stack.scale (2.f, 2.f, 2.f);

        renderMouseoverTooltip (stack, i, j);

        stack.pop ();
    }



    @Override
    public boolean mouseClicked (double d, double e, int i) {
        Pair <Vec2f, Vec2f>       pageCoords = PAGE_COORDINATES.get (bookProgress);
        Pair <Matrix4f, Matrix4f> pageShearInverses = PAGE_SHEAR_INVERSES.get (bookProgress);

        Pair <Vec2f, Vec2f> transformCoords = transformMouseCoords (d, e, pageCoords, pageShearInverses);

        if (BookHelper.withinPageBounds (transformCoords.getLeft ().x, transformCoords.getLeft ().y))
            return clickPageWidgets (leftPageWidgets, transformCoords.getLeft ().x, transformCoords.getLeft ().y, i);
        else if (BookHelper.withinPageBounds (transformCoords.getRight ().x, transformCoords.getRight ().y))
            return clickPageWidgets (rightPageWidgets, transformCoords.getRight ().x, transformCoords.getRight ().y, i);

        return false;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        previousMouse = null;

        return super.mouseReleased (d, e, i);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (previousMouse == null)
            previousMouse = new Vec2f ((float) d, (float) e);

        Pair <Vec2f, Vec2f>       pageCoords = PAGE_COORDINATES.get (bookProgress);
        Pair <Matrix4f, Matrix4f> pageShearInverses = PAGE_SHEAR_INVERSES.get (bookProgress);

        Pair <Vec2f, Vec2f> transformCoords = transformMouseCoords (d, e, pageCoords, pageShearInverses);

        Vector4f transformedLeftDelta  = new Vector4f ((float) d - previousMouse.x, (float) e - previousMouse.y, 0.f, 0.f);
        Vector4f transformedRightDelta = new Vector4f ((float) d - previousMouse.x, (float) e - previousMouse.y, 0.f, 0.f);

        transformedLeftDelta.transform  (pageShearInverses.getLeft ());
        transformedRightDelta.transform (pageShearInverses.getRight ());

        previousMouse = new Vec2f ((float) d, (float) e);

        if (BookHelper.withinPageBounds (transformCoords.getLeft ().x, transformCoords.getLeft ().y))
            return dragPageWidgets (leftPageWidgets, transformCoords.getLeft ().x, transformCoords.getLeft ().y, i,
                    transformedLeftDelta.getX (), transformedLeftDelta.getY ());
        else if (BookHelper.withinPageBounds (transformCoords.getRight ().x, transformCoords.getRight ().y))
            return dragPageWidgets (rightPageWidgets, transformCoords.getRight ().x, transformCoords.getRight ().y, i,
                    transformedRightDelta.getX (), transformedRightDelta.getY ());

        return false;
    }

    private boolean clickPageWidgets (List <AbstractGuidebookWidget> widgets, double x, double y, int i) {
        AbstractGuidebookWidget focusWidget = null;
        for (AbstractGuidebookWidget widget : widgets) {
            if (! widget.mouseClicked (x, y, i))
                return false;

            focusWidget = widget;
        }

        this.setFocused (focusWidget);
        if (i == 0)
            this.setDragging (true);

        return true;
    }

    private boolean dragPageWidgets (List <AbstractGuidebookWidget> widgets, double x, double y, int i, double delX, double delY) {
        AbstractGuidebookWidget focusWidget = null;
        for (AbstractGuidebookWidget widget : widgets) {
            if (! widget.mouseDragged (x, y, i, delX, delY))
                return false;

            focusWidget = widget;
        }

        this.setFocused (focusWidget);
        if (i == 0)
            this.setDragging (true);

        return true;
    }

    private void renderMouseoverTooltip (MatrixStack stack, int mouseX, int mouseY) {
        List <Text> tooltip = new ArrayList<> ();

        Pair <Vec2f, Vec2f>       pageCoords = PAGE_COORDINATES.get (bookProgress);
        Pair <Matrix4f, Matrix4f> pageShearInverses = PAGE_SHEAR_INVERSES.get (bookProgress);

        Pair <Vec2f, Vec2f> transformCoords = transformMouseCoords (mouseX, mouseY, pageCoords, pageShearInverses);

        boolean flag =
                (BookHelper.withinPageBounds (transformCoords.getLeft ().x, transformCoords.getLeft ().y)
                        &&
                setTooltip (leftPageWidgets, tooltip, transformCoords.getLeft ().x, transformCoords.getLeft ().y, mouseX, mouseY))

                        ||

                (BookHelper.withinPageBounds (transformCoords.getRight ().x, transformCoords.getRight ().y)
                        &&
                setTooltip (rightPageWidgets, tooltip, transformCoords.getRight ().x, transformCoords.getRight ().y, mouseX, mouseY));

        if (flag)
            this.renderTooltip (stack, tooltip, mouseX, mouseY);
    }

    private boolean setTooltip (List <AbstractGuidebookWidget> widgets, List <Text> tooltip, double transformX, double transformY,
                                int mouseX, int mouseY) {
        for (AbstractGuidebookWidget widget : widgets) {
            if (widget.addTooltip (tooltip, transformX, transformY, mouseX, mouseY))
                return true;
        }

        return false;
    }

    private Pair <Vec2f, Vec2f> transformMouseCoords (double d, double e, Pair <Vec2f, Vec2f> pageCoords,
                                                            Pair <Matrix4f, Matrix4f> pageShearInverses) {
        Vector4f mouseCoords = new Vector4f ((float) d, (float) e, 0.f, 0.f);

        Vector4f leftTransformCoords = new Vector4f (
                mouseCoords.getX () - (this.width - 320 + pageCoords.getLeft ().x) / 2.f,
                mouseCoords.getY () - (pageCoords.getLeft ().y) / 2.f,
                0.f, 0.f);
        leftTransformCoords.transform (pageShearInverses.getLeft ());

        Vector4f rightTransformCoords = new Vector4f (
                mouseCoords.getX () - (this.width - 320 + pageCoords.getRight ().x) / 2.f,
                mouseCoords.getY () - (pageCoords.getRight ().y) / 2.f,
                0.f, 0.f);
        rightTransformCoords.transform (pageShearInverses.getRight ());

        return new Pair<> (new Vec2f (leftTransformCoords.getX (), leftTransformCoords.getY ()),
                new Vec2f (rightTransformCoords.getX (), rightTransformCoords.getY ()));
    }

     @Override
    public void onClose() {
        syncPage ();
        super.onClose ();
    }

    private void syncPage () {
        PacketByteBuf data = new PacketByteBuf (Unpooled.buffer());
        data.writeIdentifier (currentPage.id);

        ClientSidePacketRegistryImpl.INSTANCE.sendToServer (AlchymReference.Packets.SYNC_GUIDEBOOK.id, data);
    }
}
