package jard.alchym.mixin;

import jard.alchym.AlchymReference;
import jard.alchym.api.ingredient.SolubleIngredient;
import jard.alchym.blocks.blockentities.ChymicalContainerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/***
 *  ItemSolubleMixin
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 01:04 on April, 05, 2021.
 ***/
@Mixin (BlockItem.class)
public abstract class BlockItemSolubleMixin extends Item implements SolubleIngredient {
    public BlockItemSolubleMixin (Settings settings) {
        super (settings);
    }

    @Override
    public boolean canInsert (ChymicalContainerBlockEntity container) {
        return getMaterial () != null;
    }

    @Override
    public AlchymReference.IMaterial getMaterial () {
        return AlchymReference.AdditionalMaterials.getExistingSpeciesMaterial (getBlock ());
    }

    @Override
    public long getSolubility (Fluid fluid) {
        return AlchymReference.FluidSolubilities.getSolubility (fluid, this);
    }

    // Returns a unit volume with respect to the number of millibuckets in a bucket of fluid (1000).
    // The total volume of an SolubleIngredient is that SolubleIngredient's amount multiplied by the result of this function.
    // Block solutes will always have a volume of 1000 millibuckets per unit.

    @Override
    public long getVolume () {
        return 1000;
    }

    @Shadow
    public Block getBlock () {
        return null;
    }
}
