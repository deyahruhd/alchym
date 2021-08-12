package jard.alchym.fluids;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/***
 *  MaterialFluid
 *  A generic material fluid which is instantiated with a certain material.
 *
 *  Created by jard at 22:10 on February, 08, 2021.
 ***/
public class MaterialFluid extends FlowableFluid {
    protected MaterialFluid still   = null;
    protected MaterialFluid flowing = null;

    public final AlchymReference.Materials material;

    public MaterialFluid (AlchymReference.Materials material) {
        super ();

        this.material = material;
    }

    @Override
    public Fluid getFlowing () {
        if (flowing == null)
            flowing = new Flowing (this);

        return flowing;
    }

    @Override
    public Fluid getStill () {
        if (still == null)
            still = new Still (this);

        return still;
    }

    @Override
    protected boolean isInfinite () {
        return false;
    }

    @Override
    protected void beforeBreakingBlock (WorldAccess worldAccess, BlockPos blockPos, BlockState blockState) {

    }

    @Override
    protected final int getFlowSpeed (WorldView worldView) {
        return material == AlchymReference.Materials.MERCURY ? 1 : 4;
    }

    @Override
    protected final int getLevelDecreasePerBlock (WorldView worldView) {
        return material == AlchymReference.Materials.MERCURY ? 2 : 1;
    }

    @Override
    public Item getBucketItem () {
        return null;
    }

    @Override
    protected boolean canBeReplacedWith (FluidState fluidState, BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getTickRate (WorldView worldView) {
        return material == AlchymReference.Materials.MERCURY ? 20 : 5;
    }

    @Override
    protected float getBlastResistance () {
        return 100.f;
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getFlowing () || fluid == getStill ();
    }

    @Override
    protected BlockState toBlockState (FluidState fluidState) {
        return Alchym.content ().blocks.getFluidBlock (material).getDefaultState ()
                .with (Properties.LEVEL_15, getBlockStateLevel (fluidState));
    }

    @Override
    public boolean isStill (FluidState fluidState) {
        return false;
    }

    @Override
    public int getLevel (FluidState fluidState) {
        return fluidState.get (LEVEL);
    }

    protected static class Flowing extends MaterialFluid {
        public Flowing (MaterialFluid base) {
            super (base.material);

            this.flowing = this;
        }

        protected void appendProperties (StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties (builder);
            builder.add (LEVEL);
        }

        @Override
        public final int getLevel (FluidState fluidState) {
            return fluidState.get (LEVEL);
        }
    }

    protected static class Still extends MaterialFluid {
        public Still (MaterialFluid base) {
            super (base.material);

            this.still = this;
        }

        @Override
        public boolean isStill (FluidState fluidState) {
            return true;
        }

        @Override
        public final int getLevel (FluidState fluidState) {
            return 8;
        }
    }
}
