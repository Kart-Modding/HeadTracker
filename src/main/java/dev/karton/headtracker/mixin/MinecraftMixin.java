package dev.karton.headtracker.mixin;

import dev.karton.headtracker.PetTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A plain client tick hook, so the pet tracker doesn't need a loader-specific event.
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void headtracker$tickPetTracker(CallbackInfo ci) {
        PetTracker.tick((Minecraft) (Object) this);
    }
}
