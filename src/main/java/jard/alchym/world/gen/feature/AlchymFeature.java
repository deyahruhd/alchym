package jard.alchym.world.gen.feature;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;

import java.util.function.Predicate;

/***
 *  AlchymFeature
 *  Generic abstract template feature class for use by Alchym's world generation.
 *
 *  Created by jard at 14:39 on March, 13, 2021.
 ***/
public interface AlchymFeature <T extends FeatureConfig> {
    ConfiguredFeature <?, ?> getConfiguration ();

    GenerationStep.Feature getGenerationStep ();

    Predicate <BiomeSelectionContext> getSelectors ();
}
