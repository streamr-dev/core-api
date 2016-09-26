package com.unifina.service

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Session
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

class CassandraService {
	static transactional = false

	GrailsApplication grailsApplication

	private static final int FETCH_SIZE = 5000;

	// Thread-safe
	private Session session

	/**
	 * Returns a thread-safe session connected to the Streamr Cassandra cluster.
     */
	@CompileStatic
	Session getSession() {
		if (session==null) {
			Cluster.Builder builder = Cluster.builder();
			for (String host : grailsApplication.config["streamr"]["cassandra"]["hosts"]) {
				builder.addContactPoint(host);
			}
			Cluster cluster = builder.build();

			session = cluster.connect(grailsApplication.config["streamr"]["cassandra"]["keySpace"].toString());
			session.getCluster().getConfiguration().getQueryOptions().setFetchSize(FETCH_SIZE);
		}

		return session
	}

}
