<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="streamr-table"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-table" extends="streamr-widget" attributes="maxRows">
	<template>
		<shadow></shadow>
	</template>
	
	<script>
		Polymer('streamr-table', {
			publish: {
				maxRows: 0
			},
			ready: function() {
				var _this = this
				this.bindEvents(_this.$.container)

				this.getModuleJson(function(json) {
					var resendOptions = _this.getResendOptions(json)
					var options = []
					options.maxRows = _this.maxRows

					_this.table = new StreamrTable(_this.$.container, options)
					_this.table.initTable()

					_this.sendRequest({type:'initRequest'}, function(response) {
						_this.table.receiveResponse(response.initRequest)
					})

					_this.subscribe(
						function(message) {
					    	_this.table.receiveResponse(message)
					    },
					    resendOptions
					)
				})

			},
			getTable: function() {
				return this.table
			},
			<g:if test="${params.lightDOM}">
				parseDeclaration: function(elementElement) {
					return this.lightFromTemplate(this.fetchTemplate(elementElement))
				}
			</g:if>
		});
	</script>
</polymer-element>