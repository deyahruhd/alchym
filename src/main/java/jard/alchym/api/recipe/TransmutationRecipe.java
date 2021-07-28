package jard.alchym.api.recipe;

import jard.alchym.AlchymReference;
import jard.alchym.api.exception.InvalidRecipeException;
import jard.alchym.api.ingredient.Ingredient;
import jard.alchym.api.ingredient.IngredientGroup;
import jard.alchym.api.transmutation.ReagentItem;
import jard.alchym.api.transmutation.TransmutationAction;
import jard.alchym.api.transmutation.TransmutationInterface;
import jard.alchym.api.transmutation.impl.DryTransmutationInterface;
import jard.alchym.api.transmutation.impl.WetTransmutationInterface;
import jard.alchym.helper.TransmutationHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldAccess;

/***
 *  TransmutationRecipe
 *  A recipe class which describes the transformation of an input {@link IngredientGroup} into an output
 *  {@link IngredientGroup}.
 *
 *  Created by jared at 11:38 PM on April 27, 2018.
 ***/
public class TransmutationRecipe {
    /**
     * Enumerator denoting the levels or media which tramsmutations can occur in
     */
    public enum TransmutationMedium {
        DRY,
        DRY_OR_WET,
        WET
    }


    public enum TransmutationType {
        CALCINATION,
        SOLVATION,
        DISTILLATION,
        FERMENTATION,
        COAGULATION
    }

    private final String name;

    // Any input objects for the transmutation, like input stacks, reagent type, transmutation level,
    // temperatures, special tiles, etc
    public final TransmutationType type;
    private final AlchymReference.Reagents reagent;
    private final TransmutationMedium medium;
    private final IngredientGroup inputs;
    private final long requiredCharge;

    // Any output objects for the transmutation, like output stacks, special behavior, etc
    private final IngredientGroup outputs;
    final TransmuteSpecialBehavior specialBehavior;


    /**
     * Constructor to initialize a new TransmutationRecipe.
     *
     * @param name The name of the recipe.
     * @param inputs An {@link IngredientGroup} corresponding to this recipe's inputs.
     * @param reagent The reagent type required for this recipe.
     * @param medium The medium in which the recipe takes place.
     * @param type The categorization of this recipe.
     * @param requiredCharge The required amount of reagent to initiate this transmutation.
     * @param outputs An {@link IngredientGroup} corresponding to this recipe's outputs.
     * @param specialBehavior A helper class containing modify the world
     *
     * @throws InvalidRecipeException if the specified parameters do not match the following criteria:
     *             - if inputs is empty or null
     *             - if outputs is null or empty, and specialBehavior is null
     */
    public TransmutationRecipe (String name, IngredientGroup inputs, AlchymReference.Reagents reagent,
                                TransmutationMedium medium, TransmutationType type, long requiredCharge,
                                IngredientGroup outputs, TransmuteSpecialBehavior specialBehavior)
            throws InvalidRecipeException {
        this.name = name;
        this.inputs = inputs;
        this.reagent = reagent;
        this.medium = medium;
        this.type = type;
        this.requiredCharge = requiredCharge;

        this.outputs = outputs;
        this.specialBehavior = specialBehavior;

        if (this.inputs == null || this.inputs.isEmpty ())
            throw new InvalidRecipeException ("Invalid recipe: Inputs object must not be empty.");
        //If outputs is null or empty, then specialBehavior must be non-null (otherwise this transmutation doesn't achieve anything)
        if ((this.outputs == null || this.outputs.isEmpty ()) && this.specialBehavior == null)
            throw new InvalidRecipeException ("Invalid recipe: If outputs is empty, specialBehavior must not be null.");
    }

