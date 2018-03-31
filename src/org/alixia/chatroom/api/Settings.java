package org.alixia.chatroom.api;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public final class Settings {

	public static final List<Field> saveAll() throws Exception {

		if (isSaveAvailable())
			throw new Exception("Saving settings is not available.");

		List<Field> failedFields = new LinkedList<>();
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(getSaveLocation()))) {

			Field[] data = Settings.class.getDeclaredFields();
			for (Field f : data)
				try {
					if (f.getClass().isPrimitive() && Modifier.isStatic(f.getModifiers())
							&& !DeclaredValueChecker.isOriginalValue(f.getName(), Settings.class))
						try {
							writer.println(f.getName() + "=" + f.get(null));
						} catch (IllegalArgumentException | IllegalAccessException e) {
							// This shouldn't get called.
							e.printStackTrace();
						}
				} catch (NoSuchFieldException | RuntimeException | IOException e) {
					failedFields.add(f);
				}
			return failedFields;

		} catch (FileNotFoundException e1) {
			// This shouldn't get called either.
			e1.printStackTrace();
			return null;
		}

	}

	private static String getSaveLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	private static boolean isSaveAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	private Settings() {
	}

}
