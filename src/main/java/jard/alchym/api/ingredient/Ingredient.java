package jard.alchym.api.ingredient;

import net.minecraft.nbt.NbtCompound;

/***
 *  Ingredient
 *  Generic, abstract class intended to wrap an {@linkplain net.minecraft.item.ItemStack item}, {@linkplain alexiil.mc.lib.attributes.fluid.volume.FluidVolume fluid},
 *  or other substance for usage in {@link IngredientGroup}s.
 *  This allows for cleaner code that makes use of the existing generic {@link java.util.Collection}s, like {@link java.util.HashSet}.
 *  The solutions system also makes heavy use of generalized ingredients for representing solutions.
 *
 *  @param <T> the type of the {@code instance} variable.
 *
 *  Created by jard at 8:14 PM on November 18, 2018. Yarn'd at 9:48 AM on January 18, 2019.
 ***/
public abstract class Ingredient <T> {
    /**
     * Returns a hash code for this {@code Ingredient}, conventionally defined as the hash code of the {@code Ingredient}'s wrapped {@code instance}.
     *
     * @return the hash code of the {@code instance}.
     */
    public abstract int hashCode ( );

    /**
     * Generates an empty {@code Ingredient} based on the type of this {@code Ingredient}.
     *
     * @return an {@code Ingredient} with an empty {@code instance}.
     */
    public abstract Ingredient<T> getDefaultEmpty ( );

    /**
     * Generates a duplicate {@code Ingredient} with the specified count.
     *
     * @param count the volume of the ingredient
     * @return The duplicated {@code Ingredient}
     */
    public abstract Ingredient<T> dup (int count);

    /**
     * Indicates whether an {@code Ingredient} represents an empty ingredient.
     *
     * @return true if the wrapped instance is empty or null.
     */
    public abstract boolean isEmpty ( );

    /**
     * Returns the wrapped {@code instance}'s amount.
     *
     * @return the integer count of the {@code instance}.
     */
    public abstract int getAmount ( );

    /**
     * Divides this {@code Ingredient} into a 'trimmed' part with the user-specified volume and the remaining part.
     * Returns the trimmed {@code Ingredient} and assigns the remaining portion to this {@code Ingredient}.
     *
     * @param vol the volume of {@code Ingredient} that should be split from this {@code Ingredient}.
     * @return an {@code Ingredient} with the specified volume, or this {@code Ingredient} if vol is greater than this Ingredient's volume.
     */
    public abstract Ingredient<T> trim (long vol);

    /**
     * Indicates if the specified {@code Ingredient}'s instance matches this {@code Ingredient}'s instance by species ({@link net.minecraft.fluid.Fluid}-
     * or {@link net.minecraft.item.Item}-wise), but not necessarily by count.
     *
     * @param other the other {@code Ingredient} to compare this {@code Ingredient} to.
     * @return true if {@code other.instance} matches this {@code Ingredient}'s instance
     */
    protected abstract boolean instanceMatches (Ingredient other);

    /**
     * Indicates if the specified {@code Ingredient}'s instance equals this {@code Ingredient}'s instance exactly, both by species and by count.
     *
     * @param other the other {@code Ingredient} to compare this {@code Ingredient} to.
     * @return true if {@code other.instance} equals this {@code Ingredient}'s instance
     */
    protected abstract boolean instanceEquals (Ingredient other);

    /**
     * If possible, merges the {@code instance} of the specified {@code Ingredient} into this {@code Ingredient}.
     *
     * @param in the {@code Ingredient} being merged into this {@code Ingredient}.
     */
    protected abstract void mergeExistingStack (Ingredient<T> in);

    /**
     * Serializes this {@code Ingredient} into the supplied {@linkplain NbtCompound tag}.
     *
     * @param nbt the {@link NbtCompound} to use when serializing
     * @return the resulting {@link NbtCompound}
     */
    protected abstract NbtCompound writeNbt (NbtCompound nbt);

    /**
     * Deserializes the supplied NbtCompound into this {@code Ingredient}, overriding any pre-existing {@code instance}.
     *
     * @param nbt the {@link NbtCompound} to use for deserialization
     */
    protected abstract void readNbt (NbtCompound nbt);

    /**
     * Returns the inner {@link net.minecraft.fluid.Fluid}/{@link net.minecraft.item.Item} of the {@code instance}.
     *
     * @return {@code instance}'s {@link net.minecraft.fluid.Fluid} or {@link net.minecraft.item.Item}, casted to an {@link Object}.
     */
    public abstract Object unwrapSpecies ( );

    protected T instance;
    protected IngredientGroup parent;

    Class<T> type;

    /**
     * Whether this Ingredient is present in the inputs or outputs of a TransmutationRecipe. Used
     * for comparison via the overriden equals method.
     *
     * @see Ingredient#equals (Object)
     */
    private boolean isRecipeInstance = false;

    /**
     * Constructs an {@code Ingredient}.
     *
     * @param instance the {@code T} instance to be wrapped
     * @param parameterType the class type of {@code T}
     */
    protected Ingredient (T instance, Class<T> parameterType) {
        this.instance = instance;
        type = parameterType;

        this.parent = null;
    }

    protected Ingredient (T instance, Class<T> parameterType, IngredientGroup parent) {
        this.instance = instance;
        type = parameterType;

        this.parent = parent;
        isRecipeInstance = parent.isRecipeGroup;
    }

    /**
     * Deserializes a {@link NbtCompound} to yield a new {@code Ingredient} instance.
     *
     * @param nbt a serialized {@code Ingredient}
     * @param parameterType the class type of {@code T}
     */
    protected Ingredient (NbtCompound nbt, Class<T> parameterType) {
        type = parameterType;
        readNbt (nbt);
    }

    /**
     * Determines whether this {@code Ingredient}'s wrapped instance implements the {@link SolubleIngredient} interface
     *
     * @return true if the instance's 'species' is a {@link SolubleIngredient} and has non-null material
     */
    protected final boolean isSolubleIngredient ( ) {
        return !isEmpty () && unwrapSpecies () instanceof SolubleIngredient && ((SolubleIngredient) unwrapSpecies ()).getMaterial () != null;
    }

    /**
     * @return This {@code Ingredient}'s wrapped instance
     */
    public final T unwrap ( ) {
        return instance;
    }

    @Override
    public final boolean equals (Object rhs) {
        // Preliminary check to make sure we are checking two wrappers of the same type: FluidInstances are not ItemStacks
        if (! (rhs instanceof Ingredient) || ! type.isInstance (((Ingredient) rhs).instance))
            return false;

        boolean flag1 = instanceMatches ((Ingredient) rhs);

        // If both this Ingredient and rhs are present in a ingredient, we are comparing two
        // recipes together. In this case, neither ItemStack describes an actual entity in the world, and so
        // item counts can be ignored.
        // In the other possible case where this and rhs are not part of a ingredient, we simply return true.
        // We are comparing two Ingredients that exist in the world, and so we do not need to check
        // for subsets.
        if (flag1 && isRecipeInstance == ((Ingredient) rhs).isRecipeInstance)
            return true;

        // Otherwise, compare amounts.
        int thisCount = getAmount ();
        int rhsCount = ((Ingredient) rhs).getAmount ();

        return flag1 &&
                // If this item is part of a transmutation ingredient, we ALWAYS check if rhs has as many items as this, as
                // we are trying to determine if a supplied ItemStack meets or exceeds the required count of items of the ingredient.
                // Otherwise, perform normal comparison (rhs can be "contained" in this ItemStackIngredient)
                (isRecipeInstance ? rhsCount >= thisCount : rhsCount <= thisCount);
    }
}

