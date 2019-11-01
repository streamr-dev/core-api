package com.unifina.signalpath.streams;

import com.unifina.domain.data.Stream;
import com.unifina.domain.security.Permission;
import com.unifina.domain.security.SecUser;
import com.unifina.service.PermissionService;
import com.unifina.signalpath.*;
import grails.orm.HibernateCriteriaBuilder;
import grails.util.Holders;
import groovy.lang.Closure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchStream extends AbstractSignalPathModule {

	private final StringInput searchName = new StringInput(this, "name");
	private final StringOutput streamOutput = new StringOutput(this, "stream");
	private final MapOutput fields = new MapOutput(this, "fields");
	private final BooleanOutput found = new BooleanOutput(this, "found");

	private transient PermissionService permissionService;

	@Override
	public void sendOutput() {
		if (permissionService == null) {
			permissionService = Holders.getApplicationContext().getBean(PermissionService.class);
		}

		List<Stream> streams = permissionService.get(Stream.class,
			SecUser.loadViaJava(getGlobals().getUserId()),
			Permission.Operation.READ,
			true,
			new NameFilteringClosure(this, searchName.getValue()));

		if (streams.isEmpty()) {
			found.send(false);
		} else {
			found.send(true);
			Stream stream = streams.get(0);
			Map<String, Object> config = stream.getStreamConfigAsMap();
			if (config.containsKey("fields")) {
				fields.send(listOfFieldConfigsToMap((List<Map<String, String>>) config.get("fields")));
			}
			streamOutput.send(stream.getId());
		}
	}

	@Override
	public void clearState() {}

	private static Map<String, String> listOfFieldConfigsToMap(List<Map<String, String>> fieldConfigs) {
		Map<String, String> map = new HashMap<>();
		for (Map<String, String> fieldConfig: fieldConfigs) {
			map.put(fieldConfig.get("name"), fieldConfig.get("type"));
		}
		return map;
	}

	static class NameFilteringClosure extends Closure {

		private String name;

		public NameFilteringClosure(Object owner, String name) {
			super(owner);
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public Object call() {
			return ((HibernateCriteriaBuilder) getDelegate()).eq("name", name);
		}

	}
}
