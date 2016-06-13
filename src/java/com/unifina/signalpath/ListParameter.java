package com.unifina.signalpath;

import grails.converters.JSON;
import org.codehaus.groovy.grails.web.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ListParameter extends Parameter<List> {

	public ListParameter(AbstractSignalPathModule owner, String name, List defaultValue) {
		super(owner, name, defaultValue, "List");
	}

	public ListParameter(AbstractSignalPathModule owner, String name) {
		super(owner, name, new ArrayList(), "List");
	}

	@Override
	public List parseValue(String s) {
		try {
			return (JSONArray) JSON.parse(s);
		} catch (Exception e) {
			throw new RuntimeException("Invalid list parameter representation: " + s, e);
		}
	}

	@Override
	protected boolean isEmpty(List value) {
		return super.isEmpty(value) || (value != null && value.isEmpty());
	}
}
