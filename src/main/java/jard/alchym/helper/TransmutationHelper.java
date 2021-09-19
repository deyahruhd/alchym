package jard.alchym.helper;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.exception.InvalidActionException;
import jard.alchym.api.exception.InvalidInterfaceException;
import jard.alchym.api.ingredient.Ingredient;
import jard.alchym.api.ingredient.SolutionGroup;
import jard.alchym.api.ingredient.impl.ItemStackIngredient;
import jard.alchym.api.recipe.TransmutationRecipe;
import jard.alchym.api.transmutation.ReagentItem;
import jard.alchym.api.transmutation.TransmutationAction;
import jard.alchym.api.transmutation.impl.DryTransmutationInterface;
import jard.alchym.api.transmutation.impl.WetTransmutationInterface;
import jard.alchym.blocks.blockentities.ChymicalContainerBlockEntity;
import jard.alchym.items.MaterialItem;
import jard.alchym.items.PhilosophersStoneItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Pair;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/***
 *  TransmutationHelper
 *  Contains various helper methods relating to transmutation (e.g. handling of dry transmutation or item entity raytracing methods)
 *
 *  Created by jard at 5:18 PM on February 13, 2018.
 ***/
public class TransmutationHelper {
    public static boolean tryDryTransmute (World world, PlayerEntity player, ItemStack reagent) {
        if (!isReagent(reagent))
            return false;

        ItemEntity itemEntity = getLookedAtItem(player, 1.f);
        if (itemEntity == null)
            return false;

        Pair<World, Vec3d> endpoint = new Pair<>(world, itemEntity.getPos ());

        DryTransmutationInterface source, target;

        try {
            source = new DryTransmutationInterface (endpoint);
            target = new DryTransmutationInterface (endpoint);
        } catch (InvalidInterfaceException e) {
            return false;
        }

        TransmutationRecipe recipe = Alchym.content ().getTransmutations ()
                .getClosestRecipe (source, reagent, TransmutationRecipe.TransmutationMedium.DRY, world);

        if (recipe == null)
            return false;

        int recipeScale = recipe.getRecipeScale (source);
        int reagentScale = (int) (getReagentCharge (reagent) / recipe.getCharge ());

        if (reagentScale < recipeScale)
            recipeScale = reagentScale;

        if (recipeScale == 0)
            throw new RuntimeException ("Attempted transmutation recipe '" + recipe.getName () + "', which returned a " +
                    "0 recipe scale. This should never happen, something is gravely broken");

        TransmutationAction action = new TransmutationAction(source, target, recipe, world);

        try {
            if (action.apply(reagent, new BlockPos (itemEntity.getPos()))) {
                if (reagent.getItem () instanceof PhilosophersStoneItem) {
                    // Just subtract off the current recipe's cost times recipeScale
                } else if (reagent.getItem () instanceof MaterialItem) {
                    if (((MaterialItem) reagent.getItem ()).form == AlchymReference.Materials.Forms.REAGENT_POWDER) {
                        long newCharge = getReagentCharge (reagent) - (recipe.getCharge () * recipeScale);

                        reagent.setCount ((int) newCharge);
                    } else {
                        throw new IllegalStateException ("Player '" + player.getDisplayName () +
                                "' attempted a transmutation with a non-reagent item '" +
                                reagent.getItem ().getName () + "'!");
                    }
                }

                world.playSound(null, new BlockPos (itemEntity.getPos ()), Alchym.content().sounds.dryTransmute, SoundCategory.PLAYERS, 1.f, 1.f);
            }
        }
        catch (InvalidActionException e) {
            return false;
        }

        return true;
    }

    public static boolean tryWetTransmute (World world, ChymicalContainerBlockEntity container, Ingredient reagent) {
        if (! (reagent instanceof ItemStackIngredient) || !isReagent (((ItemStackIngredient) reagent).unwrap()))
            return false;

        WetTransmutationInterface source, target;

        try {
            source = new WetTransmutationInterface (container);
            target = new WetTransmutationInterface (container);
        } catch (InvalidInterfaceException e) {
            return false;
        }

        TransmutationRecipe recipe = Alchym.content ().getTransmutations ()
                .getClosestRecipe (source, ((ItemStackIngredient) reagent).unwrap(), TransmutationRecipe.TransmutationMedium.WET, world);

        if (recipe == null)
            return false;

        // Calcination can not happen if there is no insoluble group in the container
        if (recipe.type == TransmutationRecipe.TransmutationType.CALCINATION && ! container.hasOnlyInsoluble ())
            return false;

        int recipeScale = recipe.getRecipeScale (source);
        int reagentScale = (int) (getReagentCharge (((ItemStackIngredient) reagent).unwrap ()) / recipe.getCharge ());

        if (reagentScale < recipeScale)
            recipeScale = reagentScale;

        if (recipeScale == 0)
            throw new RuntimeException ("Attempted transmutation recipe '" + recipe.getName () + "', which returned a " +
                    "0 recipe scale. This should never happen, something is gravely broken");

        TransmutationAction action = new TransmutationAction(source, target, recipe, world);

        try {
            if (action.apply (((ItemStackIngredient) reagent).unwrap (), container.getPos ())) {
                container.pullIngredient (reagent);

                AlchymReference.Materials baseMaterial = ((MaterialItem) ((ItemStackIngredient) reagent).unwrap ().getItem ()).material;
                Item baseItem;
                int baseCount = (int) (getReagentCharge (((ItemStackIngredient) reagent).unwrap ()) - (recipe.getCharge () * recipeScale));

                baseItem = Alchym.content ().items.getMaterial (baseMaterial, AlchymReference.Materials.Forms.REAGENT_POWDER);

                ItemStackIngredient newReagent = new ItemStackIngredient (new ItemStack (baseItem, baseCount));

                if (! newReagent.isEmpty ()) {
                    SolutionGroup groupToTransmute = container.insertIngredient (newReagent);
                    container.postInsert (groupToTransmute);
                }
            }
        } catch (InvalidActionException e) {
            return false;
        }

        return true;
    }

