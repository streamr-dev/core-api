package com.unifina.serialization;

import java.lang.reflect.Field;
import java.util.*;

import static com.unifina.serialization.ReflectionTools.notGeneratedByGroovy;
import static com.unifina.serialization.ReflectionTools.notStatic;

/**
 * Used to determine whether, and to pinpoint where, a subclass hides/shadows a field of any of its parent classes by
 * declaring a field with the same name as a field of one its parents.
 */
public class HiddenFieldDetector {

	private Map<String, List<Class>> hiddenFields;

	public HiddenFieldDetector(Class clazz) {
		Map<String, List<Class>> classesByFields = collectFieldNamesThroughInheritanceChain(clazz);
		hiddenFields = keepEntriesWithMoreThanOneValue(classesByFields);
	}

	public boolean anyHiddenFields() {
		return !hiddenFields().isEmpty();
	}

	public Map<String, List<Class>> hiddenFields() {
		return Collections.unmodifiableMap(hiddenFields);
	}

	private static Map<String, List<Class>> collectFieldNamesThroughInheritanceChain(Class<?> clazz) {
		Map<String, List<Class>> classesByFieldName = new HashMap<>();

		while (!clazz.equals(Object.class)) {
			for (String fieldName : namesOfImmediateFields(clazz)) {
				if (!classesByFieldName.containsKey(fieldName)) {
					classesByFieldName.put(fieldName, new ArrayList<Class>());
				}
				classesByFieldName.get(fieldName).add(clazz);
			}

			clazz = clazz.getSuperclass();
		}

		return classesByFieldName;
	}

	private static Map<String, List<Class>> keepEntriesWithMoreThanOneValue(Map<String, List<Class>> classesByFields) {
		Map<String, List<Class>> result = new HashMap<>();

		for (Map.Entry<String, List<Class>> entry : classesByFields.entrySet()) {
			if (entry.getValue().size() > 1) {
				result.put(entry.getKey(), entry.getValue());
			}
		}

		return result;
	}

	private static Iterable<String> namesOfImmediateFields(Class<?> clazz) {
		Set<String> fieldNames = new HashSet<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (notGeneratedByGroovy(field) && notStatic(field)) {
				fieldNames.add(field.getName());
			}
		}
		return fieldNames;
	}
}
