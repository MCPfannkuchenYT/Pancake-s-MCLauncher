package de.pfannekuchen.launcher.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "amj")
public class MixinTest {

	@Overwrite
	public void string() {
		
	}
	
}
