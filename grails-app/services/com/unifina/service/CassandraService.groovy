package com.unifina.service

import com.datastax.driver.core.Session
import com.unifina.feed.cassandra.CassandraConfig
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
	Session getSession() {
		if (session==null) {
			CassandraConfig config = new CassandraConfig(grailsApplication.config.streamr.cassandra)
			session = config.createSession()
			session.getCluster().getConfiguration().getQueryOptions().setFetchSize(FETCH_SIZE);
		}

		return session
	}

}
