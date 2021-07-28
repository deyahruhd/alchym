package jard.alchym.blocks;

import jard.alchym.AlchymReference;
import jard.alchym.fluids.MaterialFluid;
import jard.alchym.init.InitBlocks;
import net.minecraft.block.FluidBlock;

/***
 *  MaterialFluidBlock
 *  Subclass of {@link FluidBlock} intended for use by {@link MaterialFluid} instances.
 *  This primarily exists to enforce the typing of certain registered {@link FluidBlock}s
 *  in {@link InitBlocks}.
 *
 *  Created by jard at 23:03 on February, 08, 2021.
 ***/
public class MaterialFluidBlock extends FluidBlock {
    public final AlchymReference.Materials material;

    protected MaterialFluidBlock (MaterialFluid flowableFluid, Settings settings) {
        super (flowableFluid, settings);

        material = flowableFluid.material;
    }
}
