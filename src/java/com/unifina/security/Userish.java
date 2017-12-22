package com.unifina.security;

import com.unifina.domain.security.SecUser;

public interface Userish {
	Userish resolveToUserish();
	SecUser resolveToSecUser();
}
