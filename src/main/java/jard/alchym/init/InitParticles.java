package jard.alchym.init;

import jard.alchym.AlchymReference;
import jard.alchym.client.particle.FireTrailParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/***
 *  InitParticles
 *  The initializing module that initializes every particle in the mod.
 *
 *  Created by jard at 03:03 on September 10, 2021.
 ***/
public class InitParticles {
    public final DefaultParticleType fireTrail = FabricParticleTypes.simple ();

    protected final InitAlchym alchym;

    InitParticles (InitAlchym alchym) {
        this.alchym = alchym;
    }

    public void initialize () {
        register (AlchymReference.Particles.FIRE_ROCKET_TRAIL.getName (), fireTrail, FireTrailParticle.DefaultFactory::new);
    }

    private void register (String id, DefaultParticleType particle, ParticleFactoryRegistry.PendingParticleFactory <DefaultParticleType> factory) {
        Registry.register (Registry.PARTICLE_TYPE, new Identifier (AlchymReference.MODID, id), particle);
        ParticleFactoryRegistry.getInstance ().register (particle, factory);
    }
}
