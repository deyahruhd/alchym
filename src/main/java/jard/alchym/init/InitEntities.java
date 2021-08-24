package jard.alchym.init;

import jard.alchym.AlchymReference;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;

/***
 *  InitEntities
 *  The initializing module that initializes every {@link net.minecraft.entity.Entity} in the mod.
 *
 *  Created by jard at 02:30 on August 23, 2021.
 ***/
public class InitEntities extends InitAbstract <EntityType <?>> {
    InitEntities (InitAlchym alchym) {
        super (Registry.ENTITY_TYPE, alchym);
    }

    public final EntityType revolverBullet = from (RevolverBulletEntity::new, null);

    @Override
    public void initialize () {
        register (AlchymReference.Entities.REVOLVER_BULLET.getName (), revolverBullet);
    }

    private EntityType from (EntityType.EntityFactory<? extends Entity> entity, SpawnGroup spawnGroup) {
        return FabricEntityTypeBuilder.create (spawnGroup, entity).dimensions (EntityDimensions.fixed (0.0f, 0.0f)).build ();
    }
}
