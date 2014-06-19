package com.unifina.controller.kafka;

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin

import org.junit.After
import org.junit.Before
import org.junit.Test

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.kafka.KafkaHistoricalFeed
import com.unifina.service.KafkaService
import com.unifina.utils.TimeOfDayUtil

@TestMixin(ControllerUnitTestMixin) // adds JSON converter support
@TestFor(KafkaService)
@Mock([FeedFile, Task, Stream, Feed])
class KafkaServiceTests {

	Feed feed
	Stream stream
	
	@Before
	public void setUp() throws Exception {
		feed = new Feed(backtestFeed: KafkaHistoricalFeed.class.name)
		feed.save(validate:false)
		assert Feed.count()==1
		
		stream = new Stream(feed: feed, name: "testfeed", streamConfig: "{'topic':'test'}", localId:"test")
		stream.save(validate:false)
		assert Stream.count()==1
		assert Stream.findByFeed(feed)==stream
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void createCollectTasksFirst() {
		// No feed files, should start from yesterday
		def result = service.createCollectTasks(stream)
		assert result.size()==1
		assert Task.count()==1
		Map config = JSON.parse(result[0].config)
		assert new Date(config.beginDate) == TimeOfDayUtil.getMidnight(new Date()-1)
	}

	@Test
	public void createCollectTasksLater() {
		// Existing FeedFile(s)
		FeedFile feedFile = new FeedFile(feed: feed, beginDate: TimeOfDayUtil.getMidnight(new Date()-10), endDate: new Date(TimeOfDayUtil.getMidnight(new Date()-9).time-1))
		feedFile.save(validate:false)
		assert FeedFile.count()==1
		
		def result = service.createCollectTasks(stream)
		assert result.size()==9
		assert Task.count()==9
		Map config = JSON.parse(result[8].config)
		assert new Date(config.endDate).before(TimeOfDayUtil.getMidnight(new Date()))
	}
	
}
