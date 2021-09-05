package de.pfannekuchen.launcher;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;

import de.pfannekuchen.launcher.Utils.Os;
import de.pfannekuchen.launcher.exceptions.ConnectionException;
import de.pfannekuchen.launcher.exceptions.ExtractionException;
import de.pfannekuchen.launcher.json.Library;
import de.pfannekuchen.launcher.json.NativesDownload;
import de.pfannekuchen.launcher.json.Rule;
import de.pfannekuchen.launcher.json.VersionJson;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/**
 * Downloads all dependencies from a given json file url
 * @author Pancake
 */
public class JsonDownloader {

	private static final Gson gson = new Gson();
	
	/**
	 * Downloads the dependencies into the folder
	 * @param out Output Folder for dependencies
	 * @param json Link to all dependencies
	 */
	public static void downloadDeps(File out, URL json, File jvmCache) {
		int time = (int) System.currentTimeMillis();
		out.mkdir();
		jvmCache.mkdir();
		File natives = new File(out, "natives");
		File libs = new File(out, "libraries");
		File runtime = new File(out, ".minecraft");
		runtime.mkdir();
		natives.mkdir();
		libs.mkdir();
		VersionJson in = gson.fromJson(Utils.readAllBytesAsStringFromURL(json), VersionJson.class);
		System.out.println(String.format("[JsonDownloader] Downloading Dependencies for Minecraft version %s", in.id));
		Os os = Utils.getOs();
		System.out.println(String.format("[JsonDownloader] Detected operating system: %s", os.name()));
		List<Library> dependencies = sortOutDependencies(in.libraries, os);
		System.out.println(String.format("[JsonDownloader] Fetching %d dependencies", dependencies.size()));
		try {
			for (Library library : dependencies) {
				if (library.downloads.artifact != null) {
					Files.copy(new URL(library.downloads.artifact.url).openStream(), new File(libs, library.downloads.artifact.path.replaceAll("/", "\\.")).toPath());
					System.out.println(String.format("[JsonDownloader] Downloading %s...", library.downloads.artifact.path.replaceAll("/", "\\.")));
				}
				if (library.downloads.classifiers != null) {
					NativesDownload nativesWin32 = library.downloads.classifiers.nativesWindows32; 	
					NativesDownload nativesWin64 = library.downloads.classifiers.nativesWindows64;
					NativesDownload nativesWin = library.downloads.classifiers.nativesWindows;
					NativesDownload nativesLinux = library.downloads.classifiers.nativesLinux;
					NativesDownload nativesOsx = library.downloads.classifiers.nativesOsx;
					switch (os) {
						case WIN64:
							if (nativesWin64 != null) {
								Files.copy(new URL(nativesWin64.url).openStream(), new File(natives, nativesWin64.path.replaceAll("/", "\\.")).toPath());
								unzipFileAndDelete(natives, nativesWin64.path.replaceAll("/", "\\."), "natives");
							}
							if (nativesWin != null) {
								Files.copy(new URL(nativesWin.url).openStream(), new File(natives, nativesWin.path.replaceAll("/", "\\.")).toPath());
								unzipFileAndDelete(natives, nativesWin.path.replaceAll("/", "\\."), "natives");
							}
							break;
						case WIN32:
							if (nativesWin32 != null) {
								Files.copy(new URL(nativesWin32.url).openStream(), new File(natives, nativesWin32.path.replaceAll("/", "\\.")).toPath());
								unzipFileAndDelete(natives, nativesWin32.path.replaceAll("/", "\\."), "natives");
							}
							if (nativesWin != null) {
								Files.copy(new URL(nativesWin.url).openStream(), new File(natives, nativesWin.path.replaceAll("/", "\\.")).toPath());
								unzipFileAndDelete(natives, nativesWin.path.replaceAll("/", "\\."), "natives");
							}
							break;
						case LINUX:
							if (nativesLinux != null) {
								Files.copy(new URL(nativesLinux.url).openStream(), new File(natives, nativesLinux.path.replaceAll("/", "\\.")).toPath());
								unzipFileAndDelete(natives, nativesLinux.path.replaceAll("/", "\\."), "natives");
							}
							break;
						case OSX:
							if (nativesOsx != null) {
								Files.copy(new URL(nativesOsx.url).openStream(), new File(natives, nativesOsx.path.replaceAll("/", "\\.")).toPath());
								unzipFileAndDelete(natives, nativesOsx.path.replaceAll("/", "\\."), "natives");
							}
							break;
					}
				}
			}
		} catch (Exception e) {
			Utils.deleteDirectory(out);
			throw new ConnectionException("Error downloading dependencies", e);
		}
		try {
			System.out.println(String.format("[JsonDownloader] Downloading Client..."));
			Files.copy(new URL(in.downloads.client.url).openStream(), new File(natives, "client.jar").toPath());
		} catch (Exception e) {
			Utils.deleteDirectory(out);
			throw new ConnectionException("Error downloading client", e);
		}
		File jvm = new File(jvmCache, "adoptopenjdk-" + in.javaVersion.majorVersion);
		try {
			if (!jvm.exists()) {
				jvm.mkdir();
				System.out.println(String.format("[JsonDownloader] Downloading JVM..."));
				String url = "https://mgnet.work/jvm/AdoptOpenJDK-Java" + in.javaVersion.majorVersion + "-";
				if (os == Os.LINUX) url += "Linux.zip";
				else if (os == Os.OSX) url += "OSX.zip";
				else url += "Windows.zip";
				Files.copy(new URL(url).openStream(), new File(jvm, "jvm.zip").toPath());
				System.out.println(String.format("[JsonDownloader] Extracting JVM..."));
				unzipFileAndDelete(jvm, "jvm.zip", "jvm");
			}
		} catch (Exception e) {
			Utils.deleteDirectory(jvm);
			throw new ConnectionException("Error downloading JVM", e);
		}
		System.out.println(String.format(Locale.ENGLISH, "[JsonDownloader] All files successfully downloaded. Took %.2f seconds", (((int) System.currentTimeMillis()) - time) / 1000.0f));
	}

