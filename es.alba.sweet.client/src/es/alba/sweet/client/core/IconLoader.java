package es.alba.sweet.client.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;

import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.core.constant.Directory;

public class IconLoader {

	public final static String UNKNOWN_ICON_FILE = "unknown.png";

	public static ImageDescriptor load(String filename) {
		URL url = DirectoryLocator.findDirectory(Paths.get(Directory.ICONS));
		return load(url, filename);
	}

	public static ImageDescriptor loadFromURI(String iconURI) {
		try {
			URL url = new URL(iconURI);
			Path path = Paths.get(new File(FileLocator.toFileURL(url).getPath()).getAbsolutePath());
			URL directoryURL = DirectoryLocator.findDirectory(path.getParent().getFileName());
			String filename = path.getFileName().toString();

			return load(directoryURL, filename);
		} catch (IOException e) {
			// e.printStackTrace();
			Output.DEBUG.error("es.alba.sweet.core.IconLoader.loadFromURI", "No file found in " + iconURI);
			return loadUnknownIcon(DirectoryLocator.findDirectory(Paths.get(Directory.ICONS)));
		}
	}

	private static ImageDescriptor load(URL directoryURL, String filename) {

		try {
			Path path = Paths.get(new File(FileLocator.toFileURL(directoryURL).getPath()).getAbsolutePath(), filename);
			if (Files.exists(path)) {
				return ImageDescriptor.createFromURL(path.toUri().toURL());
			}

			Output.DEBUG.warning("es.alba.sweet.core.IconLoader.load", "File " + filename + " not found. Loading the unknown icon file");
			return loadUnknownIcon(directoryURL);

		} catch (IOException e) {
			Output.DEBUG.error("es.alba.sweet.core.IconLoader.load", e.getMessage());
			e.printStackTrace();
		}

		return null;

	}

	private static ImageDescriptor loadUnknownIcon(URL directoryURL) {
		try {
			Path path = Paths.get(new File(FileLocator.toFileURL(directoryURL).getPath()).getAbsolutePath(), UNKNOWN_ICON_FILE);
			if (Files.exists(path)) {
				return ImageDescriptor.createFromURL(path.toUri().toURL());
			}
			Output.DEBUG.error("es.alba.sweet.core.IconLoader.load", "File " + UNKNOWN_ICON_FILE + " not found.");
			return null;

		} catch (IOException e) {
			Output.DEBUG.error("es.alba.sweet.core.IconLoader.loadUnknownIcon", e.getMessage());
			return null;
		}

	}
}
