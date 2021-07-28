package jard.alchym.init;

import jard.alchym.AlchymReference;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

public class InitSounds extends InitAbstract <SoundEvent> {
    InitSounds (InitAlchym alchym) {
        super (Registry.SOUND_EVENT, alchym);
    }

    public final SoundEvent revolverFire = new SoundEvent (AlchymReference.Sounds.REVOLVER_FIRE.location);
    public final SoundEvent hitSound1 = new SoundEvent (AlchymReference.Sounds.HITSOUND_1.location);
    public final SoundEvent hitSound2 = new SoundEvent (AlchymReference.Sounds.HITSOUND_2.location);
    public final SoundEvent hitSound3 = new SoundEvent (AlchymReference.Sounds.HITSOUND_3.location);
    public final SoundEvent hitSound4 = new SoundEvent (AlchymReference.Sounds.HITSOUND_4.location);

    public final SoundEvent dryTransmute = new SoundEvent (AlchymReference.Sounds.TRANSMUTE_DRY.location);
    public final SoundEvent transmuteFumes = new SoundEvent (AlchymReference.Sounds.TRANSMUTE_FUMES.location);

    @Override
    public void initialize() {
        register (AlchymReference.Sounds.REVOLVER_FIRE.getRegistryId (), revolverFire);
        register (AlchymReference.Sounds.HITSOUND_1.getRegistryId (), hitSound1);
        register (AlchymReference.Sounds.HITSOUND_2.getRegistryId (), hitSound2);
        register (AlchymReference.Sounds.HITSOUND_3.getRegistryId (), hitSound3);
        register (AlchymReference.Sounds.HITSOUND_4.getRegistryId (), hitSound4);

        register (AlchymReference.Sounds.TRANSMUTE_DRY.getRegistryId (), dryTransmute);
        register (AlchymReference.Sounds.TRANSMUTE_FUMES.getRegistryId (), transmuteFumes);
    }
}
