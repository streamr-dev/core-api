package com.unifina.cps;

public interface CommunityOperatorService {
	CommunityOperatorServiceImpl.ProxyResponse stats(String communityAddress);

	CommunityOperatorServiceImpl.ProxyResponse members(String communityAddress);

	CommunityOperatorServiceImpl.ProxyResponse memberStats(String communityAddress, String memberAddress);
}
