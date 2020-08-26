package com.unifina.feed;

import com.unifina.domain.Stream;

public interface DataRangeProvider {
	DataRange getDataRange(Stream stream);
}
