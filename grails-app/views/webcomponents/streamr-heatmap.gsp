<link rel="import" href="${r.resource(uri:"/js/polymer/polymer.html")}">

<r:require module="jquery"/>
<r:require module="streamr-heatmap"/>

<r:layoutResources disposition="head"/>
<r:layoutResources disposition="defer"/>

<polymer-element name="streamr-heatmap" attributes="channel resendLast resendAll lifeTime fadeInTime fadeOutTime min max radius center zoom minZoom maxZoom">
	<template>
		<link rel="stylesheet" href="${r.resource(uri:'/js/leaflet-0.7.3/leaflet-0.7.3.css')}">
		<streamr-client id="client"></streamr-client>
		<div id="container" style="min-height:400px"></div>
	</template>
	
	<script>
		Polymer('streamr-heatmap',{
			publish: {
				channel: undefined,
				resendLast: 0,
				resendAll: undefined
			},
			ready: function() {
				var _this = this

				// Takes parent element as argument
				var map = new StreamrHeatMap(this.$.container, {
					lifeTime: this.lifeTime,
					fadeInTime: this.fadeInTime,
					fadeOutTime: this.fadeOutTime,
					min: this.min,
					max: this.max,
					radius: this.radius,
					center: this.center,
					zoom: this.zoom,
					minZoom: this.minZoom,
					maxZoom: this.maxZoom
				})
				var trySubscribe = function() {
					if(_this.$.client.streamrClient) {
						var client = _this.$.client.streamrClient
						var resendOptions = {}

						if (_this.resendAll)
							resendOptions.resend_all = true
						else if (_this.resendLast)
							resendOptions.resend_last = _this.resendLast

						console.log(resendOptions)

						client.subscribe(
						    _this.channel, 
						    function(message) {
						    	map.handleMessage(message)
						    },
						    resendOptions
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