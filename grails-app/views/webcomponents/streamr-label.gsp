<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<polymer-element name="streamr-label" extends="streamr-widget" attributes="channel">
	<template>
		<streamr-client id="client"></streamr-client>
		<span class="streamr-label-value">{{value}}</span>
	</template>
	
	<script>
		Polymer('streamr-label',{
			ready: function() {
				this.bindEvents(this.$.container)

				var _this = this
				var trySubscribe = function() {
					if (_this.$.client.streamrClient) {
						_this.$.client.streamrClient.subscribe(
							_this.channel, 
							function(message) {
								_this.value = message.value
								_this.fire('value')
							},
							{resend_last: 1}
						)
					}
					else {
						setTimeout(trySubscribe, 200)
					}
				}
				trySubscribe()
			},
			getValue: function() {
				return this.value
			},
			<g:if test="${params.lightDOM}">
				parseDeclaration: function(elementElement) {
					return this.lightFromTemplate(this.fetchTemplate(elementElement))
				}
			</g:if>
		});
	</script>
</polymer-element>

