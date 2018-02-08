package com.unifina.service

import grails.gorm.PagedResultList

class ListResult {
	private final PagedResultList results
	private final Integer offset

	ListResult(PagedResultList results, Integer offset) {
		this.results = results
		this.offset = offset ?: 0
	}

	Map toMap() {
		int totalCount = results.totalCount
		int size = results.size()
		Integer nextOffset = null
		if (offset + size < totalCount) {
			nextOffset = offset + size
		}

		return [
			totalCount: totalCount,
			numOfItems: size,
			offset: offset,
			nextOffset: nextOffset,
			items: results*.toMap(),
		]
	}
}
