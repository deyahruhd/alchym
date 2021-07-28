package jard.alchym.api.transmutation;

import jard.alchym.api.exception.InvalidActionException;
import jard.alchym.api.ingredient.Ingredient;
import jard.alchym.api.recipe.TransmutationRecipe;
import jard.alchym.api.recipe.TransmuteSpecialBehavior;
import jard.alchym.api.transmutation.impl.DryTransmutationInterface;
import jard.alchym.api.transmutation.impl.WetTransmutationInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

/***
 *  TransmutationAction
 *  Implements the generalized transmutation mechanic as outlined in https://github.com/deyahruhd/alchym/issues/4.
 *
 *  A {@code TransmutationAction} takes three objects:
 *  - the {@link TransmutationRecipe} which it is supposed to enact,
 *  - a source {@link TransmutationInterface} which supplies the inputs of the {@link TransmutationRecipe}, and
 *  - an output {@link TransmutationInterface} which the action inserts outputs into.
 *
 *  Created by jard at 4:47 PM on April 18, 2019.
 ***/
public class TransmutationAction {
    /**
     * Constructs a new {@code TransmutationAction} with the supplied {@code source}, {@code target}, and {@code recipe},
     * and prepares it for performing the desired transmutation.
     *
     * @param source The source interface
     * @param target The sink interface
     * @param recipe A desired {@link TransmutationRecipe}
     */
    public TransmutationAction (TransmutationInterface source, TransmutationInterface target,
                                TransmutationRecipe recipe, WorldAccess world) {
        this.source = source;
        this.target = target;
        this.recipe = recipe;
        this.world = world;

        source.closePushChannel ();
        target.closePullChannel ();

        valid = true;
    }

    TransmutationInterface source;
    TransmutationInterface target;
    TransmutationRecipe recipe;
    WorldAccess world;

    boolean valid;

    /**
     * Attempts to apply the transmutation.
     *
     * @return true if successful
     */
    public boolean apply (ItemStack reagent, BlockPos pos) throws InvalidActionException {
        if (! valid)
            return false;

        TransmutationRecipe.TransmutationMedium medium;

        if (source instanceof DryTransmutationInterface)
            medium = TransmutationRecipe.TransmutationMedium.DRY;
        else if (source instanceof WetTransmutationInterface)
            medium = TransmutationRecipe.TransmutationMedium.WET;
        else {
            valid = false;
            throw new InvalidActionException("Attempted to utilize an unknown 'TransmutationInterface' for a transmutation!");
        }

        if (! recipe.matches (source, reagent, medium, world)) {
            valid = false;
            return false;
        }

        int recipeScale = recipe.getRecipeScale (source);

        // Recipe matches; initiate transmutation
        // Handle the world modification first before consuming ingredients
        TransmuteSpecialBehavior editWorld = recipe.getSpecialBehavior ();

        if (editWorld != null && !editWorld.modifyWorld (world, pos, recipeScale))
            return false;

        // Pull input ingredients
        for (Ingredient ingredient : recipe.getInputs ()) {
            source.extract (ingredient.dup (ingredient.getAmount () * recipeScale));
        }

        // Push output ingredients
        for (Ingredient ingredient : recipe.getOutputs()) {
            target.insert (ingredient.dup (ingredient.getAmount () * recipeScale));
        }

        valid = false;
        return true;
    }
}
