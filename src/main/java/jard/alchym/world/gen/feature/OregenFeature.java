package jard.alchym.world.gen.feature;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

import java.util.Random;
import java.util.function.Predicate;

/***
 *  OregenFeature
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 20:26 on March, 18, 2021.
 ***/
public class OregenFeature extends OreFeature implements AlchymFeature <OreFeatureConfig> {
    private RangeDecoratorConfig rangeConfig;
    private final Predicate <BiomeSelectionContext> selectors;

    private final OreFeatureConfig mainConfig;
    private final int veinsPerChunk;

    public OregenFeature (Block ore, int veinSize, int veinsPerChunk, int lowerYBound, int upperYBound,
                          int maxRange, Predicate <BiomeSelectionContext> selectors, RuleTest generateIn) {
        super (OreFeatureConfig.CODEC);

        this.mainConfig = new OreFeatureConfig (generateIn, ore.getDefaultState (), veinSize);
        this.rangeConfig = new RangeDecoratorConfig (lowerYBound, upperYBound, maxRange);
        this.selectors = selectors;

        this.veinsPerChunk = veinsPerChunk;
    }

    @Override
    public ConfiguredFeature<?, ?> getConfiguration () {
        return configure (mainConfig)
                .decorate (Decorator.RANGE.configure (rangeConfig)
                .spreadHorizontally ()
                .repeat (veinsPerChunk));
    }

    @Override
    public final GenerationStep.Feature getGenerationStep () {
        return GenerationStep.Feature.UNDERGROUND_ORES;
    }

    @Override
    public Predicate<BiomeSelectionContext> getSelectors () {
        return selectors;
    }
}
