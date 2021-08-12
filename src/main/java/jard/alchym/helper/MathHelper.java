package jard.alchym.helper;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class MathHelper {
    public static double FLOAT_ZERO_THRESHOLD = 1.0E-7D;

    public static int rectify (int i) {
        return Math.max (0, i);
    }

    public static Vec3d lerp (Vec3d p, Vec3d n, double factor) {
        return p.add (n.subtract (p).multiply (factor));
    }

    public static boolean implies (boolean a, boolean b) {
        return ! (a && ! b);
    }

    public static boolean inRange (float val, float min, float max) {
        return val >= min && val <= max;
    }

    public static float quinticSpline (float x, float xMid, float [][] coeffs) {
        if (x <= 0.f || x >= 1.f)
            return 0.f;

        float xPow = 1.f, polynomial = 0.f;
        int index = 0;

        if (x >= xMid)
            index = 1;

        for (int i = 0; i < 6; ++ i) {
            polynomial += (xPow * coeffs [index][i]);
            xPow *= x;
        }

        return polynomial;
    }

    public static Vec3d castToBlockEdge (Vec3d initial, BlockPos blockPos, Direction side, float width) {
        Vec3d blockCenter = new Vec3d (blockPos.getX () + 0.5, blockPos.getY () + 0.5, blockPos.getZ () + 0.5);
        Vec3d result = initial.subtract (blockCenter);
        double maxCoord, scale;

        switch (side) {
            case WEST:
            case EAST:
                maxCoord = Math.max (Math.abs (result.y), Math.abs (result.z));
                scale = (0.5 + width) / maxCoord;
                result = result.multiply (1.0, scale, scale).add (width * side.getOffsetX (), 0.0, 0.0);
                break;
            case UP:
            case DOWN:
                maxCoord = Math.max (Math.abs (result.x), Math.abs (result.z));
                scale = (0.5 + width) / maxCoord;
                result = result.multiply (scale, 1.0, scale).add (0.0, width * side.getOffsetY (), 0.0);
                break;
            case NORTH:
            case SOUTH:
                maxCoord = Math.max (Math.abs (result.x), Math.abs (result.y));
                scale = (0.5 + width) / maxCoord;
                result = result.multiply (scale, scale, 1.0).add (0.0, 0.0, width * side.getOffsetZ ());
                break;
        }

        return result.add (blockCenter);
    }
}
