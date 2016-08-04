package com.unifina.feed.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CassandraConfig extends HashMap<String, Object> {

	public CassandraConfig(Map<String, Object> map) {
		super(map);
	}

	public CassandraConfig(List<String> hosts, String keySpace) {
		this.put("hosts", hosts);
		this.put("keySpace", keySpace);
	}

	public List<String> getHosts() {
		return (List<String>) this.get("hosts");
	}

	public String getKeySpace() {
		return (String) this.get("keySpace");
	}

	/**
	 * Creates a Cassandra session from this config. Remember to close the cluster
	 * after you're done by calling session.getCluster().close().
     */
	public Session createSession() {
		Cluster.Builder builder = Cluster.builder();
		for (String host : getHosts()) {
			builder.addContactPoint(host);
		}
		Cluster cluster = builder.build();

		Session session = cluster.connect(getKeySpace());
		return session;
	}

}
