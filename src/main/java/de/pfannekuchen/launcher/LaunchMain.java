package de.pfannekuchen.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Defines the entry point for testing purposes
 * @author Pancake
 */
public class LaunchMain {

	/**
	 * Entry point for testing with given file
	 * @param args Unused parameters
	 * @throws IOException Throws an IO Exception whenever something failed
	 */
	public static void main(String[] args) throws IOException {
		File out = new File(".out");
		File jvmCache = new File(System.getProperty("user.home"), ".jvmcache");
		if (out.exists()) Utils.deleteDirectory(out);
		if (jvmCache.exists()) Utils.deleteDirectory(jvmCache);
		JsonDownloader.downloadDeps(out, new URL("https://launchermeta.mojang.com/v1/packages/0a5a761091458d69e3dea629a018eff7d74eb534/1.8.9.json"), jvmCache);
	}
	
}
