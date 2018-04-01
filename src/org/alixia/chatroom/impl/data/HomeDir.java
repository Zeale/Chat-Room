package org.alixia.chatroom.impl.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.data.JarData;
import org.alixia.chatroom.impl.DevelopmentEnvironmentException;

public final class HomeDir {

	private static File homeDirectory;

	private static final String INSTALL_LOCATION_FILE_PATH = "local/data/install.crd";

	public static void assertNonDevEnv() throws DevelopmentEnvironmentException {
		if (ChatRoom.isDevelopmentEnvironment())
			throw new DevelopmentEnvironmentException();
	}

	public static File getHomeDir() throws NoHomeDirectoryException {
		if (homeDirectory == null)
			throw new NoHomeDirectoryException();
		return homeDirectory;
	}

	public static InputStream getInstallLocInput() throws FileNotFoundException {
		return ChatRoom.isDevelopmentEnvironment()
				? new FileInputStream(new File(JarData.getRuntimeLocation(), INSTALL_LOCATION_FILE_PATH))
				: HomeDir.class.getResourceAsStream(INSTALL_LOCATION_FILE_PATH);
	}

	public static boolean hasLocalHomeDirectory() {
		// assertNonDevEnv();
		return ChatRoom.isDevelopmentEnvironment()
				? new File(JarData.getRuntimeLocation(), INSTALL_LOCATION_FILE_PATH).isFile()
				: HomeDir.class.getResourceAsStream(INSTALL_LOCATION_FILE_PATH) != null;

	}

	public static boolean isHomeDirSet() {
		return homeDirectory != null;
	}

	/**
	 * This method will attempt to set up the install directory that it reads from
	 * {@link #getInstallLocInput()}. It will also set {@value #homeDirectory}.
	 *
	 * @throws LocalInstallDirectoryBuggedException
	 *             If reading the install directory failed.
	 * @throws DirectoryCreationFailedException
	 *             If creating the install directory failed.
	 */
	public static void loadLocalHomeDirectory()
			throws LocalInstallDirectoryBuggedException, DirectoryCreationFailedException {

		InputStream localDirFileInput;
		try {
			localDirFileInput = getInstallLocInput();
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
		final Scanner scanner = new Scanner(localDirFileInput);
		final String directory = scanner.nextLine();
		scanner.close();
		try {
			File installDir = new File(directory);
			if (!installDir.getName().equals("Chat Room"))
				installDir = new File(installDir, "Chat Room");
			installDir.mkdirs();

			if (!installDir.exists())
				throw new DirectoryCreationFailedException(installDir);

			homeDirectory = installDir;

		} catch (final Exception e) {
			throw new LocalInstallDirectoryBuggedException(e, directory);
		}
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
	 * will install everything. Also, {@link #getHomeDir()} will return a
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
	 * @throws IOException
	 *             In case an {@link IOException} occurs while attempting to read
	 *             all the data from the program's current jar file.
	 */
	public static void setSaveLocation(final File saveLocation)
			throws NullPointerException, RuntimeException, IOException, FileNotFoundException {
		Objects.requireNonNull(saveLocation);

		try {
			if (!saveLocation.isDirectory())
				saveLocation.delete();
			saveLocation.mkdirs();
			if (!saveLocation.exists())
				throw new RuntimeException("Failed to make the installation directory.");
		} catch (final Exception e) {
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		}

		// So, the above code *should* throw an exception if the save location is not
		// viable. Now let's edit the currently running jar file to add in the save
		// location. This way, the next time the program is started, the file will be
		// checked (as a program resource) and an installation directory can be read
		// from. This way, the program doesn't have to find a viable place to store the
		// location of the user's selected installation directory; it can just store the
		// location inside itself.
		//
		// I hope this works on other platforms...

		final File runtimeLocation = JarData.getRuntimeLocation();

		JarData data;
		try {
			data = JarData.current(true);
		} catch (final DevelopmentEnvironmentException e) {
			final File file = JarData.getRuntimeLocation();
			final File installLoc = new File(file, INSTALL_LOCATION_FILE_PATH);
			installLoc.mkdirs();
			installLoc.createNewFile();
			try (PrintWriter writer = new PrintWriter(new FileOutputStream(installLoc))) {
				writer.println(saveLocation.getAbsolutePath());
			}
			return;
		}
		try (JarOutputStream rawStream = new JarOutputStream(new FileOutputStream(runtimeLocation));
				PrintWriter writer = new PrintWriter(rawStream)) {

			// We are not in a dev env.
			final Map<JarEntry, List<Integer>> entries = data.getEntries();

			// Write the current vals
			for (final Entry<JarEntry, List<Integer>> e : entries.entrySet()) {
				e.getKey().setTime(System.currentTimeMillis());
				rawStream.putNextEntry(e.getKey());
				for (final int i : e.getValue())
					rawStream.write(i);
			}

			rawStream.putNextEntry(new JarEntry(INSTALL_LOCATION_FILE_PATH));
			writer.println(saveLocation.getAbsolutePath());
			// The try statement will close the outputs.

		}

		homeDirectory = saveLocation;
	}

	/**
	 * Adds some stuff so that this program will recognize that it has already
	 * installed itself to the directory.
	 */
	public static void setupInstallDir() {
		if (homeDirectory == null)
			throw new RuntimeException();
		if (homeDirectory.exists() && !homeDirectory.isDirectory())
			homeDirectory.delete();
		homeDirectory.mkdirs();
	}

}
