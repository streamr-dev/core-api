<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<r:layoutResources disposition="head"/>
<r:layoutResources disposition="defer"/>

<polymer-element name="streamr-label" attributes="channel">
	<template>
		<streamr-client id="client"></streamr-client>
		<span class="streamr-label-value">{{value}}</span>
	</template>
	
	<script>
		Polymer('streamr-label',{
			ready: function() {
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
			parseDeclaration: function(elementElement) {
				return this.lightFromTemplate(this.fetchTemplate(elementElement))
			}
		});
	</script>
</polymer-element>

