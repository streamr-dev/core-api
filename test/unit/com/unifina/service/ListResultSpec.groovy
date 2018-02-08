package com.unifina.service

import com.unifina.domain.signalpath.Canvas
import grails.gorm.PagedResultList
import spock.lang.Specification

class ListResultSpec extends Specification {
	List<Canvas> canvases = [
		new Canvas(name: "canvas-1", state: Canvas.State.RUNNING, json: "{}"),
		new Canvas(name: "canvas-2", state: Canvas.State.STOPPED, json: "{}"),
		new Canvas(name: "canvas-3", state: Canvas.State.STOPPED, json: "{}"),
		new Canvas(name: "canvas-4", state: Canvas.State.STOPPED, json: "{}")
	]

	PagedResultList stubbedList

	void setup() {
		stubbedList = Stub(PagedResultList) {
			getTotalCount() >> 528
			size() >> 4
			iterator() >> canvases.iterator()
		}
	}

	void "toMap() returns expected Map"() {
		expect:
		new ListResult(stubbedList, null).toMap() == [
		    totalCount: 528L,
			numOfItems: 4,
			offset: 0,
			nextOffset: 4,
			items: canvases*.toMap()
		]
	}

	void "toMap() calculates offsets correctly"() {
		expect:
		new ListResult(stubbedList, 0).toMap().subMap("offset", "nextOffset") == [
			offset: 0,
			nextOffset: 4
		]

		new ListResult(stubbedList, 50).toMap().subMap("offset", "nextOffset") == [
			offset: 50,
			nextOffset: 54
		]


		new ListResult(stubbedList, 523).toMap().subMap("offset", "nextOffset") == [
			offset: 523,
			nextOffset: 527
		]


		new ListResult(stubbedList, 524).toMap().subMap("offset", "nextOffset") == [
			offset: 524,
			nextOffset: null
		]

		new ListResult(stubbedList, 999).toMap().subMap("offset", "nextOffset") == [
			offset: 999,
			nextOffset: null
		]
	}
}
