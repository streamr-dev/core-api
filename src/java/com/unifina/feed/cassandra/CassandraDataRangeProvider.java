package com.unifina.feed.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractDataRangeProvider;
import com.unifina.feed.DataRange;
import com.unifina.service.CassandraService;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Date;

/**
 * Reads the timestamp of the first and last rows in the Cassandra cluster defined by the given Stream.
 */
public class CassandraDataRangeProvider extends AbstractDataRangeProvider {

	public CassandraDataRangeProvider(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

	@Override
	public DataRange getDataRange(Stream stream) {
		Session session = null;
		try {
			session = getSession(stream);
			Date minDate = null;
			Date maxDate = null;
			for (int partition=0; partition<stream.getPartitions(); partition++) {
				DataRange range = getDataRangeForStreamPartition(stream, partition, session);
				if (range != null) {
					if (minDate == null || range.getBeginDate().before(minDate)) {
						minDate = range.getBeginDate();
					}
					if (maxDate == null || range.getEndDate().after(maxDate)) {
						maxDate = range.getEndDate();
					}
				}
			}
			if (minDate != null && maxDate != null) {
				return new DataRange(minDate, maxDate);
			} else {
				return null;
			}
		} finally {
			if (session != null) {
				cleanup(session);
			}
		}
	}

	private DataRange getDataRangeForStreamPartition(Stream stream, Integer partition, Session session) {
		ResultSet resultSet = session.execute("SELECT ts FROM stream_events WHERE stream = ? AND stream_partition = ? ORDER BY kafka_offset ASC LIMIT 1", stream.getId(), partition);
		Row firstRow = resultSet.one();
		if (firstRow != null) {
			Date firstTimestamp = firstRow.getTimestamp("ts");
			resultSet = session.execute("SELECT ts FROM stream_events WHERE stream = ? AND stream_partition = ? ORDER BY kafka_offset DESC LIMIT 1", stream.getId(), partition);
			Row lastRow = resultSet.one();
			Date lastTimestamp = lastRow.getTimestamp("ts");
			return new DataRange(firstTimestamp, lastTimestamp);
		}
		else return null;
	}

	protected Session getSession(Stream stream) {
		return grailsApplication.getMainContext().getBean(CassandraService.class).getSession();
	}

	protected void cleanup(Session session) {
		// Don't close the session, it's reusable
	}
}
