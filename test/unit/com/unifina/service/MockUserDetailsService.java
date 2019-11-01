package com.unifina.service;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;

public class MockUserDetailsService implements UserDetailsService {
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		return new MockUserDetails(username);
	}

	static class MockUserDetails implements UserDetails {

		private String username;

		public MockUserDetails(String username) {
			this.username = username;
		}
		
		@Override
		public Collection<GrantedAuthority> getAuthorities() {
			return new ArrayList<GrantedAuthority>(0);
		}

		@Override
		public String getPassword() {
			return "";
		}

		@Override
		public String getUsername() {
			return username;
		}

		@Override
		public boolean isAccountNonExpired() {
			return true;
		}

		@Override
		public boolean isAccountNonLocked() {
			return true;
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return true;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
		
	}
	
}
