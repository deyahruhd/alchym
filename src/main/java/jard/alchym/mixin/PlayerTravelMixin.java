/*
 * The following notice pertains to the implementation of `playerGrappleMove` below.
 *
 * All Rights Reserved
 *
 * Copyright (c) 1996 Perecli Manole
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jard.alchym.mixin;

import jard.alchym.client.ExtraPlayerDataAccess;
import jard.alchym.client.QuakeKnockbackable;
import jard.alchym.helper.MathHelper;
import jard.alchym.helper.MovementHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Stack;

/***
 *  PlayerTravelMixin
 *  Mixes in Quake movement physics into the player movement logic.
 *
 *  Created by jard at 23:51 on June, 15, 2021.
 ***/
@Mixin (PlayerEntity.class)
public abstract class PlayerTravelMixin extends LivingEntity implements QuakeKnockbackable {
    private static final float WALKSPEED                  = MovementHelper.upsToSpt (320.f);
    private static final float STOPSPEED                  = MovementHelper.upsToSpt (320.f);
    private static final float AIRSPEED                   = MovementHelper.upsToSpt (240.f);
    private static final float AIRSTRAFE_SPEED            = MovementHelper.upsToSpt (40.f);
    private static final float GRAPPLE_PULL_MAX_SPEED     = MovementHelper.upsToSpt (960.f);

    private static final float GROUND_ACCEL               = 9.5f  / 20.f;
    private static final float AIR_ACCEL                  = 1.0f  / 20.f;
    private static final float AIRSTRAFE_ACCEL            = 10.0f / 20.f;
    private static final float FRICTION                   = 3.5f  / 20.f;

    private static final float GRAPPLE_RESTRAINMENT_ACCEL = 0.5f  / 20.f;
    private static final float GRAPPLE_PULL_ACCEL         = 1.5f  / 20.f;
    private static final float GRAPPLE_TENSION_THRESHOLD  = 0.25f;
    private static final float GRAPPLE_LINK_WIDTH         = 0.1f;

    private static boolean wasOnGround = true;
    private static Vec3d displacement  = Vec3d.ZERO;
    private static int skimTimer       = 0;
    private static int gbTimer         = 0;

    @Shadow
    @Final
    public PlayerAbilities abilities;

    @Shadow
    public void increaseTravelMotionStats (double d, double e, double f) {}
    @Shadow
    public void incrementStat (Identifier stat) {}
    @Shadow
    public void addExhaustion (float exhaustion) {}

    protected PlayerTravelMixin (EntityType<? extends LivingEntity> entityType, World world) {
        super (entityType, world);
    }

    private static java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

    @Inject (method = "travel", at = @At ("HEAD"), cancellable = true)
    public void hookTravel (Vec3d movementIn, CallbackInfo info) {
        if (!world.isClient)
            return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;


        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance ();
        float speed = (float) getVelocity ().multiply (1.f, 0.f, 1.f).length () * 1140.4134f;
        client.inGameHud.setOverlayMessage (new net.minecraft.text.LiteralText ((int) speed + " ups"), false);



        setSprinting (false);

        Vec3d prevPos = player.getPos ();

        Vec3d wishDir = MovementHelper.getWishDir (player.getYaw (0.f), movementIn);

        if (quakeMovement (player, wishDir)) {
            Vec3d preSkimVel = player.getVelocity ().multiply (1.0, 0.0, 1.0);

            if (isOnGround () && ! wasOnGround)
                skimTimer = 5;

            // Update all tracking variables
            wasOnGround = player.isOnGround ();

            player.move (MovementType.SELF, player.getVelocity ());

            if (! player.isOnGround ())
                preSkimVel = preSkimVel.add (0.0, player.getVelocity ().y, 0.0);

            if (0 < skimTimer && skimTimer <= 5)
                player.setVelocity (preSkimVel);
        } else
            super.travel(wishDir);

        displacement = getPos ().subtract (prevPos);

        increaseTravelMotionStats (displacement.x, displacement.y, displacement.z);

        updateLimbs (this, this instanceof Flutterer);

        if (skimTimer > 0)
            skimTimer --;

        if (gbTimer > 0)
            gbTimer --;

        info.cancel ();
    }

    @Inject (method = "jump", at = @At ("HEAD"))
    public void stopSprintJump (CallbackInfo info) {
        setSprinting (false);
    }

    @Inject (method = "clipAtLedge", at = @At ("HEAD"), cancellable = true)
    public void dontClipOnLedgeIfSliding (CallbackInfoReturnable <Boolean> info) {
        if (isSneaking () && getVelocity ().multiply (1.f, 0.f, 1.f).length () >= WALKSPEED) {
            info.setReturnValue (false);
            info.cancel ();
        }
    }

