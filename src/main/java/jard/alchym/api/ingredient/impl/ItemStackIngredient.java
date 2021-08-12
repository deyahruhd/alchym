package jard.alchym.api.ingredient.impl;

import jard.alchym.api.ingredient.SolubleIngredient;
import jard.alchym.api.ingredient.Ingredient;
import jard.alchym.api.ingredient.IngredientGroup;
import jard.alchym.helper.MaterialItemConversionHelper;
import jard.alchym.items.MaterialItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/***
 *  ItemStackIngredient
 *  An implementation of {@link Ingredient} specialized for {@linkplain ItemStack ItemStacks}.
 *
 *  Created by jard at 1:55 AM on November 19, 2018.
 ***/
public class ItemStackIngredient extends Ingredient<ItemStack> {
    public ItemStackIngredient (ItemStack instance) {
        super (instance, ItemStack.class);
    }

    public ItemStackIngredient (ItemStack instance, IngredientGroup parent) {
        super (instance, ItemStack.class, parent);
    }

    public ItemStackIngredient (NbtCompound nbt, Class<ItemStack> parameterType) {
        super (nbt, parameterType);
    }

    @Override
    public int hashCode () {
        return instance.getItem ().hashCode ();
    }

    @Override
    public ItemStackIngredient getDefaultEmpty () {
        return new ItemStackIngredient (ItemStack.EMPTY);
    }

    @Override
    public Ingredient<ItemStack> dup (int count) {
        ItemStack dupInstance = this.instance.copy ();
        dupInstance.setCount (count);
        return new ItemStackIngredient (dupInstance);
    }

    @Override
    public boolean isEmpty () {
        return instance.isEmpty ();
    }

    @Override
    public int getAmount () {
        return instance.getCount ();
    }

    @Override
    public Ingredient<ItemStack> trim (long vol) {
        if (vol <= 0 || instance == ItemStack.EMPTY || ! isSolubleIngredient ())
            return getDefaultEmpty ();

        long currentVolume = (getAmount () * ((SolubleIngredient) instance.getItem ()).getVolume ());

        vol = vol > currentVolume ? currentVolume : vol;

        if (instance.getItem () instanceof MaterialItem) {
            ItemStack trimmed = MaterialItemConversionHelper.matchVolume (instance, vol);

            instance = MaterialItemConversionHelper.subtractStacks (instance, trimmed);

            return new ItemStackIngredient (trimmed);
        } else {
            int units = (int) (vol / ((SolubleIngredient) instance.getItem ()).getVolume ());

            ItemStack result = instance.copy ();
            result.setCount (units);
            instance.setCount (instance.getCount() - units);

            return new ItemStackIngredient (result);
        }
    }

    @Override
    public boolean instanceMatches (Ingredient other) {
        if (! (other instanceof ItemStackIngredient))
            return false;

        return ItemStack.areItemsEqualIgnoreDamage (instance, ((ItemStackIngredient) other).unwrap ());
    }

    @Override
    protected boolean instanceEquals (Ingredient other) {
        if (! (other instanceof ItemStackIngredient))
            return false;

        return ItemStack.areEqual (instance, ((ItemStackIngredient) other).unwrap ());
    }

    @Override
    protected void mergeExistingStack (Ingredient<ItemStack> in) {
        if (instanceMatches (in)) {
            instance.setCount (instance.getCount () + in.getAmount ());

            // MaterialItem special case: check if both ItemStacks are MaterialItems, then determine the appropriate unit to convert
            // them to
        } else if (in instanceof ItemStackIngredient){
            ItemStackIngredient inItem = (ItemStackIngredient) in;
            if (instance.getItem () instanceof MaterialItem && inItem.instance.getItem () instanceof MaterialItem &&
                    ((MaterialItem) instance.getItem ()).material == ((MaterialItem) inItem.instance.getItem ()).material) {
                instance = MaterialItemConversionHelper.mergeStacks (instance, inItem.instance);
            }
        }
    }

    @Override
    public Object unwrapSpecies ( ) {
        return instance.getItem ();
    }

    @Override
    protected NbtCompound writeNbt (NbtCompound nbt) {
        nbt.put ("InnerItemStack", instance.writeNbt (new NbtCompound ()));

        return nbt;
    }

    @Override
    protected void readNbt (NbtCompound nbt) {
        if (nbt == null || ! nbt.contains ("InnerItemStack"))
            return;

        this.instance = ItemStack.fromNbt (nbt.getCompound ("InnerItemStack"));
    }
}
