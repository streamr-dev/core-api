<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="streamr-heatmap"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-heatmap" extends="streamr-widget" attributes="lifeTime fadeInTime fadeOutTime min max radius center zoom minZoom maxZoom">
	<!-- Using shadow element doesn't work with CSS -->
	<template>
		<link rel="stylesheet" href="${r.resource(dir:'/js/leaflet', file:'leaflet.css', plugin:'unifina-core')}">
		<streamr-client id="client"></streamr-client>
		<div id="container"></div>
	</template>
	
	<script>
		Polymer('streamr-heatmap', {
			publish: {
				// hint that center is an array
				center: []
			},
			ready: function() {			
				var _this = this
				this.bindEvents(_this.$["streamr-widget-container"])

				this.$["streamr-widget-container"].setAttribute("style", "min-height:400px")

				this.getModuleJson(function(json) {
					var resendOptions = _this.getResendOptions(json)
					var mapOptions = {}
					if (json.options) {
						Object.keys(json.options).forEach(function(key) {
							mapOptions[key] = json.options[key].value
						})
					}
					_this.map = new StreamrHeatMap(_this.$["streamr-widget-container"], {
						lifeTime: this.lifeTime !== undefined ? this.lifeTime : mapOptions.lifeTime,
						fadeInTime: this.fadeInTime !== undefined ? this.fadeInTime : mapOptions.fadeInTime,
						fadeOutTime: this.fadeOutTime !== undefined ? this.fadeOutTime : mapOptions.fadeOutTime,
						min: this.min !== undefined ? this.min : mapOptions.min,
						max: this.max !== undefined ? this.max : mapOptions.max,
						centerLat: this.centerLat !== undefined ? this.centerLat : mapOptions.centerLat,
						centerLng: this.centerLng !== undefined ? this.centerLng : mapOptions.centerLng,
						zoom: this.zoom !== undefined ? this.zoom : mapOptions.zoom,
						minZoom: this.minZoom !== undefined ? this.minZoom : mapOptions.minZoom,
						maxZoom: this.maxZoom !== undefined ? this.maxZoom : mapOptions.maxZoom
					})

					_this.subscribe(
						function(message) {
					    	_this.map.handleMessage(message)
					    },
					    resendOptions
					)

				})

			},
			centerChanged: function(oldValue, newValue) {
				this.map.setCenter(newValue)
			},
			<g:if test="${params.lightDOM}">
				parseDeclaration: function(elementElement) {
					return this.lightFromTemplate(this.fetchTemplate(elementElement))
				}
			</g:if>
		});
	</script>
</polymer-element>