<link rel="import" href="${r.resource(uri:"/js/polymer/polymer.html")}">

<r:require module="jquery"/>
<r:require module="highstock"/>
<r:require module="streamr-chart"/>

<r:layoutResources disposition="head"/>
<r:layoutResources disposition="defer"/>

<polymer-element name="streamr-chart" attributes="channel rangeDropdown showHideButtons">
	<template>
		<streamr-client id="client"></streamr-client>
		<div id="container"></div>
	</template>
	
	<script>
		Polymer('streamr-chart',{
			publish: {
				rangeDropdown: false,
				showHideButtons: false
			},
			ready: function() {
				var _this = this
				// Takes parent element as argument
				var chart = new StreamrChart(this.$.container, {
					rangeDropdown: this.rangeDropdown,	// default: true
					showHideButtons: this.showHideButtons	// default: true
				})
				var trySubscribe = function() {
					if(_this.$.client.streamrClient) {
						var client = _this.$.client.streamrClient
						client.subscribe(
						    _this.channel, 
						    function(message) {
						    	console.log(message)
						    	chart.handleMessage(message)
						    },
						    { 
						        resend_all: true
						    }
						)
					}
					else {
						setTimeout(trySubscribe, 200)
					}
				}
				trySubscribe()
			}
		});
	</script>
</polymer-element>