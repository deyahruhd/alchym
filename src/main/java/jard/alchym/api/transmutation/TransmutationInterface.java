package jard.alchym.api.transmutation;

import jard.alchym.api.exception.InvalidInterfaceException;
import jard.alchym.api.ingredient.Ingredient;
import jard.alchym.api.recipe.TransmutationRecipe;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/***
 *  TransmutationInterface
 *  An interface that specifies how a TransmutationAction should interact with the world.
 *
 *  Essentially, this interface comprises of four objects:
 *  - a {@code K} instance which represents anything that can be a source and sink (the endpoint),
 *  - a {@link BiConsumer} which accepts an {@link Ingredient} and K instance (usually, the endpoint)
 *    and pushes it to the endpoint (the pushing 'channel'),
 *  - a {@link BiConsumer} which accepts an arbitrary {@link Ingredient} instance and K instance
 *    (usually, the endpoint) attempts to pull that object from the endpoint (the pulling 'channel'), and
 *  - a {@link BiFunction} which 'peeks' into the endpoint to see if a matching {@link Ingredient} exists within the
 *    endpoint. This shall return the unit count of the {@link Ingredient} if found, or 0 otherwise.
 *
 *  Created by jard at 5:24 PM on April 18, 2019.
 ***/
public abstract class TransmutationInterface <T extends Ingredient, K> {
    private final BiConsumer<T, K> CLOSED_CHANNEL = ($, $end) -> {};

    protected final K endpoint;

    BiConsumer <T, K> push;
    BiConsumer <T, K> pull;

    BiFunction <T, K, Integer> peek;

    protected final Set <TransmutationRecipe.TransmutationType> supportedOps;

    /**
     * Constructs a new TransmutationInterface with an endpoint, push channel, pull channel, and peek channel.
     *
     * @param endpoint Generic-type instance to peek, push, or pull from
     * @param pusher A {@linkplain BiConsumer} which inserts a {@code T} parameter into the endpoint
     * @param puller A {@linkplain BiConsumer} which attempts to pull a {@code T} instance from the endpoint
     * @param peeker A {@linkplain BiPredicate} which probes the endpoint for the existence of a matching {@code T} instance
     * @param ops The set of transmutation operations this transmutation interface supports.
     */
    public TransmutationInterface (K endpoint, BiConsumer<T, K> pusher, BiConsumer<T, K> puller, BiFunction <T, K, Integer> peeker,
                                   TransmutationRecipe.TransmutationType ... ops) throws InvalidInterfaceException {
        if (ops.length == 0)
            throw new InvalidInterfaceException ("TransmutationInterface must take at least one transmutation operation.");

        this.endpoint = endpoint;
        push = pusher;
        pull = puller;
        peek = peeker;
        supportedOps = new HashSet<> (Arrays.asList (ops));
    }

    /**
     * Closes the push channel, effectively preventing this interface from being able to insert objects
     *
     * @return The {@code TransmutationInterface} being closed after closure
     */
    final TransmutationInterface closePushChannel () {
        push = CLOSED_CHANNEL;
        return this;
    }

    /**
     * Closes the pull channel, effectively preventing this interface from being able to remove objects
     *
     * @return The {@code TransmutationInterface} being closed after closure
     */
    final TransmutationInterface closePullChannel () {
        pull = CLOSED_CHANNEL;
        peek = ($, $end) -> 0;
        return this;
    }

    /**
     * Closes the push and pull channels, turning this {@code TransmutationInterface} into just a peek interface.
     *
     * @return The {@code TransmutationInterface} being closed after closure
     */
    public final TransmutationInterface close () {
        return closePushChannel ().closePullChannel ();
    }

    /**
     * Inserts the supplied {@link Ingredient}s into {@code endpoint}.
     *
     * @param instances A variable-arity array of {@link Ingredient}s
     */
    @SafeVarargs
    public final void insert (T ... instances) {
        for (T instance : instances)
            push.accept (instance, endpoint);
    }

    /**
     * Peeks into the endpoint.
     *
     * @param instance The instance to peek
     * @return The amount of the instance, if found, or 0 otherwise
     */
    public final int peek (T instance) {
        return peek.apply (instance, endpoint);
    }

    /**
     * Removes the supplied {@link Ingredient}s from the endpoint.
     *
     * @param instances A variable-arity array of {@link Ingredient}s
     */
    @SafeVarargs
    public final void extract (T ... instances) {
        for (T instance : instances) {
            pull.accept (instance, endpoint);
        }
    }

    public boolean supports (TransmutationRecipe.TransmutationType type) {
        return supportedOps.contains (type);
    }
}
