package com.unifina.serialization;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionTools {
	public static boolean notGeneratedByGroovy(Field field) {
		return !field.isSynthetic(); // e.g. Groovy's synthetic fields (metaClass, $staticClassInfo, $callSiteArray)
	}

	public static boolean notStatic(Field field) {
		return !Modifier.isStatic(field.getModifiers());
	}

	public static boolean notTransient(Field field) {
		return !Modifier.isTransient(field.getModifiers());
	}
}
