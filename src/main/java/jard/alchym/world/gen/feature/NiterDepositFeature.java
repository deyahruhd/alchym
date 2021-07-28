package jard.alchym.world.gen.feature;

import com.mojang.serialization.Codec;
import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.impl.biome.modification.BiomeSelectionContextImpl;
import net.minecraft.block.Blocks;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;

import java.util.*;
import java.util.function.Predicate;

/***
 *  NiterDepositFeature
 *  World generator feature which spawns niter deposit structures in caves.
 *
 *  Created by jard at 14:33 on March, 13, 2021.
 ***/
public class NiterDepositFeature extends Feature <DefaultFeatureConfig> implements AlchymFeature <DefaultFeatureConfig> {
    private static final List <Pair <BlockPos, Double>> OFFSETS_WEIGHTS = Arrays.asList (
            new Pair<> (new BlockPos (1, 0, 0), 0.33),
            new Pair<> (new BlockPos (1, 0, 1), 0.25),
            new Pair<> (new BlockPos (0, 0, 1), 0.33),
            new Pair<> (new BlockPos (-1, 0, 1), 0.25),
            new Pair<> (new BlockPos (-1, 0, 0), 0.33),
            new Pair<> (new BlockPos (-1, 0, -1), 0.25),
            new Pair<> (new BlockPos (0, 0, -1), 0.33),
            new Pair<> (new BlockPos (1, 0, 1), 0.25),
            new Pair<> (new BlockPos (0, 1, 0), 0.66)
    );

    public NiterDepositFeature (Codec<DefaultFeatureConfig> codec) {
        super (codec);
    }

    @Override
    public boolean generate (StructureWorldAccess world, ChunkGenerator generator, Random random, BlockPos position, DefaultFeatureConfig config) {
        BlockPos startPos = new BlockPos (position.getX (), 32, position.getZ ());
        int top = world.getTopPosition (Heightmap.Type.WORLD_SURFACE, position).getY ();

        while (startPos.getY () <= top &&
                // If the scan pos isn't cave air, or
                // If the scan pos is cave air but the block below it isn't a solid block
                (world.getBlockState (startPos).getBlock () != Blocks.CAVE_AIR ||
                ! world.getBlockState (startPos.add (0, -1, 0)).isOpaque ())) {
            startPos = startPos.add (0, +1, 0);
        }

        if (startPos.getY () > top)
            return false;

        placeDepositAt (world, random, startPos);

        return true;
    }

    @Override
    public ConfiguredFeature<?, ?> getConfiguration () {
        return configure (FeatureConfig.DEFAULT)
                .decorate (Decorator.CHANCE.configure (new ChanceDecoratorConfig (5)));
    }

    @Override
    public GenerationStep.Feature getGenerationStep () {
        return GenerationStep.Feature.SURFACE_STRUCTURES;
    }

    @Override
    public Predicate<BiomeSelectionContext> getSelectors () {
        return BiomeSelectors.all ();
    }

    private void placeDepositAt (StructureWorldAccess world, Random random, BlockPos rootPos) {
        int numBlocks = random.nextInt (8) + 5; // [5, 12]

        Map <BlockPos, Double> weightedPositions = new HashMap<> ();
        weightedPositions.put (rootPos, 1.0);

        for (int i = 0; i < numBlocks && ! weightedPositions.isEmpty (); ++ i) {
            double totalWeights = weightedPositions.values ().stream ().mapToDouble (d -> d).sum ();

            ArrayList <BlockPos> positions = new ArrayList <> (weightedPositions.keySet ());
            int index = 0;
            for (double r = Math.random () * totalWeights; index < positions.size () - 1; ++ index) {
                r -= weightedPositions.get (positions.get (index));

                if (r <= 0.0)
                    break;
            }

            BlockPos position = positions.get (index);

            double selectedWeight = weightedPositions.get (position);

            world.setBlockState (positions.get (index), Alchym.content ().blocks.niterrock.getDefaultState (), 3);

            weightedPositions.remove (position);

            for (Pair <BlockPos, Double> offset : OFFSETS_WEIGHTS) {
                BlockPos newPos = position.add (offset.getLeft ());

                if (! world.getBlockState (newPos).isOpaque ())
                    weightedPositions.putIfAbsent (newPos, offset.getRight () * selectedWeight);
            }
        }
    }
}
