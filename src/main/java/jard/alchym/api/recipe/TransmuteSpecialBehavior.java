package jard.alchym.api.recipe;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

/***
 *  TransmuteSpecialBehavior
 *  Contains a method utilized by transmutation recipes to perform modifications on the world when a transmutation occurs.
 *
 *  @see TransmutationRecipe#specialBehavior
 *
 *  TODO: Implement a simple scripting language that can allow pack developers to program how the world should be modified.
 *  TODO: Currently, an instance of an anonymous class is created from this class where the modifyWorld method is overridden,
 *  TODO: which is not extensible in the long run.
 *
 *  Created by jared at 12:02 AM on May 06, 2018.
 ***/
public abstract class TransmuteSpecialBehavior {
    public boolean modifyWorld (WorldAccess world, BlockPos position, int count) {
        return true;
    }
}
