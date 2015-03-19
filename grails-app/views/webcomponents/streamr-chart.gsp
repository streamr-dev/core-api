<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<r:require module="highstock"/>
<r:require module="streamr-chart"/>

<r:layoutResources disposition="head"/>
<r:layoutResources disposition="defer"/>

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

				this.getModuleJson(function(json) {
					var resendOptions = _this.getResendOptions(json)

					var chart = new StreamrChart(_this.$.container, {
						rangeDropdown: _this.rangeDropdown,	// default: true
						showHideButtons: _this.showHideButtons	// default: true
					})

					_this.sendRequest({type:'initRequest'}, function(response) {
						chart.handleMessage(response.initRequest)
					})

					_this.subscribe(
						function(message) {
					    	chart.handleMessage(message)
					    },
					    resendOptions
					)
				})
			}
		});
	</script>
</polymer-element>