package com.unifina.security;

import com.unifina.domain.security.SecUser;
import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;

import java.util.Date;

public class SessionToken {
	private String token;
	private SecUser user;
	private Date expiration;

	private static DateFormat df;
	private static DateFormat getDateFormat() {
		if (df == null) {
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		return df;
	}

	public SessionToken(int tokenLength, SecUser user, int ttlHours) {
		this.token = RandomStringUtils.randomAlphanumeric(tokenLength);
		this.user = user;
		this.expiration = new DateTime().plusHours(ttlHours).toDate();
	}

	public String getToken() {
		return token;
	}

	public SecUser getUser() {
		return user;
	}

	public Date getExpiration() {
		return expiration;
	}
}
