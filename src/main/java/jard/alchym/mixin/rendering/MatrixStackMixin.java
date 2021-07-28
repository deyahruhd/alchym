package jard.alchym.mixin.rendering;

import jard.alchym.client.MatrixStackAccess;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Deque;

/***
 *  MatrixStackMixin
 *  Mixins the {@link jard.alchym.client.MatrixStackAccess} interface into
 *  {@link net.minecraft.client.util.math.MatrixStack}.
 *
 *  Created by jard at 19:32 on January, 02, 2021.
 ***/
@Mixin (MatrixStack.class)
public abstract class MatrixStackMixin implements MatrixStackAccess {
    @Shadow
    @Final
    private Deque<MatrixStack.Entry> stack;

    public void multiply (Matrix4f matrix) {
        Matrix3f matrix3f = new Matrix3f (matrix);

        MatrixStack.Entry entry = this.stack.getLast();
        entry.getModel ().multiply  (matrix);
        entry.getNormal ().multiply (matrix3f);
    }
}
