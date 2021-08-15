package jard.alchym.client;

import net.minecraft.util.math.Vec3d;

public interface QuakeKnockbackable {
    void radialKnockback (Vec3d from, float radius, double verticalStrength, double horizontalStrength, boolean skim, boolean icy);
}