    public static boolean isReagent (ItemStack reagent) {
        return ! reagent.isEmpty() && reagent.getItem () instanceof ReagentItem && ((ReagentItem) reagent.getItem()).isReagent();
    }

    public static long getReagentCharge (ItemStack reagent) {
        if (! isReagent (reagent))
            return 0L;

        if (reagent.getItem() instanceof PhilosophersStoneItem)
            PhilosophersStoneItem.setHeldStack (reagent);

        return ((ReagentItem) reagent.getItem ()).getUnitCharge() * reagent.getCount();
    }

    // Returns the ItemEntity that a player may be looking at, or null if the player is not looking at any ItemEntity.
    public static ItemEntity getLookedAtItem (PlayerEntity player, float partialTicks) {
        ItemEntity item = null;

        if (player != null && player.world != null) {
            double reach = 4.5;

            boolean flag = false;
            if (player.isCreative ())
                reach = 6.0D;
            else
                flag = reach > 3.0D;

            double reachSq = reach * reach;

            Vec3d pos  = player.getCameraPosVec (partialTicks);
            Vec3d look = player.getRotationVec (partialTicks);

            Box box = player.getBoundingBox ().stretch(look.multiply (reach)).expand(1.0D, 1.0D, 1.0D);
            EntityHitResult hitResult = ProjectileUtil.raycast (player, pos, pos.add (look.multiply (reach)), box, (entity) ->
                    entity instanceof ItemEntity, reachSq);
            if (hitResult != null) {
                ItemEntity entity = (ItemEntity) hitResult.getEntity ();
                double dist = pos.squaredDistanceTo (hitResult.getPos ());
                if (! (flag && dist > 9.0D) && dist < reachSq)
                    item = entity;
            }
        }

        return item;
    }

    // Calculates the averaged position of all of the ItemEntities in the argument.
    private static Vec3d getTransmutationCenter (ItemEntity [] items) {
        Vec3d transmutationCenter = Vec3d.ZERO;

        for (ItemEntity item : items) {
            transmutationCenter.add (new Vec3d (item.getX (), item.getY (), item.getZ ()));
        }

        if (items.length > 0)
            transmutationCenter.multiply (1.0 / ((double) items.length));

        return transmutationCenter;
    }

    public static Fluid getFluidFromBucket (Item bucket) {
        if (bucket == Items.WATER_BUCKET)
            return Fluids.WATER;
        else if (bucket == Items.LAVA_BUCKET)
            return Fluids.LAVA;
        else
            return null;
    }

    public static Vec3d bumpFromSurface (BlockHitResult cast, double radius) {
        double len = net.minecraft.util.math.MathHelper.clamp (0.34 - 0.08 * radius, 0.1, 0.3);
        Vec3d normal = new Vec3d (cast.getSide ().getUnitVector ()).multiply (len);
        return cast.getPos ().add(normal);
    }

    public static HitResult raycastEntitiesAndBlocks (Entity originator, World world, Vec3d start, Vec3d end) {
        HitResult blockCast = world.raycast (new RaycastContext (start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, originator));
        HitResult entityCast = null;
        Box entitySearch = new Box (start, end).expand (0.5, 0.5, 0.5);

        List <LivingEntity> list = world.getEntitiesByType (
                TypeFilter.instanceOf (LivingEntity.class),
                entitySearch,
                livingEntity -> ! livingEntity.equals (originator)
        );

        float closestEntityCast = Float.POSITIVE_INFINITY;

        for (LivingEntity entity : list) {
            Box entityBox = entity.getBoundingBox ();
            Optional <Vec3d> entityBoxCast = entityBox.raycast (start, end);

            if (entityBox.contains (start) && entityBoxCast.isEmpty ()) {
                entityCast = new EntityHitResult (entity, start);
            } else if (entityBoxCast.isPresent () && entity.squaredDistanceTo (start) < closestEntityCast) {
                closestEntityCast = (float) entity.squaredDistanceTo (start);
                entityCast = new EntityHitResult (entity, entityBoxCast.get ());
            }
        }

        // Calculate the distance of the closest entity and block and determine the minimum of the two. Whichever field is the
        // smallest value determines the correct RayTraceResult to return.
        float blockDist  = blockCast.getType () == HitResult.Type.MISS ? Float.POSITIVE_INFINITY  : (float) (start.squaredDistanceTo (blockCast.getPos ()));
        float entityDist = entityCast == null ? Float.POSITIVE_INFINITY : (float) (start.squaredDistanceTo (entityCast.getPos ()));

        if (blockDist == Float.POSITIVE_INFINITY && entityDist == Float.POSITIVE_INFINITY)
            return BlockHitResult.createMissed (end, null, null);

        return blockDist < entityDist ? blockCast : entityCast;
    }
}