	private static void unzipFileAndDelete(File zipDir, String zipFile, String job) {
		ZipFile zip = new ZipFile(new File(zipDir, zipFile));
		System.out.println(String.format("[JsonDownloader] Extracting %s: %s", job, zipFile));
		try {
			for (FileHeader fileHeader : zip.getFileHeaders()) {
				if (fileHeader.getFileName().contains("META-INF")) continue;
				zip.extractFile(fileHeader, zipDir.getAbsolutePath());
				System.out.println(String.format("[JsonDownloader]     %s", fileHeader.getFileName()));
			}
			new File(zipDir, zipFile).delete();
		} catch (ZipException e) {
			throw new ExtractionException("Error extracting " + job + ": " + zipFile, e);
		}
	}

	private static List<Library> sortOutDependencies(List<Library> in, Os os) {
		// remove unwanted dependencies based on mojangs rule system
		DEPENDENCYLOOP: for (Library library : new ArrayList<>(in)) {
			if (library.rules != null) {
				// check rules
				boolean shouldBeAllowedByDefault = false;
				for (Rule rule : library.rules) {
					boolean action = "allow".equals(rule.action);
					if (rule.os == null) {
						shouldBeAllowedByDefault = action;
						continue;
					}
					if ("windows".equals(rule.os.name) && (os == Os.WIN32 || os == Os.WIN64)) {
						if (!action) in.remove(library);
						continue DEPENDENCYLOOP;
					} else if ("osx".equals(rule.os.name) && os == Os.OSX) {
						if (!action) in.remove(library);
						continue DEPENDENCYLOOP;
					} else if ("linux".equals(rule.os.name) && os == Os.LINUX) {
						if (!action) in.remove(library);
						continue DEPENDENCYLOOP;
					}
				}
				if (!shouldBeAllowedByDefault) in.remove(library);
			}	
		}
		return in;
	}
	
}
