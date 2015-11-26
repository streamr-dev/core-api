package com.unifina.signalpath.custom;

import com.unifina.signalpath.Endpoint;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores and clears the <code>Endpoint</code> fields of a <code>AbstractCustomModule</code>. Used by
 * <code>AbstractJavaCodeWrapper</code> to serialize fields of custom modules.
 */
public class StoredEndpointFields implements Serializable {

	private static final Logger log = Logger.getLogger(StoredEndpointFields.class);
	private Map<String, Endpoint> endpointsByFieldName = new HashMap<>();

	public void setValuesOn(AbstractCustomModule instance) {
		for (Field f : fields(instance)) {
			try {
				f.setAccessible(true);
				if (Endpoint.class.isAssignableFrom(f.getType())) {
					f.set(instance, endpointsByFieldName.get(f.getName()));
				}
			} catch (IllegalAccessException e) {
				log.error("Could not set field: " + f + ", class: " + instance.getClass() + " due to exception: " + e);
			} finally {
				f.setAccessible(false);
			}
		}
	}

	public static StoredEndpointFields clearAndCollect(AbstractCustomModule instance) {
		StoredEndpointFields ef = new StoredEndpointFields();
		for (Field f : fields(instance)) {
			ef.fetchAndClearField(instance, f);
		}
		return ef;
	}

	private void fetchAndClearField(AbstractCustomModule instance, Field f) {
		try {
            f.setAccessible(true); // avoid java.lang.IllegalAccessException and requires privileges
            Object obj = f.get(instance);
            if (obj instanceof Endpoint) {
                endpointsByFieldName.put(f.getName(), (Endpoint) obj);
                f.set(instance, null);
            }
        } catch (IllegalAccessException e) {
            log.error("Could not get field: " + f + ", class: " + instance.getClass() + " due to exception: " + e);
        } finally {
            f.setAccessible(false); // Set the field back to non-accessible
        }
	}

	private static Field[] fields(AbstractCustomModule instance) {
		return instance.getClass().getDeclaredFields();
	}
}
