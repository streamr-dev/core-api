<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="streamr-chart"/>
	<r:require module="highstock"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-chart" extends="streamr-widget" attributes="rangeDropdown showHideButtons">
	<template>
		<shadow></shadow>
	</template>
	
	<script>
		Polymer('streamr-chart',{
			publish: {
				rangeDropdown: false,
				showHideButtons: false
			},
			ready: function() {
				var _this = this
				this.bindEvents(_this.$.container)

				this.getModuleJson(function(json) {
					var resendOptions = _this.getResendOptions(json)

					_this.chart = new StreamrChart(_this.$.container, {
						rangeDropdown: _this.rangeDropdown,	// default: true
						showHideButtons: _this.showHideButtons	// default: true
					})

					_this.sendRequest({type:'initRequest'}, function(response) {
						_this.chart.handleMessage(response.initRequest)
					})

					_this.subscribe(
						function(message) {
					    	_this.chart.handleMessage(message)
					    },
					    resendOptions
					)
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