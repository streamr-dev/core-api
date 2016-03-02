<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="streamr-map"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-map" extends="streamr-widget" attributes="centerLat centerLng zoom minZoom maxZoom drawTrace traceRadius">
	<!-- Using shadow element doesn't work with CSS -->
	<template>
		<link rel="stylesheet" href="${r.resource(dir:'/js/leaflet', file:'leaflet.css', plugin:'unifina-core')}">
		<streamr-client id="client"></streamr-client>
	</template>
	
	<script>
		Polymer('streamr-map', {
			ready: function() {
				var _this = this
				this.bindEvents(_this.$["streamr-widget-container"])

				this.$["streamr-widget-container"].setAttribute("style", "min-height:400px")
				this.$["streamr-widget-container"].classList.add("map-container")

				this.getModuleJson(function(json) {
					var resendOptions = _this.getResendOptions(json)
					var mapOptions = {}
					if (json.options) {
						Object.keys(json.options).forEach(function(key) {
							mapOptions[key] = json.options[key].value
						})
					}
					_this.map = new StreamrMap(_this.$["streamr-widget-container"], {
						centerLat: this.centerLat !== undefined ? this.centerLat : mapOptions.centerLat,
						centerLng: this.centerLng !== undefined ? this.centerLng : mapOptions.centerLng,
						zoom: this.zoom !== undefined ? this.zoom : mapOptions.zoom,
						minZoom: this.minZoom !== undefined ? this.minZoom : mapOptions.minZoom,
						maxZoom: this.maxZoom !== undefined ? this.maxZoom : mapOptions.maxZoom,
						drawTrace: this.drawTrace !== undefined ? this.drawTrace : mapOptions.drawTrace,
						traceRadius: this.traceRadius !== undefined ? this.traceRadius : mapOptions.traceRadius
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