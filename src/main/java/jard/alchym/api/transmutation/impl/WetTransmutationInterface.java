package jard.alchym.api.transmutation.impl;

import jard.alchym.api.exception.InvalidInterfaceException;
import jard.alchym.api.ingredient.Ingredient;
import jard.alchym.api.recipe.TransmutationRecipe;
import jard.alchym.api.transmutation.TransmutationInterface;
import jard.alchym.blocks.blockentities.ChymicalContainerBlockEntity;
import jard.alchym.helper.MathHelper;

/***
 *  WetTransmutationInterface
 *  Implementation of the TransmutationInterface class specifically for transmutations which manipulate SolutionGroups within
 *  GlassContainerBlockEntities.
 *
 *  Created by jard at 6:31 PM on April 18, 2019.
 ***/
public class WetTransmutationInterface extends TransmutationInterface <Ingredient, ChymicalContainerBlockEntity> {
    public WetTransmutationInterface (ChymicalContainerBlockEntity endpoint) throws InvalidInterfaceException {
        super (endpoint,
                // Push channel
                (ingr, container) -> container.insertIngredient (ingr),

                // Pull channel
                (ingr, container) -> container.pullIngredient (ingr),

                // Peek channel
                (ingr, container) -> container.countIngredient (ingr),

                endpoint.getOps ()
        );
    }

    @Override
    public boolean supports (TransmutationRecipe.TransmutationType type) {
        return super.supports (type) &&
                MathHelper.implies (type == TransmutationRecipe.TransmutationType.CALCINATION,
                endpoint.hasOnlyInsoluble ());
    }
}
