package com.unifina.api

import javax.servlet.http.HttpServletRequest

class HTTPRequestDumper {
	static void dump(String classDotMethodName, HttpServletRequest req) {
		System.out.printf("%s%n", classDotMethodName)
		for (final  String headerName : req.getHeaderNames()) {
			System.out.printf("%s%n", headerName)
			for (final String headerValue : req.getHeaders(headerName)) {
				System.out.printf("\t%s%n", headerValue)
			}
		}
		System.out.printf("%n%n")
	}
}
