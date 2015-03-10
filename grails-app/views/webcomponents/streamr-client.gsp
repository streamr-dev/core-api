<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<r:require module="streamr-client"/>

<r:layoutResources disposition="head"/>
<r:layoutResources disposition="defer"/>

<polymer-element name="streamr-client" attributes="server">
	<script>
	(function(){
		var streamrClient = new StreamrClient()

		Polymer('streamr-client', {
			// This function is executed multiple times, once for each element!
			created: function() {
				this.streamrClient = streamrClient
			},
			// This function is executed multiple times, once for each element!
			ready: function() {
				if (this.server) {
					streamrClient.options.server = this.server
					streamrClient.connect()
				}
			},
			getClient: function() {
				console.log("getClient called!")
					return streamrClient
			}
		});
	})();
	</script>
	
</polymer-element>
