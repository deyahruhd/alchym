package jard.alchym.api.ingredient;

import com.google.common.collect.Lists;
import jard.alchym.api.ingredient.impl.FluidVolumeIngredient;
import jard.alchym.api.ingredient.impl.ItemStackIngredient;
import jard.alchym.api.recipe.TransmutationRecipe;
import jard.alchym.init.RecipeGroupAccessor;
import jard.alchym.api.transmutation.TransmutationInterface;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

import java.util.*;

/***
 *  IngredientGroup
 *
 *  A generic class for storing a group of {@link FluidVolume}s or {@link ItemStack}s, with functionality to check equality between
 *  {@code IngredientGroup}s in the "subset" sense, that is, two {@code IngredientGroup}s A and B are equal if A ⊆ B.
 *
 *  {@code IngredientGroup} does not have a public constructor. Instead, {@code IngredientGroups} are instantiated through use of the
 *  static methods {@code fromIngredients}, {@code fromItemStacks}, {@code fromFluidVolumes}, and {@code fromItemEntities}.
 *
 *  @see IngredientGroup#fromIngredients(Ingredient...)
 *  @see IngredientGroup#fromItemStacks(ItemStack...)
 *  @see IngredientGroup#fromFluidVolumes(FluidVolume...)
 *  @see IngredientGroup#fromItemEntities(ItemEntity...)
 *
 *  Created by jared at 12:01 AM on May 06, 2018. Yarn'd at 9:48 AM on January 18, 2019.
 ***/
public class IngredientGroup implements Iterable <Ingredient>{
    final boolean isRecipeGroup;
    final Comparator <Ingredient> ingredientOrdering =
            // Comparator sorts Ingredients by the following rules:
            //    - FluidVolumes are always greater than ItemStacks
            //    - Higher amounts are always greater than lower amounts
            (o1, o2) -> {
                if (o1.equals (o2))
                    return 0;
                else {
                    if (o1.type == o2.type) {
                        int initialCompare = Integer.compare (o1.getAmount (), o2.getAmount ());

                        return initialCompare != 0 ? initialCompare :
                                // Need some way to differentiate between two Ingredients with differing instances but
                                // the same stack count - in this case, it does not matter what order they fall under.
                                // Just use their hash codes as the deciding factor
                                Integer.compare (o1.hashCode (), o2.hashCode ());
                    } else if (o1.type == FluidVolume.class)
                        return 1;
                    else
                        return - 1;
                }
            };
    final Set <Ingredient> contents = new TreeSet<> (ingredientOrdering);

    public IngredientGroup () {
        this.isRecipeGroup = false;
    }

    /**
     * Constructs an {@code IngredientGroup}.
     *
     * @param isRecipeGroup if this is a group for use in recipe comparisons
     * @param stacks the list of {@link Ingredient}s this group contains
     */
    IngredientGroup (boolean isRecipeGroup, Ingredient... stacks) {
        this.isRecipeGroup = isRecipeGroup;
        this.contents.addAll (Lists.newArrayList (stacks));

        for (Ingredient ingredient : contents) {
            ingredient.parent = this;
        }
    }

    /**
     * Generates an IngredientGroup from a list of {@linkplain Ingredient ingredients}.
     *
     * @param ingredients a varargs argument, representing the list of {@linkplain Ingredient ingredients} the new {@code IngredientGroup} should
     *                    have.
     * @return an {@code IngredientGroup} with the supplied ingredients.
     */
    public static IngredientGroup fromIngredients (Ingredient... ingredients) {
        return new IngredientGroup (false, ingredients);
    }

    /**
     * Generates an IngredientGroup from a list of {@linkplain ItemStack ItemStacks}.
     *
     * @param stacks a varargs argument, representing the list of {@linkplain ItemStack ItemStacks} the new {@code IngredientGroup} should
     *               have.
     * @return an {@code IngredientGroup} with the supplied stacks.
     */
    public static IngredientGroup fromItemStacks (ItemStack... stacks) {
        return fromItemStacks (false, stacks);
    }

