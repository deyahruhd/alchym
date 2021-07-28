## Quake-inspired movement mechanics

Alchym will feature movement mechanics inspired by those of Quake 1 and Quake III Arena. I am not aiming to perfectly 
replicate one set of movement mechanics from a specific game or engine, but rather wish to create something unique.

### Differential strafe

Alchym's movement is based on a system of niche bunny hop physics known as 'differential strafe': an implementation of
air physics which combines Q3A's strafe jumping mechanic and QW's bunny hopping mechanic. 

Q3A's air physics allow players to accelerate incredibly fast at the expense of air control, while QW's air physics
grants a significant degree of air control at the expense of acceleration. The two are normally irreconcilable without
game engine modification, because they use the exact same acceleration formula with different acceleration parameters.

Differential strafe automatically switches between these two physics based on the player's mouse turning rate, granting
them the ability to both accelerate with strafe jumping and turn in the air with bunny hop.

In technicality, Alchym's differential strafe physics are implemented as 'additive' strafe, where the effects
of QW bunny hop are applied on top of Q3A's acceleration, though the resulting physics are only marginally different.

### Strafe/bhop versus weapons

Furthermore, air strafe in Alchym will be nerfed to such an extent where achieving high speeds would be only attainable
with knockback weapons - think of rocket jumping, plasma boosting, and any other type of trickjumping that involve
weapon knockback.

This is done by using a soft cap where any acceleration above the walking speed is scaled down by a damping factor.
Thus, with perfect strafe and any amount of time and space, players can accelerate to any arbitrary speed with strafes
alone, but this would in practice be infeasible.

All of this is a consequence of a critical design decision in Alchym: the mod should easily communicate why players are
moving *fast*. That is, new players being exposed to the mod should immediately understand that usage of a rocket's
knockback or a plasma ground boost allow attaining fast sppeds, not by pure strafe alone.

This permits Alchym's movement mechanics to be easier for new movement players to grasp, while also giving experienced
players the opportunity to differentiate themselves in strafing skill.

### Knockback damage

Knockback weapons will deal a special type self-damage to the player which bypasses armor and Protection enchants. There
are several planned ways to mitigate this:

- Each limb of _**Automail**_ the player possesses decreases incoming self-damage by 12.5%. Four limbs gives a maximum
  self-damage reduction of 50%.
- Each level of _**Blast Protection**_ on armor decreases incoming self-damage by 1.5625% *additively*. Stacked with
  automail protection, full Blast Protection IV will increase self-damage reduction to 75%. The maximum cap of
  self-damage reduction is 80%, which is only achievable with full Blast Protection V or greater.
- The _**Blast Belt**_, a armor item for the leggings slot, will absorb the self-damage of projectiles based on the
  number of 'blast packs' the belt is equipped with (up to 5), and the rate of fire of the projectile in question. For
  example, if the player is wearing a Blast Belt with one 'blast pack', then they are allowed to absorb one rocket's
  worth of damage every 0.75 seconds. If they receive damage from a second rocket, such as from a 2x rocket stack,
  they will take the damage of the second rocket.

### Wall skimming

Alchym's physics will allow players to 'skim' past corners through the skill of wall skimming.

After landing on the ground, a small grace period is granted to the player where walls and other geometry shall not clip
the player's velocity upon collision, thus allowing them to carry their momentum around corners.

### Ground sliding

From a lore standpoint, air strafe in Alchym is achieved by the active transmutation of flowing air around the player,
accelerating them to greater speeds than normal. As a result, air strafe will passively consume a small amount of
energy, or 'alchymical charge', on each jump.

Both speed and alchymical charge can be conserved by sneaking on the ground while moving above walking speed. The player
will slide across the ground with a small amount of control and little ground friction, granting the ability to slide
under short hallways and spaces, preserve momentum across a surface, and time jumps for wall skimming as needed.
