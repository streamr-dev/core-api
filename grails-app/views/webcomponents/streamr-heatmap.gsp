<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="streamr-heatmap"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-heatmap" extends="streamr-widget" attributes="lifeTime fadeInTime fadeOutTime min max radius centerLat centerLng zoom minZoom maxZoom">
	<!-- Using shadow element doesn't work with CSS -->
	<template>
		<link rel="stylesheet" href="${r.resource(dir:'/js/leaflet', file:'leaflet.css', plugin:'unifina-core')}">
		<streamr-client id="client"></streamr-client>
		<div id="container"></div>
	</template>
	
	<script>
		Polymer('streamr-heatmap', {
			ready: function() {			
				var _this = this
				this.bindEvents(_this.$["streamr-widget-container"])

				this.$["streamr-widget-container"].setAttribute("style", "min-height:400px")

				this.getModuleJson(function(json) {
					var options = _this.getModuleOptionsWithOverrides(json)
					_this.map = new StreamrHeatMap(_this.$["streamr-widget-container"], options)

					_this.subscribe(function(message) {
						_this.map.handleMessage(message)
					})
				})

			},
			getMap: function() {
				return this.map
			},
			centerLatChanged: function(oldValue, newValue) {
				this.map.setCenter(newValue, this.map.map.getCenter().lng)
			},
			centerLngChanged: function(oldValue, newValue) {
				this.map.setCenter(this.map.map.getCenter().lat, newValue)
			},
			<g:if test="${params.lightDOM}">
				parseDeclaration: function(elementElement) {
					return this.lightFromTemplate(this.fetchTemplate(elementElement))
				}
			</g:if>
		});
	</script>
</polymer-element>