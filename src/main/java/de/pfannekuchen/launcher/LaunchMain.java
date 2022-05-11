package de.pfannekuchen.launcher;

import java.io.File;
import java.net.URL;

import com.google.gson.Gson;

import de.pfannekuchen.launcher.json.VersionJson;
import de.pfannekuchen.launcher.launcher.MinecraftLauncher;

/**
 * Defines the entry point for testing purposes
 * @author Pancake
 */
public class LaunchMain {

	public static final Gson gson = new Gson();
	
	/**
	 * Entry point for testing with given file
	 * @param args Unused parameters
	 * @throws Exception Throws an Exception whenever something failed
	 */
	public static void main(String[] args) throws Exception {
		File out = new File(".out");
		if (out.exists()) Utils.deleteDirectory(out);
		VersionJson in = gson.fromJson(Utils.readAllBytesAsStringFromURL(new URL("https://launchermeta.mojang.com/v1/packages/20116297638f7c70cd046e25a6ac90fee4cae61a/1.6.json")), VersionJson.class);
		
		JsonDownloader.downloadDeps(out, in);
		MinecraftLauncher.launch(out, new File(out, "natives"), new File(out, "libraries"), new File(out, ".minecraft"), in, true, "Pfannekuchen", new File(out, "assets").getAbsolutePath(), "uuidLuL", "accesme", "1.12.2");
	}
	
}
