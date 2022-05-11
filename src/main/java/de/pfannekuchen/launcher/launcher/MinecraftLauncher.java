package de.pfannekuchen.launcher.launcher;

import java.io.File;
import java.util.Arrays;

import de.pfannekuchen.launcher.json.VersionJson;

/**
 * Launches a minecraft instance
 * @author Pancake
 */
public class MinecraftLauncher {
	
	/**
	 * Launches a minecraft instance
	 * @param bin Client binary folder
	 * @param natives Natives folder
	 * @param libraries Libraries Folder
	 * @param minecraft .minecraft folder
	 * @param json Version Json for the minecraft instance
	 * @param waitFor Whether it should wait for the process to end
	 * @throws Exception Zip Error
	 */
	public static void launch(File bin, File natives, File libraries, File minecraft, VersionJson json, boolean waitFor, String username, String assetsDir, String uuid, String accessToken, String version) throws Exception {
		String[] args = json.minecraftArguments.replaceAll("\\$\\{auth_player_name\\}", username).replaceAll("\\$\\{version_name\\}", version).replaceAll("\\$\\{version_type\\}", "mclauncher").replaceAll("\\$\\{version\\}", json.id).replaceAll("\\$\\{game_directory\\}", "\"" + minecraft.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\").replace("\"", "") + "\"").replaceAll("\\$\\{assets_root\\}", "\"" + assetsDir.replaceAll("\\\\", "\\\\\\\\") + "\"").replace("\"", "").replaceAll("\\$\\{assets_index_name\\}", json.assetIndex.id).replaceAll("\\$\\{auth_uuid\\}", uuid).replaceAll("\\$\\{auth_access_token\\}", accessToken).replaceAll("\\$\\{user_properties\\}", "").replaceAll("\\$\\{user_type\\}", "mojang").split(" ");

		System.setProperty("java.library.path", natives.getAbsolutePath());
		System.setProperty("org.lwjgl.librarypath", natives.getAbsolutePath());
		
		ZipClassLoader loader = new ZipClassLoader(new File(bin, "client.jar"), libraries.listFiles());
		loader.preloadClasses();

		System.out.println(String.format("[MinecraftLauncher] Launching via arguments: %s", Arrays.toString(args)));
		loader.loadClass(json.mainClass).getDeclaredMethod("main", String[].class).invoke(null, new Object[] {args});
	}
	
}
