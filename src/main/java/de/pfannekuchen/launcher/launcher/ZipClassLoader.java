package de.pfannekuchen.launcher.launcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

/**
 * A custom class loader that loads and manipulates classes and resources from jars
 * @author Pancake
 */
public class ZipClassLoader extends ClassLoader {

	public static ZipClassLoader instance;
	public static IMixinTransformer mixinTransformer;
	
	private URL[] urls;
	private HashMap<String, URL> resources = new HashMap<>();
	private HashMap<String, byte[]> classes = new HashMap<>();
	
	/**
	 * Makes a new Class Loader
	 * @throws IOException Read only
	 */
	public ZipClassLoader(File client, File[] libs) throws IOException {
		super(ClassLoader.getSystemClassLoader());
		for (File zip : libs)
			loadZip(zip);
		loadZip(client);
		
		urls = new URL[1 + libs.length];
		for (int i = 0; i < libs.length; i++) {
			urls[i] = libs[i].toURI().toURL();
		}
		urls[urls.length-1] = client.toURI().toURL();

		instance = this;
	}

	/**
	 * Loads a zip into the resources map and load all classes
	 * @param zip Zip File to read
	 * @throws IOException Read only
	 */
	private void loadZip(File zip) throws IOException {
		ZipInputStream stream = new ZipInputStream(new FileInputStream(zip));
		ZipEntry e;
		byte[] buffer = new byte[1024];
		int length;
		while ((e = stream.getNextEntry()) != null) {
			if (!e.isDirectory()) {
				if (e.getName().endsWith(".class")) {
					// Read and load class file
					ByteArrayOutputStream o = new ByteArrayOutputStream();
					while ((length = stream.read(buffer)) != -1) {
						o.write(buffer, 0, length);
					}
					o.close();
					byte[] file = o.toByteArray();
					this.classes.put(e.getName().replace('/', '.').replace(".class", ""), file);
				} else {
					// Add URL to resources map
					this.resources.put(e.getName(), new URL("jar:file:/" + zip.getAbsolutePath().replace('\\', '/') + "!/" + e.getName().replace('\\', '/')));
				}
			}
		}
		stream.close();
	}
	
	/**
	 * Loads all classes
	 * @throws ClassNotFoundException 
	 */
	public void preloadClasses() throws ClassNotFoundException {
		System.out.println("[MinecraftLauncher] Preloading " + classes.size() + " classes");
		for (String clazz : new ArrayList<>(classes.keySet()))
			try { loadClass(clazz); } catch (Throwable e) { }
	}
	
	/**
	 * Returns a classes bytes
	 */
	public static byte[] getBytes(String name) {
		return instance.classes.get(name);
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
		try {
			URL resource = this.getResource(name);
			return resource == null ? super.getResourceAsStream(name) : resource.openStream();
		} catch (IOException e) {
			return super.getResourceAsStream(name);
		}
	}
	
	@Override
	public URL getResource(String name) {
		if (this.resources.containsKey(name))
			return this.resources.get(name); 
		return super.getResource(name);
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		if (this.resources.containsKey(name))
			return Collections.enumeration(Arrays.asList(this.resources.get(name)));
		return super.getResources(name);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (this.classes.containsKey(name)) {
			byte[] clazz = this.classes.remove(name);
			
			// mixin processor
			clazz = ZipClassLoader.mixinTransformer.transformClass(MixinEnvironment.getCurrentEnvironment(), name, clazz);
			
			this.defineClass(name, clazz, 0, clazz.length);
		}
		return super.loadClass(name);
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		return super.findClass(name);
	}
	
}
