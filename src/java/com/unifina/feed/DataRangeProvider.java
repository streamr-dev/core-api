package com.unifina.feed;

import com.unifina.domain.data.Stream;

public interface DataRangeProvider {
	DataRange getDataRange(Stream stream);
}
