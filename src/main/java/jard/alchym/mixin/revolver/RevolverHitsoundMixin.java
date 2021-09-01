package jard.alchym.mixin.revolver;

import jard.alchym.AlchymReference;
import jard.alchym.items.RevolverItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 *  RevolverHitsoundMixin
 *  TODO: Add a description for this file.
 *
 *  Created by jard at 6:01 PM on October 08, 2019.
 ***/

@Mixin (LivingEntity.class)
public abstract class RevolverHitsoundMixin extends Entity {
    public RevolverHitsoundMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(method = "applyDamage", at = @At ("HEAD"))
    protected void applyDamage (DamageSource source, float amount, CallbackInfo info) {
        if (source.getAttacker() != null && !this.isInvulnerableTo (source)) {
            Entity attacker = source.getAttacker ();
            if (attacker instanceof PlayerEntity && ((PlayerEntity) attacker).getMainHandStack ().getItem () instanceof RevolverItem) {
                int id = (int) Math.ceil ((1.f - (amount / 16.0)) * 4.f);
                id = (id > 4) ? 4 : (id < 1) ? 1 : id;
                String hitSound = "misc.hitsound." + id;

                SoundEvent event = new SoundEvent (new Identifier (AlchymReference.MODID, hitSound));
                ((PlayerEntity) attacker).playSound (event, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }
    }
}
