package org.alixia.chatroom.api.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.impl.DevelopmentEnvironmentException;

public class JarData {

	private final File jarFile;

	/**
	 * Creates a {@link JarData} based off of the Jar file that this program is
	 * running off of, if it can be obtained. If the program is running in a
	 * development environment (or the jar file simply cannot be found) then this
	 * method throws a {@link DevelopmentEnvironmentException}.
	 * 
	 * @throws DevelopmentEnvironmentException
	 *             Incase the program is in a development environment or the
	 *             program's jar file cannot be found for any other reason.
	 */
	public JarData() throws DevelopmentEnvironmentException {
		this(getCurrentJarFile());
	}

	public static File getCurrentJarFile() throws DevelopmentEnvironmentException {
		if (ChatRoom.isDevelopmentEnvironment())
			throw new DevelopmentEnvironmentException();
		return new File(JarData.class.getProtectionDomain().getCodeSource().getLocation().getFile());
	}

	public JarData(File jarFile) {
		this(jarFile, true);
	}

	/**
	 * Creates a {@link JarData} object off of the specified jar file.
	 * 
	 * @param jarFile
	 *            The jar file to create this object off of.
	 * @param read
	 *            Whether or not the contents of the jar file should be loaded into
	 *            this JarData right now (during construction) or when data is
	 *            requested (when {@link #getEntries()} or {@link #getManifest()}
	 *            are called). If this is <code>true</code>, this constructor may
	 *            throw a {@link RuntimeException}, since reading in the jar file
	 *            can undergo problems. See below.
	 * @throws RuntimeException
	 *             Incase <code>read</code> is <code>true</code> and an
	 *             {@link IOException} occurs while reading in the jar file.
	 * 
	 */
	public JarData(File jarFile, boolean read) throws RuntimeException {
		this.jarFile = jarFile;
		if (read)
			try {
				readFile();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
	}

	/**
	 * The {@link Manifest} of this {@link JarData}'s jar file. Manifests are a part
	 * of jar files. They tell Java which class the program should start from and
	 * more.
	 */
	private Manifest manifest;
	/**
	 * <p>
	 * Each {@link JarEntry} and its data, in this jar data's jar file. A
	 * {@link JarEntry} is a file inside the jar file.
	 * <p>
	 * Normally, when obtaining {@link JarEntry JarEntries} from a jar file, folders
	 * are also read in as entries. This map, however, only contains files, where
	 * the name of an entry that represents a nested file contains a slash to
	 * separate the parent folder from the file.
	 * <p>
	 * E.g.
	 * 
	 * <pre>
	 * <code>folder/File.txt</code>
	 * </pre>
	 * 
	 * or
	 * 
	 * <pre>
	 * <code>resources/graphics/icons/cr.png</code>
	 * </pre>
	 */
	private Map<JarEntry, List<Integer>> entries;

	public Manifest getManifest() throws IOException {
		if (manifest == null)
			readFile();

		return manifest;
	}

	private void readFile() throws IOException {
		try (JarInputStream jis = new JarInputStream(new FileInputStream(jarFile))) {
			if (manifest == null) {
				manifest = jis.getManifest();
			}
			if (entries == null) {
				Map<JarEntry, List<Integer>> entries = new HashMap<>();
				JarEntry entry;
				while ((entry = jis.getNextJarEntry()) != null) {
					if (entry.isDirectory())
						continue;
					List<Integer> bytes = new LinkedList<>();
					int data;
					while ((data = jis.read()) != -1)
						bytes.add(data);
					entries.put(entry, bytes);
					jis.closeEntry();
				}
				this.entries = entries;
			}
		}
	}

	public Map<JarEntry, List<Integer>> getEntries() throws IOException {
		if (entries == null)
			readFile();
		return entries;
	}

	/**
	 * Creates a {@link JarData} based off of the current jar file.
	 * 
	 * @return <code>new </code>{@link #JarData()}<code>;</code>
	 * @throws DevelopmentEnvironmentException
	 *             Incase the program is in a development environment or the jar
	 *             file for this program could not be located for any other reason.
	 */
	public static JarData current() throws DevelopmentEnvironmentException {
		return new JarData();
	}

}
