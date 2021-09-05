package de.pfannekuchen.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

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
		if (out.exists()) {
			Files.walk(out.toPath()).forEach(c -> {
				c.toFile().delete();
			});
			out.delete();
		}
		JsonDownloader.downloadDeps(out, new URL("https://launchermeta.mojang.com/v1/packages/0a5a761091458d69e3dea629a018eff7d74eb534/1.8.9.json"));
	}
	
}
