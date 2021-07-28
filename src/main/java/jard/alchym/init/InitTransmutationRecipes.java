package jard.alchym.init;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.exception.InvalidRecipeException;
import jard.alchym.api.ingredient.SolutionGroup;
import jard.alchym.api.ingredient.impl.FluidVolumeIngredient;
import jard.alchym.api.ingredient.impl.ItemStackIngredient;
import jard.alchym.api.recipe.TransmutationRecipe;
import jard.alchym.api.recipe.TransmuteSpecialBehavior;
import jard.alchym.api.transmutation.TransmutationInterface;
import jard.alchym.blocks.ChymicalContainerBlock;
import jard.alchym.blocks.blockentities.ChymicalContainerBlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.*;

/***
 *  InitTransmutationRecipes
 *  The initializing module that initializes every transmutation recipe in the mod.
 *
 *  Created by jard at 2:36 PM on May 5, 2018.
 ***/
public class InitTransmutationRecipes {
    protected SortedSet<TransmutationRecipe> transmutationSet = new TreeSet<>(new Comparator<TransmutationRecipe>() {
        @Override
        public int compare(TransmutationRecipe r1, TransmutationRecipe r2) {
            if (r1 == null || r2 == null)
                return 0;

            int complexityCheck = Integer.compare (r2.getInputs ().getCount (), r1.getInputs ().getCount ());

            if (complexityCheck != 0)
                return complexityCheck;

            if (r1.getOutputs () == null || r2.getOutputs () == null) {
                if (r2.getOutputs () != null)
                    return 1;
                else
                    return -1;
            }

            complexityCheck = Integer.compare (r2.getOutputs ().getCount (), r1.getOutputs ().getCount ());

            return complexityCheck != 0 ? complexityCheck : Integer.compare (r2.hashCode (), r1.hashCode ());
        }
    });

    protected final InitAlchym alchym;

    public InitTransmutationRecipes (InitAlchym alchym) {
        this.alchym = alchym;
    }

