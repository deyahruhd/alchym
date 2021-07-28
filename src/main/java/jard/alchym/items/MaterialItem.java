package jard.alchym.items;

import jard.alchym.AlchymReference;
import jard.alchym.api.ingredient.SolubleIngredient;
import jard.alchym.blocks.blockentities.ChymicalContainerBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

/***
 *  MaterialItem
 *  A generic material item which is instantiated with a certain material and form.
 *
 *  Created by jard at 1:36 PM on December 21, 2018.
 ***/
public class MaterialItem extends TransmutableReagentItem implements SolubleIngredient {
    public final AlchymReference.Materials material;
    public final AlchymReference.Materials.Forms form;

    public MaterialItem (Settings settings, AlchymReference.Materials material, AlchymReference.Materials.Forms form) {
        super (settings);

        this.material = material;
        this.form = form;
    }

    @Environment (EnvType.CLIENT)
    @Override
    public boolean hasGlint (ItemStack itemStack) {
        return material == AlchymReference.Materials.ALCHYMIC_GOLD;
    }

    @Override
    public boolean canInsert (ChymicalContainerBlockEntity container) {
        return  form == AlchymReference.Materials.Forms.POWDER ||
                form == AlchymReference.Materials.Forms.REAGENT_POWDER ||
                form == AlchymReference.Materials.Forms.NUGGET ||

                (form == AlchymReference.Materials.Forms.INGOT &&
                        container.getCapacity () >= AlchymReference.ChymicalContainers.COPPER_CRUCIBLE.capacity);
    }

    @Override
    public AlchymReference.Materials getMaterial ( ) {
        return material;
    }

    @Override
    public long getSolubility (Fluid fluid) {
        return AlchymReference.FluidSolubilities.getSolubility (fluid, this);
    }

    @Override
    public long getVolume ( ) {
        return form.volume;
    }

    @Override
    public boolean isReagent() {
        return form == AlchymReference.Materials.Forms.REAGENT_POWDER;
    }

    @Override
    public long getUnitCharge() {
        return isReagent () ? 1L : 0L;
    }

    @Override
    public AlchymReference.Reagents getReagentType() {
        switch (material) {
            case NITER:
                return AlchymReference.Reagents.NITER;
            case PROJECTION_POWDER:
                return AlchymReference.Reagents.PHILOSOPHERS_STONE;
            default:
                return AlchymReference.Reagents.UNKNOWN;
        }
    }
}
