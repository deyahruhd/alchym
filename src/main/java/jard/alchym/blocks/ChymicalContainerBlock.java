package jard.alchym.blocks;

import jard.alchym.Alchym;
import jard.alchym.AlchymReference;
import jard.alchym.api.recipe.TransmutationRecipe;
import jard.alchym.blocks.blockentities.ChymicalContainerBlockEntity;
import jard.alchym.init.InitItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Set;

/***
 *  ChymicalContainerBlock
 *  A generic solution container block which is instantiated with a capacity and a list of actions it can perform on its contents.
 *
 *  Created by jard at 12:43 PM on January 17, 2019.
 ***/
public class ChymicalContainerBlock extends BlockWithEntity implements AlchymBlock {
    private AlchymReference.ChymicalContainers container;

    public ChymicalContainerBlock (Settings settings, AlchymReference.ChymicalContainers container) {
        super (settings);
        this.container = container;
    }

    @Override
    public BlockRenderType getRenderType(BlockState blockState_1) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity (BlockView var1) {
        return new ChymicalContainerBlockEntity (container);
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView view, BlockPos pos) { return true; }

    @Override
    public ActionResult onUse (BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient)
            return ActionResult.PASS;

        ItemStack heldItem = player.getEquippedStack (hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

        if (!heldItem.isEmpty ()
                && world.getBlockEntity (pos) instanceof ChymicalContainerBlockEntity
                && ((ChymicalContainerBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).canAccept (heldItem)) {
            player.setStackInHand (hand,
                    ((ChymicalContainerBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).insertHeldItem (state, world, pos, player,
                            heldItem)
            );
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced (BlockState state, World world, BlockPos pos, BlockState state2, boolean b) {
        if (state.getBlock () != state2.getBlock () && ! world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity (pos);
            if (blockEntity instanceof ChymicalContainerBlockEntity) {
                ItemScatterer.spawn(world, pos, ((ChymicalContainerBlockEntity) blockEntity).getDrops ());
            }

            super.onStateReplaced (state, world, pos, state2, b);
        }
    }

    @Override
    public VoxelShape getCullingShape (BlockState state, BlockView view, BlockPos pos) {
        return container.boundingBox;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext shapeContext) {
        return container.boundingBox;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext shapeContext) {
        return container.boundingBox;
    }

    @Override
    public VoxelShape getVisualShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext shapeContext) {
        return container.boundingBox;
    }

    @Override
    public Item.Settings blockItemSettings () {
        return AlchymReference.LARGE_GLASSWARE_SETTINGS;
    }
}