    /**
     * Generates an IngredientGroup from a list of {@linkplain FluidVolume FluidVolumes}.
     *
     * @param fluids a varargs argument, representing the list of {@linkplain FluidVolume FluidVolumes} the new {@code IngredientGroup} should
     *      *        have.
     * @return
     */
    public static IngredientGroup fromFluidVolumes (FluidVolume ... fluids) {
        ArrayList <Ingredient> list = new ArrayList <> ();
        for (FluidVolume fluid : fluids) {
            list.add (new FluidVolumeIngredient (fluid));
        }

        return fromFluidVolumes (false, fluids);
    }

    /**
     * Generates an IngredientGroup from a list of {@linkplain ItemEntity ItemEntities}, unwrapping them first to retrieve their inner
     * {@link ItemStack}.
     *
     * @param entities a varargs argument, representing the list of {@linkplain ItemEntity ItemEntities}s the new {@code IngredientGroup} should
     *                 have.
     * @return an {@code IngredientGroup} with the supplied items.
     */
    public static IngredientGroup fromItemEntities (ItemEntity ... entities) {
        ArrayList <ItemStack> list = new ArrayList <> ();
        for (ItemEntity entity : entities) {
            list.add (entity.getStack ());
        }

        return fromItemStacks (list.toArray (new ItemStack [0]));
    }

    static IngredientGroup fromItemStacks (boolean isRecipeGroup, ItemStack... stacks) {
        ArrayList <Ingredient> list = new ArrayList <> ();
        for (ItemStack item : stacks) {
            list.add (new ItemStackIngredient (item));
        }

        return new IngredientGroup (isRecipeGroup, list.toArray (new Ingredient[]{}));
    }

    static IngredientGroup fromFluidVolumes (boolean isRecipeGroup, FluidVolume ... fluids) {
        ArrayList <Ingredient> list = new ArrayList <> ();
        for (FluidVolume fluid : fluids) {
            list.add (new FluidVolumeIngredient (fluid));
        }

        return new IngredientGroup (isRecipeGroup, list.toArray (new Ingredient[]{}));
    }


    /**
     * Indicates if this {@code IngredientGroup}'s {@code contents} is empty, or if {@code contents} contains only empty ingredients.
     *
     * @return true if this {@code IngredientGroup} is empty
     */
    public boolean isEmpty () {
        if (contents.size () > 0) {
            for (Ingredient e : contents) {
                if (!e.isEmpty ())
                    return false;
            }
        }

        return true;
    }

    /**
     * Returns the number of {@linkplain Ingredient ingredients} in {@code contents}.
     *
     * @return an int representing the size of {@code contents}
     */
    public int getCount () {
        return contents.size ();
    }

    /**
     * Determines if this {@code IngredientGroup} is a subset of the supplied {@code IngredientGroup}.
     *
     * @param rhs the superset {@code IngredientGroup} to compare to
     * @return true if {@code this} ⊆ {@code rhs}
     */
    public boolean subset (IngredientGroup rhs) {
        if (isEmpty () || rhs == null || rhs.isEmpty ()) {

            return false;
        }

        // this ⊆ rhs ⇔ (rhs ∪ this) == rhs
        Set <Ingredient> thisSet = new HashSet <> (contents);
        Set <Ingredient> rhsSet = new HashSet <> (rhs.contents);
        Set <Ingredient> union = new HashSet <> (thisSet);
        union.addAll (rhsSet);

        return union.equals (rhsSet);
    }

    /**
     * Determines if this {@code IngredientGroup} is a subset of a group of {@linkplain Ingredient}s accessed through
     * the supplied {@linkplain TransmutationInterface}.
     *
     * Note that this method is explicitly used by dry transmutation only.
     * Wet transmutations utilize {@link SolutionGroup#peek(TransmutationInterface)}.
     *
     * TODO: Maybe refactor this out into a custom ItemEntityGroup? I can't think of any other uses for it though
     *
     * @param source the source to access
     * @return true if, for every {@linkplain Ingredient} I in {@code this}, there exists some {@linkplain Ingredient} J
     *         supplied by {@code source} such that I ⊆ J.
     */
    public boolean peek (TransmutationInterface source) {
        for (Ingredient ingredient : contents) {
            assert (ingredient instanceof ItemStackIngredient);

            // Check if the peeked ingredient amount is greater than or equal to this group's instance of
            // that ingredient.
            // If the source does not have enough then this IngredientGroup isn't a subset
            if (source.peek (ingredient) < ingredient.getAmount ())
                return false;
        }

        return true;
    }

