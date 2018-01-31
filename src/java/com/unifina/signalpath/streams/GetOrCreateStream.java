package com.unifina.signalpath.streams;

import com.unifina.domain.data.Stream;
import com.unifina.domain.security.Permission;
import com.unifina.domain.security.SecUser;
import com.unifina.service.PermissionService;
import grails.orm.HibernateCriteriaBuilder;
import grails.util.Holders;
import groovy.lang.Closure;

import java.util.List;

public class GetOrCreateStream extends CreateStream {

	private transient PermissionService permissionService;

	@Override
	public void sendOutput() {
		if (permissionService == null) {
			permissionService = Holders.getApplicationContext().getBean(PermissionService.class);
		}

		String streamId = cachedStreamIdsByName.get(getStreamName());

		if (streamId == null) {
			List<Stream> streams = permissionService.get(Stream.class,
				SecUser.loadViaJava(getGlobals().getUserId()),
				Permission.Operation.READ,
				true,
				new NameFilteringClosure(this, getStreamName()));

			if (!streams.isEmpty()) {
				Stream stream = streams.get(0);
				cachedStreamIdsByName.put(getStreamName(), stream.getId());
				streamId = stream.getId();
			}
		}

		if (streamId == null) {
			// create new stream: delegate to CreateStream module
			//   surely it now will sendOutputs with created==true (it can't have cached a name streamService doesn't know of)
			super.sendOutput();
		} else {
			sendOutputs(false, streamId);
		}
	}

	class NameFilteringClosure extends Closure {

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
