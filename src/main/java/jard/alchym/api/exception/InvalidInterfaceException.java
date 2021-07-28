package jard.alchym.api.exception;

import jard.alchym.api.recipe.TransmutationRecipe;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/***
 *  InvalidInterfaceException
 *  A special exception thrown when the arguments supplied to the constructor of
 *  {@link jard.alchym.api.transmutation.TransmutationInterface} do not meet the required specification
 *
 *  @see jard.alchym.api.transmutation.TransmutationInterface#TransmutationInterface(Object, BiConsumer, BiConsumer,
 *  BiPredicate, TransmutationRecipe.TransmutationType...)
 *
 *  Created by jard at 02:02 on January, 31, 2021.
 ***/
public class InvalidInterfaceException extends Exception {
    public InvalidInterfaceException (String message) { super (message); }
}