    /**
     * Determines if the supplied transmutation parameters matches this recipe as follows:
     *
     * @param source The source {@link TransmutationInterface} to peek through for matching {@link Ingredient}s
     * @param reagent The reagent {@link ItemStack}
     * @param medium The medium in which the transmutation is taking place
     * @param world The relevant world interface - not used currently
     *
     * @return true if the recipe matches, and false otherwise
     */
    public boolean matches (TransmutationInterface source, ItemStack reagent, TransmutationMedium medium, WorldAccess world) {
        // Dry or wet matches both dry and wet, so we do not need to check equality.
        // In the case that the recipe's level isn't DRY_OR_WET we must check if the level argument is equal to the recipe's level
        if (this.medium != TransmutationMedium.DRY_OR_WET && this.medium != medium)
            return false;

        switch (this.medium) {
            case DRY:
                if (! (source instanceof DryTransmutationInterface))
                    return false;
                break;
            case WET:
                if (! (source instanceof WetTransmutationInterface))
                    return false;
                break;
            case DRY_OR_WET:
                break;
        }

        // Exit if the source does not support this transmutation's type
        if (! source.supports (type))
            return false;

        // Next, check if the item used to initiate the transmutation is a transmutation reagent in the first place
        // (implements ReagentItem and overrides ReagentItem.isReagent () to return true.)
        if (! (reagent.getItem () instanceof ReagentItem) || ! ((ReagentItem) reagent.getItem ()).isReagent ())
            return false;

        // Check if the reagent is a known valid reagent (such as niter and the Philosopher's Stone.) If it is an unknown
        // type, some glitched or broken item was used.
        AlchymReference.Reagents reagentType = ((ReagentItem) reagent.getItem ()).getReagentType ();

        if (reagentType == AlchymReference.Reagents.UNKNOWN)
            return false;

        long charge = TransmutationHelper.getReagentCharge (reagent);

        // The reagentTypes enum is designed to accomodate the fact that any reagent succeeding the previous in the
        // heirarchy can substitute as a recipe. This is determined by comparing the ordinal of the two enumerator, where
        // lower ordinals are "simpler" ingredients and higher ordinals are "complex" ingredients.
        // For example, niter transmutations can utilize the philosopher's stone as a substitute, and PHILOSOPHER_STONE
        // has a higher ordinal number than NITER.

        // If the reagent used for initiating the transmutation is strictly less than the recipe's required reagent ordinal,
        // the reagent is not sufficient enough to initiate the transmutation.
        if (reagentType.ordinal () < this.reagent.ordinal ())
            return false;

        // Perform an unsigned comparison of the supplied reagent's alchemical charge compared to the recipe's required charge.
        if (Long.compareUnsigned (charge, this.requiredCharge) < 0)
            return false;

        // Delegate further comparison to the input IngredientGroup
        if (! inputs.peek (source))
            return false;

        // Return true if all criteria were met.
        return true;
    }

    /**
     * @return The name of the recipe.
     */
    public String getName () {
        return name;
    }

    /**
     * @return A non-null {@link IngredientGroup} corresponding to the inputs of this recipe
     */
    public IngredientGroup getInputs () {
        return inputs;
    }

    /**
     * @return The required charge of the recipe.
     */
    public long getCharge () { return requiredCharge; }

    /**
     * @return A nullable {@link IngredientGroup} corresponding to the outputs of this recipe
     */
    public IngredientGroup getOutputs () {
        return outputs;
    }

    /**
     * @return An anonymous class implementation of {@link TransmuteSpecialBehavior} for special world modification
     * behavior
     */
    public TransmuteSpecialBehavior getSpecialBehavior () {
        return specialBehavior;
    }

    /**
     * @return the maximum possible recipe scale given the supplied {@link TransmutationInterface}.
     */
    public int getRecipeScale (TransmutationInterface source) {
        int scale = Integer.MAX_VALUE;

        for (Ingredient ingredient : inputs) {
            int correspondingAmount = source.peek (ingredient);

            int newScale = correspondingAmount / ingredient.getAmount ();
            if (newScale < scale)
                scale = newScale;
        }

        return scale;
    }
}
