package org.alixia.chatroom.impl.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Scanner;

import org.alixia.chatroom.impl.DevelopmentEnvironmentException;

public final class HomeDir {

	private static File homeDirectory;

	public static File getHomeDirectory() throws NoHomeDirectoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public static boolean hasLocalHomeDirectory() throws DevelopmentEnvironmentException {
		try {
			if (!new File(HomeDir.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).isFile())
				throw new DevelopmentEnvironmentException();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return HomeDir.class.getResourceAsStream("/local/data/install_dir.crf") != null;

	}

	public static void loadLocalHomeDirectory()
			throws LocalInstallDirectoryBuggedException, DirectoryCreationFailedException {
		InputStream localDirFileInput = HomeDir.class.getResourceAsStream("/local/data/install_dir.crf");
		if (localDirFileInput == null)
			return;
		else {
			Scanner scanner = new Scanner(localDirFileInput);
			String directory = scanner.nextLine();
			scanner.close();
			try {
				File installDir = new File(directory);
				if (!installDir.getName().equals("Chat Room"))
					installDir = new File(installDir, "Chat Room");
				installDir.mkdirs();

				if (!installDir.exists())
					throw new DirectoryCreationFailedException(installDir);

			} catch (Exception e) {
				throw new LocalInstallDirectoryBuggedException(e, directory);
			}
		}
	}

	public static void setupInstallDir() {
		if (homeDirectory == null)
			throw new RuntimeException();
		if (homeDirectory.exists() && !homeDirectory.isDirectory())
			homeDirectory.delete();
		homeDirectory.mkdirs();
	}

	/**
	 * <p>
	 * This will set a new installation directory for the program.
	 * <p>
	 * If the directory leads to a directory that ends in the name
	 * <code>Chat Room</code> then this program will install everything into that
	 * folder.
	 * <p>
	 * If the directory leads to a folder that does not end in the name
	 * <code>Chat Room</code>, this program will create a subfolder to the given
	 * directory named <code>Chat Room</code>. This subfolder is where this program
	 * will install everything. Also, {@link #getHomeDirectory()} will return a
	 * {@link File} representing the subfolder, not the parameter to this method.
	 * <p>
	 * This will rewrite the running jar file and cause issues with later class
	 * loading! The program should be restarted after this method is called, or
	 * {@link NoClassDefFoundError}s should be caught when calling methods off of a
	 * class that isn't yet initialized.
	 * 
	 * @param saveLocation
	 *            The new installation location.
	 * @throws NullPointerException
	 *             In case <code>saveLocation</code> is null. This is not allowed.
	 *             :)
	 */
	public static void setSaveLocation(File saveLocation) throws NullPointerException {
		Objects.requireNonNull(saveLocation);
		// TODO Rewrite this jar to include a file which contains the installation
		// directory.
		homeDirectory = saveLocation;
	}

}
