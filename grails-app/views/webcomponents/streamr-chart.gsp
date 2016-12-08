<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="streamr-chart"/>
	<r:require module="highstock"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-chart" extends="streamr-widget" attributes="rangeDropdown showHideButtons displayTitle">
	<template>
		<shadow></shadow>
	</template>
	
	<script>
		Polymer('streamr-chart',{
			ready: function() {
				var _this = this
				this.bindEvents(_this.$["streamr-widget-container"])

				this.getModuleJson(function(json) {
					var options = _this.getModuleOptionsWithOverrides(json)

					// Webcomponent has different default options for these
					if (options.rangeDropdown === undefined) {
						options.rangeDropdown = false
					}
					if (options.showHideButtons === undefined) {
						options.showHideButtons = false
					}

					_this.chart = new StreamrChart(_this.$["streamr-widget-container"], options)

					_this.sendRequest({type:'initRequest'}, function(response) {
						_this.chart.handleMessage(response.initRequest)
					})

					_this.subscribe(function(message) {
						_this.chart.handleMessage(message)
					})
				})
			},
			getChart: function() {
				return this.chart
			},
			<g:if test="${params.lightDOM}">
				parseDeclaration: function(elementElement) {
					return this.lightFromTemplate(this.fetchTemplate(elementElement))
				}
			</g:if>
		});
	</script>
</polymer-element>