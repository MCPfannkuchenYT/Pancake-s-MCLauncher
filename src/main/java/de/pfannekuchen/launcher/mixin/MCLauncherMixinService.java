package de.pfannekuchen.launcher.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment.CompatibilityLevel;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.service.MixinServiceAbstract;
import org.spongepowered.asm.transformers.MixinClassReader;

import de.pfannekuchen.launcher.launcher.ZipClassLoader;

public class MCLauncherMixinService extends MixinServiceAbstract implements IClassProvider, IClassBytecodeProvider {
	
	@Override 
	public String getName() { 
		return "MCLauncher"; 
	}
	
	@Override 
	public CompatibilityLevel getMinCompatibilityLevel() { 
		return CompatibilityLevel.JAVA_8; 
	}
	
	@Override 
	public CompatibilityLevel getMaxCompatibilityLevel() { 
		return CompatibilityLevel.JAVA_17; 
	}
	
	@Override
	public Collection<String> getPlatformAgents() { 
		return Collections.emptyList(); 
	}
	
	@Override 
	public boolean isValid() {
		return true; 
	}
	
	@Override 
	public InputStream getResourceAsStream(String name) { 
		return ZipClassLoader.instance.getResourceAsStream(name); 
	}
	
	@Override
	public IClassProvider getClassProvider() {
		return this;
	}
	
	@Override
	public IClassBytecodeProvider getBytecodeProvider() {
		return this;
	}
	
	@Override
	public IContainerHandle getPrimaryContainer() {
		return new ContainerHandleVirtual(this.getName());
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		return Class.forName(name, true, ZipClassLoader.instance);
	}
	
	@Override
	public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
		return Class.forName(name, initialize, ZipClassLoader.instance);
	}
	
	@Override
	public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
		return Class.forName(name, initialize, ZipClassLoader.instance);
	}
	
	@Override
	public ITransformerProvider getTransformerProvider() {
		return null;
	}
	
	@Override
	public IClassTracker getClassTracker() {
		return null;
	}
	
	@Override
	public IMixinAuditTrail getAuditTrail() {
		return null;
	}
	
	@Override
	public URL[] getClassPath() { 
		return null; 
	}
	
	@Override
	public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new MixinClassReader(ZipClassLoader.getBytes(name), name);
        classReader.accept(classNode, 0);
        return classNode;
	}
	
	@Override
	public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
		return this.getClassNode(name);
	}
	
	@Override
	public void offer(IMixinInternal internal) {
		if (internal instanceof IMixinTransformerFactory) {
			ZipClassLoader.mixinTransformer = ((IMixinTransformerFactory) internal).createTransformer();
		}
		super.offer(internal);
	}
	
}
