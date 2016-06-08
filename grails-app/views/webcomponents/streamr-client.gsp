<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="streamr-client"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>



<polymer-element name="streamr-client" attributes="server autoconnect autodisconnect">
	<script>
	(function(){
		var streamrClient

		function createClient(cb, element) {
			if (streamrClient)
				cb(streamrClient)
			else if (typeof StreamrClient !== 'undefined' && element.server) {
				var myOptions = {
					server: element.server,
					autoConnect: (element.autoconnect != null ? element.autoconnect : true),
					autoDisconnect: (element.autodisconnect != null ? element.autodisconnect : true)
				}

				streamrClient = new StreamrClient(myOptions)
				cb(streamrClient)
			}
			else {
				setTimeout(function() {
					createClient(cb, element)
				}, 100)
			}
		}

		Polymer('streamr-client', {
			publish: {
				// Leave the default value of server as undefined, because we identify 
				// the "top" element by checking if this.server is defined
				server: undefined, 
				autoconnect: true,
				autodisconnect: true
			},
			// This function is executed multiple times, once for each element!
			ready: function() {
				createClient(function(client) {}, this)
			},
			getClient: function(cb) {
				if (streamrClient)
					cb(streamrClient)
				else
					createClient(cb, this)
			},
			<g:if test="${params.lightDOM}">
				parseDeclaration: function(elementElement) {
					return this.lightFromTemplate(this.fetchTemplate(elementElement))
				}
			</g:if>
		});
	})();
	</script>
	
</polymer-element>
