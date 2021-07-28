package jard.alchym.items;

import jard.alchym.api.ingredient.SolutionGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/***
 *  ContainsSolutionItem
 *  Abstract class which manages and serializes a {@link SolutionGroup} within an {@link ItemStack}.
 *
 *  Created by jard at 02:01 on March, 07, 2021.
 ***/
public abstract class ContainsSolutionItem extends Item {
    public ContainsSolutionItem (Settings settings) {
        super (settings);
    }

    public static SolutionGroup getSolutionGroup (ItemStack stack) {
        if (! stack.hasTag ()) {
            stack.setTag (new CompoundTag ());
        }

        SolutionGroup group = new SolutionGroup ();

        if (! stack.getTag ().contains ("ContainedGroup"))
            stack.setTag (group.toTag (new CompoundTag ()));
        else
            group.fromTag ((CompoundTag) stack.getTag ().get ("ContainedGroup"));

        return group;
    }
}
