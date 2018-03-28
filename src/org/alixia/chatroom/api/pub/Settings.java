package org.alixia.chatroom.api.pub;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

public final class Settings {

	private static File saveLocation;

	public static void setSaveLocation(File saveLocation) throws FileNotFoundException {
		Objects.requireNonNull(saveLocation);
		if (!saveLocation.exists())
			throw new FileNotFoundException();
		if (!saveLocation.isDirectory())
			throw new RuntimeException("The given file path leads to a file and not a directory.");
		Settings.saveLocation = saveLocation;
	}

	public static boolean isSavingAvailable() {
		return saveLocation != null;
	}

	public static final void saveAll() {
		Field[] data = Settings.class.getDeclaredFields();
		for (Field f : data) {
			save(f);
		}
	}

	private static final void save(Field f) {

	}

	public static final void save(String settingName) throws NoSuchFieldException, SecurityException {
		save(Settings.class.getDeclaredField(settingName));
	}

	private Settings() {

	}

}
