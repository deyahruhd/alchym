package jard.alchym.api.transmutation.revolver;

/***
 *  RevolverBehavior
 *  Structure which bundles the three bullet function objects together.
 *
 *  Created by jard at 13:48 on September 16, 2021.
 ***/
public record RevolverBehavior (RevolverDirectHitFunction direct, RevolverSplashHitFunction splash, RevolverBulletTravelFunction travel) {
    public static final RevolverBehavior NONE = new RevolverBehavior (
                (bullet, target, hitPos, vel, random) -> {},
                (player, w, radius, vel, hitPos, hitNormal, visualPos, random, targets) -> {},
                (bullet, pos, random) -> {}
            );
}
