package jard.alchym.api.ingredient;

import jard.alchym.AlchymReference;
import jard.alchym.blocks.blockentities.ChymicalContainerBlockEntity;
import net.minecraft.fluid.Fluid;

/***
 *  SolubleIngredient
 *  Interface class for any solute that can be dissolved within a {@link ChymicalContainerBlockEntity}.
 *
 *  Describes the solubility of a solute with respect to some {@link Fluid} by returning the amount of solute that can
 *  dissolve within that {@linkplain Fluid fluid}. 0 indicates insolubility while -1 indicates complete miscibility.
 *
 *  Created by jard at 12:43 PM on January 17, 2019.
 ***/
public interface SolubleIngredient {
    /**
     * Indicates whether this {@code SolubleIngredient} can be inserted into the specified GlassContainerBlockEntity
     *
     * @param container the container in question
     * @return true if this {@code SolubleIngredient} should be inserted
     */
    boolean canInsert (ChymicalContainerBlockEntity container);

    /**
     * Returns this SolubleIngredient's material, defined in AlchymReference.
     *
     * @return the corresponding material
     */
    AlchymReference.IMaterial getMaterial ();

    /**
     * Returns a long representing the volume of this {@code SolubleIngredient} that can dissolve in 1 millibuckets of the supplied {@link Fluid}.
     *
     * @param fluid the solvent
     * @return 0 if the {@code SolubleIngredient} is insoluble, or the appropriate solubility from {@link AlchymReference}.
     * @see AlchymReference.FluidSolubilities
     */
    long getSolubility (Fluid fluid);

    /**
     * Returns a unit volume with respect to the number of millibuckets in a bucket of fluid (1000).
     * For liquid solutes, this number is always 1 (indicating 1 bucket of liquid has the same volume as 1 bucket of water),
     * since getAmount will return the amount in millibuckets anyways.
     *
     * @return 1 if this {@code SolubleIngredient} is a {@link Fluid}, or the corresponding volume.
     */
    long getVolume ();
}
