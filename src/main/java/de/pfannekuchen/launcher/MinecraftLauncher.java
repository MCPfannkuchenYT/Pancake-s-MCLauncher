package de.pfannekuchen.launcher;

import java.io.File;

import de.pfannekuchen.launcher.Utils.Os;
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
	 * @param jvmCache Cache for all JVMs
	 * @param json Version Json for the minecraft instance
	 * @param waitFor Whether it should wait for the process to end
	 */
	public static void launch(File bin, File natives, File libraries, File minecraft, File jvmCache, VersionJson json, boolean waitFor, String username, String assetsDir, String uuid, String accessToken) {
		Os os = Utils.getOs();
		String commandLine = String.format("\"%s\" -cp \"%s\";\"%s\";\"%s\" -Djava.library.path=\"%s\" %s %s", 
				new File(jvmCache, "adoptopenjdk-" + json.javaVersion.majorVersion + "/bin/java" + ((os == Os.WIN32 || os == Os.WIN64) ? ".exe" : "")).getAbsolutePath(),
				new File(bin, "client.jar").getAbsolutePath(),
				natives.getAbsolutePath() + ((os == Os.WIN32 || os == Os.WIN64) ? "\\*" : "/*"),
				libraries.getAbsolutePath() + ((os == Os.WIN32 || os == Os.WIN64) ? "\\*" : "/*"),
				natives.getAbsolutePath(),
				json.mainClass,
				json.minecraftArguments.replaceAll("\\$\\{auth_player_name\\}", username).replaceAll("\\$\\{version\\}", json.id).replaceAll("\\$\\{game_directory\\}", "\"" + minecraft.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\") + "\"").replaceAll("\\$\\{assets_root\\}", "\"" + assetsDir.replaceAll("\\\\", "\\\\\\\\") + "\"").replaceAll("\\$\\{assets_index_name\\}", json.assetIndex.id).replaceAll("\\$\\{auth_uuid\\}", uuid).replaceAll("\\$\\{auth_access_token\\}", accessToken).replaceAll("\\$\\{user_properties\\}", "").replaceAll("\\$\\{user_type\\}", "mojang"));
		System.out.println(String.format("[MinecraftLauncher] Launching via console arguments: %s", commandLine));
		ProcessBuilder builder = new ProcessBuilder(commandLine.split(" "));
		builder.directory(minecraft);
		builder.inheritIO();
		try {
			Process p = builder.start();
			if (waitFor) p.waitFor();
		} catch (Exception e) {
			throw new RuntimeException("Couldn't run process: " + commandLine, e);
		}
	}
	
}
