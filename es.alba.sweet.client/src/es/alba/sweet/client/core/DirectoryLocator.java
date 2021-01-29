package es.alba.sweet.client.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class DirectoryLocator {

	public static Path findResetLayoutFile(Path path) {
		URL url = findDirectory(path.getParent());

		try {
			File configFile = new File(FileLocator.toFileURL(url).getPath());
			Path filename = path.getFileName();
			return Paths.get(configFile.getAbsolutePath(), filename.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static URL findDirectory(Path path) {
		Bundle bundle = FrameworkUtil.getBundle(DirectoryLocator.class);
		return FileLocator.find(bundle, new org.eclipse.core.runtime.Path(path.toString()), null);
	}

	public static Path findPath(String path) {
		URL url = findDirectory(Paths.get(path));

		try {
			File file = new File(FileLocator.toFileURL(url).getPath());
			return Paths.get(file.getAbsolutePath());

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
