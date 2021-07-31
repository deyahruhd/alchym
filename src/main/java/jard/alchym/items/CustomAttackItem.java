package jard.alchym.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

/***
 *  CustomUseAnimItem
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 01:04 on June, 07, 2021.
 ***/
public interface CustomAttackItem {
    Matrix4f getAnimMatrix (ItemStack stack, Arm arm, float progress);

    int getSwingDuration (ItemStack stack);

    int getAttackCooldown (ItemStack stack);

    @Environment (EnvType.CLIENT)
    boolean clientAttack (PlayerEntity player, ItemStack stack, Vec3d aimDir);
}