    @Inject (method = "jump", at = @At ("HEAD"), cancellable = true)
    public void additiveJump (CallbackInfo info) {
        info.cancel();

        double d = (double) this.getJumpVelocity() + this.getJumpBoostVelocityModifier ();
        Vec3d vel = this.getVelocity();
        this.setVelocity(vel.x, Math.max (d + vel.y, d), vel.z);

        this.incrementStat (Stats.JUMP);
        this.addExhaustion(0.2F);
    }

    @Override
    public void radialKnockback (Vec3d from, float radius, double verticalStrength, double horizontalStrength, boolean skim, boolean icy) {
        Vec3d to = MovementHelper.getKnockbackTo ((ClientPlayerEntity) (Object) this, from, radius);
        Vec3d dir = to.subtract (from);

        double scale = net.minecraft.util.math.MathHelper.clamp (
                (radius * radius - dir.lengthSquared ()) / (radius * radius), 0.0, 1.0);
        Vec3d knockback = dir
                .normalize ().multiply (horizontalStrength * scale, verticalStrength * scale, horizontalStrength * scale);

        addVelocity (knockback.x, knockback.y, knockback.z);

        if (scale > 0.f) {
            if (skim && skimTimer == 0)
                skimTimer = 5;

            if (icy && gbTimer == 0 && !onGround)
                gbTimer = 5;
        }
    }

    private boolean quakeMovement (ClientPlayerEntity player, Vec3d wishDir) {
        Stack <Vec3d> grappleLinks = ((ExtraPlayerDataAccess) player).getGrapple ();
        double grappleLength = ((ExtraPlayerDataAccess) player).getGrappleLength ();
        if (! grappleLinks.empty () && grappleLength > 0.0) {
            double currentGrappleLength = playerMaintainGrappleLinks (player, grappleLinks);

            playerGrappleMove (player, grappleLinks.peek (), grappleLength - currentGrappleLength);
        }

        if (player.isOnGround () && ! jumping)
            playerWalkMove (player, wishDir);
        else
            playerAirMove (player, wishDir);

        player.setVelocity (player.getVelocity ().multiply (1.f, 0.9800000190734863f, 1.f));

        return true;
    }

    private void playerWalkMove (ClientPlayerEntity player, Vec3d wishDir) {
        float walkSpeed = WALKSPEED;
        float frictionSpeed = STOPSPEED;

        float accel = GROUND_ACCEL;
        float frictionAccel = FRICTION;

        if (player.isSneaking () || player.isInSneakingPose ()) {
            walkSpeed *= 0.25f;
            frictionSpeed *= 0.25f;
        }

        if (player.isSneaking () && getVelocity ().multiply (1.f, 0.f, 1.f).length () >= WALKSPEED) {
            double yStep = displacement.y;
            if (yStep > MathHelper.FLOAT_ZERO_THRESHOLD && yStep <= player.stepHeight) {
                skimTimer = 5;
            } else if (skimTimer == 0 || skimTimer >= 5)
                skimTimer = 6;
            frictionAccel *= 0.025f;
            walkSpeed = AIRSPEED;
            accel = AIR_ACCEL;
        }
        // TODO: Apply slick and ground accel whenever the player receives knockback.
        if (0 < gbTimer && gbTimer <= 8) {
            frictionAccel = 0.0f;
            walkSpeed = WALKSPEED;
            accel = GROUND_ACCEL;
        }

        MovementHelper.playerFriction (player, frictionAccel, frictionSpeed);

        player.addVelocity (0.f, -0.0784f, 0.f);


        double prevSpeed = player.getVelocity ().multiply (1.0, 0.0, 1.0).length ();
        // Ground move
        MovementHelper.playerAccelerate (player, wishDir, walkSpeed, accel);
        double currSpeed = player.getVelocity ().multiply (1.0, 0.0, 1.0).length ();

        // Cap speed if the player is sliding
        if (player.isSneaking () && currSpeed > prevSpeed && currSpeed > WALKSPEED) {
            double cap = prevSpeed / currSpeed;
            player.setVelocity (player.getVelocity ().multiply (cap, 1.0, cap));
        }
    }

