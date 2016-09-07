package com.unifina.signalpath;

public class EndpointTypeCompatibilityChecker {

	public static boolean acceptsAll(Endpoint e) {
		return e.getTypeName().contains("Object");
	}

	/**
	 * Check whether two endpoints have a compatible "data type" signature
	 */
	public static boolean areCompatible(Endpoint e1, Endpoint e2) {
		if (acceptsAll(e1) || acceptsAll(e2)) {
			return true;
		}

		String[] types1 = e1.getAcceptedTypes();
		String[] types2 = e2.getAcceptedTypes();

		for (String t1 : types1) {
			for (String t2 : types2) {
				if (t1.equals(t2)) {
					return true;
				}
			}
		}

		return false;
	}
}
