package jard.alchym.mixin;

import jard.alchym.AlchymReference;
import jard.alchym.blocks.blockentities.ChymicalContainerBlockEntity;
import jard.alchym.api.ingredient.SolubleIngredient;
import net.minecraft.fluid.Fluid;
import org.spongepowered.asm.mixin.Mixin;

/***
 *  FluidSolubleMixin
 *  Mixin that allows any arbitrary Fluid to implement the SolubleIngredient interface.
 *
 *  Created by jard at 1:31 AM on February 03, 2019.
 ***/
@Mixin (Fluid.class)
public abstract class FluidSolubleMixin implements SolubleIngredient {
    @Override
    public boolean canInsert (ChymicalContainerBlockEntity container) {
        return getMaterial () != null;
    }

    @Override
    public AlchymReference.IMaterial getMaterial () {
        return AlchymReference.AdditionalMaterials.getExistingSpeciesMaterial (this);
    }

    @Override
    public long getSolubility (Fluid fluid) {
        return AlchymReference.FluidSolubilities.getSolubility (fluid, this);
    }

    // Returns a unit volume with respect to the number of millibuckets in a bucket of fluid (1000).
    // The total volume of an SolubleIngredient is that SolubleIngredient's amount multiplied by the result of this function.
    // For liquid solutes, this number is always 1 (indicating 1 bucket of liquid has the same volume as 1 bucket of water),
    // since getAmount will return the amount in millibuckets anyways.

    @Override
    public long getVolume () {
        return 1;
    }
}
