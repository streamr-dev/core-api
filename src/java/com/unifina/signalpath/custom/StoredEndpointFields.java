package com.unifina.signalpath.custom;

import com.unifina.signalpath.Endpoint;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores and clears the <code>Endpoint</code> fields of an Object. Used by
 * <code>AbstractJavaCodeWrapper</code> to serialize fields of <code>AbstractCustomModule</code>.
 */
public class StoredEndpointFields implements Serializable {
	private Map<String, Endpoint> endpointsByFieldName;

	private StoredEndpointFields(Map<String, Endpoint> endpointsByFieldName) {
		this.endpointsByFieldName = endpointsByFieldName;
	}

	/**
	 * Collect and clear fields of type  <code>Endpoint</code>.
	 * @param instance whose values should be cleared and collected
	 * @return the holder of stored values
	 */
	public static StoredEndpointFields clearAndCollect(Object instance) {
		Map<String, Endpoint> endpointsByFieldName = new HashMap<>();

		for (Field f : fields(instance)) {
			if (isEndpoint(f)) {
				endpointsByFieldName.put(f.getName(), popField(instance, f));
			}
		}
		return new StoredEndpointFields(endpointsByFieldName);
	}

	/**
	 * Restore values of fields of type <code>Endpoint</code> on given object.
	 */
	public void restoreFields(Object instance) {
		for (Field f : fields(instance)) {
			if (isEndpoint(f)) {
				restoreField(instance, f, endpointsByFieldName.get(f.getName()));
			}
		}
	}

	private static Endpoint popField(Object instance, Field f) {
		f.setAccessible(true);
		try {
			Endpoint endpoint = (Endpoint) f.get(instance);
			f.set(instance, null);
			return endpoint;
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to pop field " + f + "for class " + instance.getClass(), e);
		} finally {
			f.setAccessible(false);
		}
	}

	private static void restoreField(Object instance, Field f, Object value) {
		f.setAccessible(true);
		try {
			f.set(instance, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to set field " + f + "for class " + instance.getClass(), e);
		} finally {
			f.setAccessible(false);
		}
	}

	private static Field[] fields(Object instance) {
		return instance.getClass().getDeclaredFields();
	}

	private static boolean isEndpoint(Field f) {
		return Endpoint.class.isAssignableFrom(f.getType());
	}
}