    public void initialize () {
        RecipeGroupAccessor accessor = RecipeGroupAccessor.getInstance ();

        try {
            register (new TransmutationRecipe ("make_alchymic_reference",
                    accessor.createRecipeGroup (TransmutationRecipe.TransmutationMedium.DRY,
                            new ItemStackIngredient (
                                    new ItemStack (alchym.items.getMaterial (AlchymReference.Materials.NITER, AlchymReference.Materials.Forms.CRYSTAL))),
                            new ItemStackIngredient (
                                    new ItemStack (Items.WRITABLE_BOOK))
                    ),
                    AlchymReference.Reagents.NITER,
                    TransmutationRecipe.TransmutationMedium.DRY,
                    TransmutationRecipe.TransmutationType.COAGULATION,
                    2L,
                    accessor.createRecipeGroup (TransmutationRecipe.TransmutationMedium.DRY,
                            new ItemStackIngredient (
                                    new ItemStack (alchym.items.alchymicReference))),
                    null));

            register (new TransmutationRecipe ("calcinate_vitriol",
                    accessor.createRecipeGroup (TransmutationRecipe.TransmutationMedium.WET,
                            new ItemStackIngredient (
                                    new ItemStack (alchym.items.getMaterial (AlchymReference.Materials.VITRIOL, AlchymReference.Materials.Forms.POWDER), 2))
                    ),
                    AlchymReference.Reagents.NITER,
                    TransmutationRecipe.TransmutationMedium.WET,
                    TransmutationRecipe.TransmutationType.CALCINATION,
                    1L,
                    accessor.createRecipeGroup (TransmutationRecipe.TransmutationMedium.WET,
                            new ItemStackIngredient (
                                    new ItemStack (alchym.items.getMaterial (AlchymReference.Materials.ASHEN_WASTE, AlchymReference.Materials.Forms.POWDER)))),
                    new TransmuteSpecialBehavior () {
                        @Override
                        public boolean modifyWorld (WorldAccess world, BlockPos position, int count) {
                            if (world.getBlockState (position.add (0, 1, 0)).getBlock () != Blocks.AIR &&
                                world.getBlockEntity (position.add (0, 1, 0)) instanceof ChymicalContainerBlockEntity) {
                                ChymicalContainerBlockEntity entity = (ChymicalContainerBlockEntity) world
                                        .getBlockEntity (position.add (0, 1, 0));

                                List <TransmutationRecipe.TransmutationType> ops = Arrays.asList (entity.getOps ());

                                if (ops.contains (TransmutationRecipe.TransmutationType.DISTILLATION)) {
                                    // Insert 500 mB of vitriolic acid into the container
                                    // TODO: This is going to need to be standardized against 81000, so we should be
                                    // TODO: inserting 20250 units soon.

                                    FluidVolume acid = FluidKeys.get (Alchym.content ().fluids.getMaterial (AlchymReference.Materials.VITRIOL))
                                            .withAmount (FluidAmount.of1620 (1620 / 2));

                                    entity.insertIngredient (new FluidVolumeIngredient (acid));
                                }
                            } else {
                                ((ServerWorld) world).spawnParticles (ParticleTypes.CLOUD,
                                        position.getX () + 0.5, position.getY () + 1.66, position.getZ () + 0.5,
                                        count,
                                        0, 0.1, 0,
                                        0.025f);

                                world.playSound (null, position.add (0, 1, 0),
                                        Alchym.content ().sounds.transmuteFumes, SoundCategory.BLOCKS,
                                        1.f, 1.f);
                            }

                            return true;
                        }
                    }));

            register (new TransmutationRecipe ("a_good_friend",
                    accessor.createRecipeGroup (TransmutationRecipe.TransmutationMedium.DRY,
                            new ItemStackIngredient (
                                    new ItemStack (alchym.items.aGoodFriendsCollar)
                            )),
                    AlchymReference.Reagents.NITER,
                    TransmutationRecipe.TransmutationMedium.DRY,
                    TransmutationRecipe.TransmutationType.COAGULATION,
                    1000L,
                    accessor.createRecipeGroup (TransmutationRecipe.TransmutationMedium.DRY),
                    new TransmuteSpecialBehavior () {
                        @Override
                        public boolean modifyWorld (WorldAccess world, BlockPos position, int count) {
                            Vec3d pos = new Vec3d (position.getX (), position.getY (), position.getZ ());
                            Box bounds = new Box (
                                    pos.subtract (new Vec3d (AlchymReference.DRY_TRANSMUTATION_RADIUS / 2.0, 1.0, AlchymReference.DRY_TRANSMUTATION_RADIUS / 2.0)),
                                    pos.add (new Vec3d (AlchymReference.DRY_TRANSMUTATION_RADIUS / 2.0, 1.0, AlchymReference.DRY_TRANSMUTATION_RADIUS / 2.0)));

                            List<Entity> wolfEntities = world.getEntitiesByClass (WolfEntity.class, bounds,
                                    wolf -> {
                                        if (wolf.getDisplayName () != null && ((WolfEntity) wolf).getOwnerUuid () != null)
                                            return (wolf.getDisplayName ().getString () + ((WolfEntity) wolf).getCollarColor () +
                                                    ((WolfEntity) wolf).getOwnerUuid ().toString ()).hashCode () == 1207256336;

                                        return false;
                                    }
                            );

                            if (wolfEntities.size () != 1)
                                return false;

                            Entity wolf = wolfEntities.get (0);
                            wolf.remove ();

                            return true;
                        }
                    }
            ));
        } catch (InvalidRecipeException e) {
            throw new RuntimeException ("An invalid recipe was supplied when registering transmutation recipes. Stacktrace: ", e);
        }
    }

    public TransmutationRecipe getClosestRecipe (TransmutationInterface source, ItemStack reagent, TransmutationRecipe.TransmutationMedium medium, WorldAccess world) {
        for (TransmutationRecipe recipe : transmutationSet) {
            if (recipe.matches (source, reagent, medium, world))
                return recipe;
        }

        return null;
    }

    private void register (TransmutationRecipe recipe) {
        transmutationSet.add (recipe);
    }
}
