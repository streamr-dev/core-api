package com.unifina.serialization;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;

import static com.unifina.serialization.ReflectionTools.notStatic;
import static com.unifina.serialization.ReflectionTools.notTransient;

public class AnonymousInnerClassDetector {

	private static final Logger log = Logger.getLogger(AnonymousInnerClassDetector.class);

	public boolean detect(Object o) {
		for (Field field : fields(o)) {
			if (notTransient(field) && notStatic(field)) {
				try {
					field.setAccessible(true);
					Object instance = field.get(o);
					if (isAnonymousInnerClass(instance)) {
						return true;
					}
				} catch (IllegalAccessException e) {
					log.error(e);
				} finally {
					field.setAccessible(false);
				}
			}
		}
		return false;
	}

	private static Field[] fields(Object o) {
		return o.getClass().getDeclaredFields();
	}

	private static boolean isAnonymousInnerClass(Object instance) {
		return instance != null && instance.getClass().isAnonymousClass();
	}
}
