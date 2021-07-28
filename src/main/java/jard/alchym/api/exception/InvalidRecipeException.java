package jard.alchym.api.exception;

import jard.alchym.AlchymReference;
import jard.alchym.api.ingredient.IngredientGroup;
import jard.alchym.api.recipe.TransmutationRecipe;
import jard.alchym.api.recipe.TransmuteSpecialBehavior;

/***
 *  InvalidRecipeException
 *  A special exception thrown when the arguments supplied to the constructor of
 *  {@link jard.alchym.api.recipe.TransmutationRecipe} do not meet the required specification
 *
 *  @see jard.alchym.api.recipe.TransmutationRecipe#TransmutationRecipe(String, IngredientGroup,
 *  AlchymReference.Reagents, TransmutationRecipe.TransmutationMedium, TransmutationRecipe.TransmutationType, long,
 *  IngredientGroup, TransmuteSpecialBehavior)
 *
 *  Created by jard at 12:06 AM on April 28, 2018.
 ***/
public class InvalidRecipeException extends Exception {
    public InvalidRecipeException (String message) {
        super (message);
    }
}
