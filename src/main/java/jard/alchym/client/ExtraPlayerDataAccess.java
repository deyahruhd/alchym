package jard.alchym.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.Vec3d;

import java.util.Stack;

public interface ExtraPlayerDataAccess {
    Vec3d getPrevVel ();
    Stack<Vec3d> getGrapple ();
    double getGrappleLength ();
    boolean isJumping ();

    ModelPart getCloak ();
}
