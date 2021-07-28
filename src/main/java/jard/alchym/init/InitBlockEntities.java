package jard.alchym.init;

import jard.alchym.AlchymReference;
import jard.alchym.blocks.blockentities.ChymicalContainerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

/***
 *  InitBlockEntities
 *  The initializing module that initializes every {@link BlockEntity} in the mod.
 *
 *  Created by jard at 1:08 AM on January 25, 2019.
 ***/
public class InitBlockEntities extends InitAbstract <BlockEntityType <?>> {
    InitBlockEntities (InitAlchym alchym) {
        super (Registry.BLOCK_ENTITY_TYPE, alchym);
    }

    public final BlockEntityType chymicalContainerBlockEntity = from (ChymicalContainerBlockEntity::new);

    @Override
    public void initialize () {
        register (AlchymReference.BlockEntities.CHYMICAL_CONTAINER.getName (), chymicalContainerBlockEntity);
    }
    private final BlockEntityType from (Supplier<BlockEntity> blockEntity) {
        return BlockEntityType.Builder.create (blockEntity).build (null);
    }
}