    /**
     * Determines if the supplied {@link Ingredient} exists in this {@code IngredientGroup}'s contents.
     *
     * @param t the {@linkplain Ingredient ingredient} to find
     *
     * @return true if item ∈ this IngredientGroup.
     */
    public boolean isInGroup (Ingredient t) {
        return contents.contains (t);
    }

    /**
     * Searches for an {@link Ingredient} in {@code contents} which matches the supplied ingredient.
     *
     * @param t the {@linkplain Ingredient ingredient} to match with
     * @return the {@link Ingredient} that matches {@code t}, or an empty ingredient if not found.
     */
    public Ingredient getMatchingIngredient (Ingredient t) {
        for (Ingredient i : contents) {
            if (i.equals (t) || i.instanceMatches (t))
                return i;
        }

        return t.getDefaultEmpty ();
    }

    /**
     * Unwraps all {@linkplain Ingredient ingredients} contained in the items set into their respective objects, and
     * returns them as an array of {@linkplain Object objects}.
     *
     * @return an array of {@linkplain Object Objects} representing the unwrapped instances
     * @see Ingredient#unwrap()
     */
    public Object[] asArray () {
        Set <Object> ret = new HashSet <> ();

        for (Ingredient wrapper : contents) {
            ret.add (wrapper.instance);
        }

        return ret.toArray (new Object[0]);
    }

    /**
     * Adds an {@link Ingredient} to {@code contents}, or merges it with an existing {@linkplain Ingredient ingredient}
     * that matches it.
     *
     * @param ingredient The {@link Ingredient} to insert.
     * @return ingredient if it was added to {@code contents}, or the {@link Ingredient} it was merged into.
     */
    public Ingredient addIngredient (Ingredient ingredient) {
        if (ingredient.isEmpty ())
            return ingredient;

        for (Ingredient ref : contents) {
            if (ref.instanceMatches (ingredient)) {
                // Merge stack into ref and return it
                ref.mergeExistingStack (ingredient);
                return ref;
            }
        }

        ingredient.parent = this;
        contents.add (ingredient);
        return ingredient;
    }


    /**
     * Rmemoves a matching {@link Ingredient} from {@code contents}.
     *
     * @param ingredient The {@link Ingredient} to match and remove.
     */
    public void removeIngredient (Ingredient ingredient) {
        Ingredient match = getMatchingIngredient (ingredient);
        contents.remove (match);
    }

    /**
     * Generates an {@link Iterator} over {@code contents} for use in for-each iteration.
     *
     * @return {@code contents}' corresponding {@link Iterator}
     */
    @Override
    public Iterator<Ingredient> iterator() {
        return contents.iterator ();
    }

    static {
        RecipeGroupAccessor.setInstance (new RecipeGroupAccessorImpl ());
    }

    /***
     * RecipeGroupAccessor
     *
     * Special recipe group accessor for the {@link IngredientGroup#IngredientGroup(boolean, Ingredient[])} constructor.
     *
     * This constructor should be left invisible outside of the Alchym API, however due to the lack of friend packages
     * and classes in Java, recipe classes in {@code jard.alchym.api.recipe} are not able to access it.
     *
     * In particular the instantiation of a {@link jard.alchym.api.recipe.TransmutationRecipe} implies that its
     * {@code inputs} and {@code outputs} {@link IngredientGroup}s are 'recipe groups'; this ensure the proper subset
     * comparison occurs during recipe matching.
     *
     * This class exposes this constructor to the {@code jard.alchym.api.recipe} package to solve this problem.
     *
     * @see RecipeGroupAccessor
     * @see IngredientGroup#IngredientGroup(boolean, Ingredient[])
     * @see IngredientGroup#subset(IngredientGroup)
     *
     ***/
    static final class RecipeGroupAccessorImpl extends RecipeGroupAccessor {
        @Override
        protected IngredientGroup createRecipeGroup (TransmutationRecipe.TransmutationMedium medium, Ingredient... stacks) {
            if (medium == TransmutationRecipe.TransmutationMedium.DRY)
                return new IngredientGroup (true, stacks);
            else
                return new SolutionGroup (true, stacks);
        }
    }
}
