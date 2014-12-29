'use strict';

var kafka = require('kafka-node');
var Consumer = kafka.Consumer;
var Producer = kafka.Producer;
var Offset = kafka.Offset;
var Client = kafka.Client;
var argv = require('optimist').argv;
var topic = argv.topic || 'twitter-test-slush';

var client = new Client('192.168.10.81:2181');
var topics = [],
    options = { 
		autoCommit: false, 
		fromBeginning: false, 
		fetchMaxWaitMs: 1000, 
		fetchMaxBytes: 1024*1024,
		fromOffset: true
	};

var consumer = new Consumer(client, topics, options);
var offset = new Offset(client);

consumer.on('message', function (message) {
    console.log(this.id, message);
});
consumer.on('error', function (err) {
    console.log('error', err);
});
consumer.on('offsetOutOfRange', function (topic) {
	console.log("topic: "+JSON.stringify(topic))
    topic.maxNum = 2;
    offset.fetch([topic], function (err, offsets) {
    	console.log("offsets: "+JSON.stringify(err))
    	console.log("err: "+JSON.stringify(err))
    	if (offsets) {
            var min = Math.min.apply(null, offsets[topic.topic][topic.partition]);
            consumer.setOffset(topic.topic, topic.partition, min-1);
    	}
    });
})

function addTopic(topic, headOffset) {
	// Query the latest offsets for the topic
	offset.fetch([{topic:topic, time:(headOffset!=null ? -1 : -2)}], function (err, offsets) {
		console.log("Offsets: "+JSON.stringify(offsets))
		var offset = (headOffset!=null ? offsets[topic]["0"][0] + headOffset : offsets[topic]["0"][0])
		console.log("Subscribing from offset: "+offset)
		consumer.addTopics([{ topic: topic, offset: offset}], function (err, added) {
			console.log("Added topics: "+JSON.stringify(added))
		}, true);
	});
}

addTopic(topic, -1)