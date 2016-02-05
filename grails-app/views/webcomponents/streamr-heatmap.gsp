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

					_this.map = new StreamrHeatMap(_this.$["streamr-widget-container"], {
						lifeTime: this.lifeTime,
						fadeInTime: this.fadeInTime,
						fadeOutTime: this.fadeOutTime,
						min: this.min,
						max: this.max,
						radius: this.radius,
						center: (this.center!=null && this.center.length===2 ? this.center : undefined),
						zoom: this.zoom,
						minZoom: this.minZoom,
						maxZoom: this.maxZoom
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