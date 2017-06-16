
<r:script>
	<g:if test="${grails.util.Holders.config.streamr.tours.enabled}">
		Streamr.Tours = [{
		title: "Introduction to Streamr",
		controller: "canvas",
		action: "editor",
		url: "${resource(dir: 'misc/tours', file: '0.js', plugin: 'unifina-core')}"
		},{
			title: "Building logic in Streamr",
			controller: "canvas",
			action: "editor",
			url: "${resource(dir: 'misc/tours', file: '1.js', plugin: 'unifina-core')}"
		},{
			title: "Real-time data and reacting to events",
			controller: "canvas",
			action: "editor",
			url: "${resource(dir: 'misc/tours', file: '2.js', plugin: 'unifina-core')}"
		}]
	</g:if>
	<g:else>
		Streamr.Tours = []
	</g:else>
</r:script>