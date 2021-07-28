package jard.alchym.helper;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.items.MaterialItem;
import net.minecraft.item.ItemStack;

import java.util.*;

/***
 *  MaterialItemConversionHelper
 *  Contains helper methods to convert MaterialItems of a specific form into other MaterialItems of a form-equivalent but differing
 *  unit size MaterialItem.
 *
 *  Created by jard at 10:05 PM on February 01, 2019.
 ***/
public class MaterialItemConversionHelper {
    private static Map <AlchymReference.Materials.Forms, AlchymReference.Materials.Forms> formPairing = new HashMap<> ();
    private static List <AlchymReference.Materials.Forms> smallForms = new ArrayList<> ();
    private static void addFormPair (AlchymReference.Materials.Forms larger, AlchymReference.Materials.Forms smaller) {
        formPairing.put (larger, smaller);
        formPairing.put (smaller, larger);

        smallForms.add (smaller);
    }

    static {
        addFormPair (AlchymReference.Materials.Forms.INGOT, AlchymReference.Materials.Forms.NUGGET);
    }

    public static ItemStack convert (ItemStack target, AlchymReference.Materials.Forms to) {
        if (target == ItemStack.EMPTY || ! (target.getItem () instanceof MaterialItem)) return target;
        if (((MaterialItem) target.getItem ()).form == to) return target;
        if (formPairing.get (((MaterialItem) target.getItem ()).form) != to) return target;

        int ratio = to.conversionFactor;

        // Large form to small form case: Simply multiply the stack size of target by the appropriate ratio, reassign the appropriate MaterialItem, and return
        if (smallForms.contains (to))
            return new ItemStack (Alchym.content ().items.getMaterial (((MaterialItem) target.getItem ()).material, to), target.getCount () * ratio);
        // Small form to large form case: This is a bit more complicated. We obviously can not convert a stack that doesn't have enough small units into a single large one,
        // and we can not convert stacks with "excess" small units into larger units since this would require 2 ItemStacks to be returned.
        // So, we just see IF the small units can be cleanly divided by the ratio, and return the appropriate large unit if they are;
        // otherwise return the same stack back.
        else if (target.getCount () % ratio == 0)
            return new ItemStack (Alchym.content ().items.getMaterial (((MaterialItem) target.getItem ()).material, to), target.getCount () / ratio);

        return target;
    }

    // Produces a new stack consisting of lhs - rhs's counts, converted to an appropriate form.
    public static ItemStack subtractStacks (ItemStack lhs, ItemStack rhs) {
        if (! (lhs.getItem () instanceof MaterialItem) || ! (rhs.getItem () instanceof MaterialItem)) return ItemStack.EMPTY;
        if (lhs == ItemStack.EMPTY) return ItemStack.EMPTY;
        if (rhs == ItemStack.EMPTY) return lhs;

        if (((MaterialItem) lhs.getItem ()).form == ((MaterialItem) rhs.getItem ()).form) {
            if (rhs.getCount () > lhs.getCount ()) return ItemStack.EMPTY;

            ItemStack ret = lhs.copy ();
            ret.setCount (lhs.getCount () - rhs.getCount ());

            return ret;
        }

        MaterialItem lhsItem = (MaterialItem) lhs.getItem ();
        MaterialItem rhsItem = (MaterialItem) rhs.getItem ();

        if (lhsItem.material != rhsItem.material) return ItemStack.EMPTY;
        if (formPairing.get (lhsItem.form) != rhsItem.form) return ItemStack.EMPTY;

        int ratio = ((MaterialItem) lhs.getItem ()).form.conversionFactor;

        // Determine the "best" unit for the resulting subtracted stack. This can be done by converting the large unit stack into small units and subtracting the
        // small unit stack's count. If the result amount is cleanly divisible by the ratio, use the larger unit, or the smaller unit otherwise.
        AlchymReference.Materials.Forms selectedForm, smallForm;

        int unitCount = 0;
        if (smallForms.contains (lhsItem.form)) {
            unitCount -= rhs.getCount () * ratio;
            unitCount += lhs.getCount ();

            smallForm = lhsItem.form;
        } else {
            unitCount -= rhs.getCount ();
            unitCount += lhs.getCount () * ratio;

            smallForm = rhsItem.form;
        }

        if (unitCount <= 0) return ItemStack.EMPTY;

        if (unitCount % ratio == 0) {
            selectedForm = formPairing.get (smallForm);
            unitCount /= ratio;
        } else {
            selectedForm = smallForm;
        }

        return new ItemStack (Alchym.content ().items.getMaterial (lhsItem.material, selectedForm), unitCount);
    }

    // Returns a new ItemStack with similar form to the base whose volume is the largest possible volume that is less than the provided volume.
    public static ItemStack matchVolume (ItemStack base, long volume) {
        if (base == ItemStack.EMPTY || ! (base.getItem () instanceof MaterialItem)) return ItemStack.EMPTY;

        AlchymReference.Materials baseMaterial = ((MaterialItem) base.getItem ()).material;
        AlchymReference.Materials.Forms smallForm;

        if (smallForms.contains (((MaterialItem) base.getItem ()).form)) {
            smallForm = ((MaterialItem) base.getItem ()).form;
        } else {
            smallForm = formPairing.get (((MaterialItem) base.getItem ()).form);
        }

        int ratio = smallForm.conversionFactor;
        int finalCount = (int) (volume / smallForm.volume);

        if (finalCount % ratio == 0)
            return new ItemStack (Alchym.content ().items.getMaterial (baseMaterial, formPairing.get (smallForm)), finalCount / ratio);
        else
            return new ItemStack (Alchym.content ().items.getMaterial (baseMaterial, smallForm), finalCount);
    }

    public static ItemStack mergeStacks (ItemStack lhs, ItemStack rhs) {
        if (lhs == ItemStack.EMPTY || rhs == ItemStack.EMPTY) return ItemStack.EMPTY;
        if (! (lhs.getItem () instanceof MaterialItem) || ! (rhs.getItem () instanceof MaterialItem)) return ItemStack.EMPTY;

        MaterialItem lhsItem = (MaterialItem) lhs.getItem ();
        MaterialItem rhsItem = (MaterialItem) rhs.getItem ();

        if (lhsItem.material != rhsItem.material) return ItemStack.EMPTY;
        if (formPairing.get (lhsItem.form) != rhsItem.form) return ItemStack.EMPTY;

        int ratio = ((MaterialItem) lhs.getItem ()).form.conversionFactor;

        // Determine the "best" unit for the resulting merged stack. This can be done by converting the large unit stack into small units and adding it with the
        // small unit stack. If the result amount is cleanly divisible by the ratio, use the larger unit, or the smaller unit otherwise.

        AlchymReference.Materials.Forms selectedForm, smallForm;

        int unitCount = 0;
        if (smallForms.contains (lhsItem.form)) {
            unitCount += rhs.getCount() * ratio;
            unitCount += lhs.getCount ();

            smallForm = lhsItem.form;
        } else {
            unitCount += rhs.getCount ();
            unitCount += lhs.getCount () * ratio;

            smallForm = rhsItem.form;
        }

        if (unitCount % ratio == 0) {
            selectedForm = formPairing.get (smallForm);
            unitCount /= ratio;
        } else {
            selectedForm = smallForm;
        }

        return new ItemStack (Alchym.content ().items.getMaterial (lhsItem.material, selectedForm), unitCount);
    }
}
