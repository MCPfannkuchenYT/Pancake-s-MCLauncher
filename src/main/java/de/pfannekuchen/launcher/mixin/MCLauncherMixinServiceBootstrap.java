package de.pfannekuchen.launcher.mixin;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class MCLauncherMixinServiceBootstrap implements IMixinServiceBootstrap {

	@Override
	public String getName() {
		return "MCLauncher";
	}

	@Override
	public String getServiceClassName() {
		return "de.pfannekuchen.launcher.mixin.MCLauncherMixinService";
	}

	@Override
	public void bootstrap() {
		
	}

}
