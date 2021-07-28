package jard.alchym.init;

import jard.alchym.api.ingredient.Ingredient;
import jard.alchym.api.ingredient.IngredientGroup;
import jard.alchym.api.recipe.TransmutationRecipe;

/***
 *  RecipeGroupAccessor
 *  Friend accessor class allowing intercommunication between the inaccessible constructor
 *  {@code IngredientGroup#IngredientGroup(boolean, Ingredient[])} and the {@code jard.alchym.init} package.
 *
 *  Created by jard at 2:24 PM on September 1, 2019.
 ***/
public abstract class RecipeGroupAccessor {
    private static RecipeGroupAccessor instance = null;

    /**
     * @return an instance of {@link IngredientGroup}'s {@code RecipeGroupAccessorImpl}
     */
    static RecipeGroupAccessor getInstance () {
        RecipeGroupAccessor access = instance;
        if (access != null)
            return access;

        return createInstance ();
    }

    /**
     * Instantiates a new {@code RecipeGroupAccessorImpl} by force loading {@link IngredientGroup}.class.
     *
     * @return an instance of {@link IngredientGroup}'s {@code RecipeGroupAccessorImpl}
     */
    private static RecipeGroupAccessor createInstance () {
        try {
            Class.forName (IngredientGroup.class.getName (), true,
                    IngredientGroup.class.getClassLoader ());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException (e);
        }

        return instance;
    }

    /**
     * Sets the current friend accessor instance.
     *
     * Note that this can only be used by {@link IngredientGroup}; any attempts to reinvoke this method with a new accessor
     * will result in an {@link IllegalStateException}.
     *
     * @param accessor The accessor supplied by {@link IngredientGroup}.
     */
    public static void setInstance (RecipeGroupAccessor accessor) {
        if (instance != null)
            throw new IllegalStateException ("RecipeGroupAccessor instance was already set!");

        instance = accessor;
    }

    protected abstract IngredientGroup createRecipeGroup (TransmutationRecipe.TransmutationMedium medium, Ingredient... stacks);
}