    private void playerAirMove (ClientPlayerEntity player, Vec3d wishDir) {
        if (player.isOnGround () && jumping && wasOnGround) {
            playerWalkMove (player, wishDir);
            return;
        }

        //if (player.isSneaking ())
        //    MovementHelper.playerFriction (player, FRICTION * 0.05f, STOPSPEED);

        player.addVelocity (0.f, player.getVelocity ().y > 0.f ? -0.0524f : -0.0784f, 0.f);

        Vec3d prevHorizontalVel = player.getVelocity ().multiply (1.0, 0.0, 1.0);
        double horizontalSpeed = prevHorizontalVel.length ();

        /* Apply strafe accel with diminishing strength if player is traveling over the walk speed
         *
         * This should give strafe that reaches a soft cap (from testing, roughly 762 UPS), which allows players to
         * gain appreciable speed through strafing skill, but not to where strafing completely dominates the speed meta.
         *
         * Knockback weapons are necessary to attain speeds higher than the soft cap by design.
         */
        // VQ3
        MovementHelper.playerAccelerate (player, wishDir, AIRSPEED, AIR_ACCEL);
        // QW
        if (Math.abs (player.getVelocity ().dotProduct (wishDir)) < AIRSTRAFE_SPEED)
            MovementHelper.playerAccelerate (player, wishDir, AIRSTRAFE_SPEED, AIRSTRAFE_ACCEL);

        double newSpeed = player.getVelocity ().multiply (1.0, 0.0, 1.0).lengthSquared ();
        // Only apply the cap if the player has gained speed
        if (newSpeed > (horizontalSpeed * horizontalSpeed) && newSpeed > WALKSPEED * WALKSPEED) {
            double strafePenalty = WALKSPEED * WALKSPEED / newSpeed;
            Vec3d delta          = player.getVelocity ().multiply (1.0, 0.0, 1.0).subtract (prevHorizontalVel);
            Vec3d softCappedVel  = prevHorizontalVel.add (delta.multiply (strafePenalty));
            softCappedVel = softCappedVel.add (0.0, player.getVelocity ().y, 0.0);

            player.setVelocity (softCappedVel);
        }
    }

    private double playerMaintainGrappleLinks (ClientPlayerEntity player, Stack<Vec3d> links) {
        Vec3d playerPos = player.getPos ().add (0.0, player.getEyeHeight (player.getPose ()) - 0.375, 0.0);
        Vec3d playerPrevPos = new Vec3d (player.prevX, player.prevY + player.getEyeHeight (player.getPose ()) - 0.375, player.prevZ);

        while (links.size () > 1) {
            Vec3d linkToBreak = links.peek ();
            Vec3d castStart = links.elementAt (links.size () - 2);

            boolean linkNotBroken = false;

            for (float step = 0.f; step <= 1.f && ! linkNotBroken; step += 0.01f) {
                Vec3d castEnd = MathHelper.lerp (linkToBreak, playerPos, step);

                BlockHitResult cast = player.world.raycast (new RaycastContext (castEnd, castStart,
                        RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

                if (cast.getType () == HitResult.Type.BLOCK)
                    linkNotBroken = true;
            }

            if (linkNotBroken)
                break;

            links.pop ();
        }

        Vec3d castStart = links.peek ();

        for (float step = 0.f; step <= 1.f; step += 0.01f) {
            Vec3d castEnd = MathHelper.lerp (playerPrevPos, playerPos, step);

            BlockHitResult cast = player.world.raycast (new RaycastContext (castEnd, castStart,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

            if (cast.getType () == HitResult.Type.BLOCK) {
                links.push (MathHelper.castToBlockEdge (cast.getPos (), cast.getBlockPos (), cast.getSide (), GRAPPLE_LINK_WIDTH));
                break;
            }
        }

        double length = 0;
        Vec3d previous = null;
        for (Vec3d link : links) {
            if (previous == null) {
                previous = link;
                continue;
            }

            length += link.subtract (previous).length ();
            previous = link;
        }

        return length;
    }

    /*
     * All Rights Reserved
     *
     * Copyright (c) 1996 Perecli Manole
     */
    private void playerGrappleMove (ClientPlayerEntity player, Vec3d linkPos, double linkMaxLength) {
        Vec3d linkVec = linkPos.subtract (player.getPos ().add (0.f, player.getEyeHeight (player.getPose ()), 0.f));
        double linkLength = linkVec.length ();
        double restrainment = 0.0;

        double velDot = player.getVelocity ().dotProduct (linkVec);
        double pullDot = Math.abs (player.getVelocity ().add(linkVec.normalize ()).normalize ().dotProduct (linkVec.normalize ()));

        player.addVelocity (0.0, (0.0524 * (1.0 - pullDot)), 0.0);

        MovementHelper.playerAccelerate (player, linkVec.normalize (), GRAPPLE_PULL_MAX_SPEED, GRAPPLE_PULL_ACCEL * (float) (0.1 + 0.9 * pullDot));

        if (linkLength > linkMaxLength) {
            Vec3d playerVelPart = linkVec.multiply (velDot / linkVec.dotProduct (linkVec));

            double defaultRestrainment = (linkVec.length () - linkMaxLength) * GRAPPLE_RESTRAINMENT_ACCEL;

            if (velDot < 0) {
                if (linkLength > linkMaxLength + GRAPPLE_TENSION_THRESHOLD)
                    player.addVelocity (- playerVelPart.x, - playerVelPart.y, - playerVelPart.z);

                restrainment = defaultRestrainment;
            } else
                if (playerVelPart.length () < defaultRestrainment)
                    restrainment = defaultRestrainment - playerVelPart.length ();
        }

        Vec3d grappleRestrain = linkVec.normalize ().multiply (restrainment);

        player.addVelocity (grappleRestrain.x, grappleRestrain.y, grappleRestrain.z);
    }
}
