package jard.alchym.init;

import jard.alchym.AlchymReference;
import jard.alchym.blocks.MaterialFluidBlock;
import jard.alchym.fluids.MaterialFluid;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.Map;

/***
 *  InitFluids
 *  The initializing module that initializes every fluid in the mod.
 *
 *  Created by jard at 22:06 on February, 08, 2021.
 ***/
public class InitFluids extends InitAbstract <Fluid> {
    private static final Map<Pair<AlchymReference.Materials, AlchymReference.Materials.Forms>, Fluid> materialFluids = new LinkedHashMap<> ();
    static {
        for (AlchymReference.Materials material : AlchymReference.Materials.values ()) {
            if (material.forms == null || ! material.forms.contains (AlchymReference.Materials.Forms.LIQUID))
                continue;

            materialFluids.put (Pair.of (material, AlchymReference.Materials.Forms.LIQUID), new MaterialFluid (material));
        }
    }

    public Fluid getMaterial (AlchymReference.Materials material) {
        return materialFluids.get (Pair.of (material, AlchymReference.Materials.Forms.LIQUID));
    }

    InitFluids (InitAlchym alchym) {
        super (Registry.FLUID, alchym);
    }

    @Override
    public void initialize () {
        for (Map.Entry<Pair<AlchymReference.Materials, AlchymReference.Materials.Forms>, Fluid> e : materialFluids.entrySet ()) {
            String name = e.getKey ().getLeft ().getName ();
            Fluid fluid = e.getValue ();

            if (fluid instanceof FlowableFluid) {
                register ("flowing_" + name, ((FlowableFluid) fluid).getFlowing ());
                register (name, ((FlowableFluid) fluid).getStill ());
            } else {
                register (name, fluid);
            }
        }
    }

    @Override
    void preRegister (String identifier, Fluid obj) {
        if (obj instanceof MaterialFluid && obj.isStill (null))
            alchym.blocks.queueFluidBlock (identifier,
                    new MaterialFluidBlock ((MaterialFluid) ((FlowableFluid) obj).getStill (), FabricBlockSettings.copy (Blocks.WATER)) {});
    }
}
