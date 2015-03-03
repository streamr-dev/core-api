import org.grails.plugin.resource.mapper.MapperPhase


class CorsResourceMapper {
	static phase = MapperPhase.ALTERNATEREPRESENTATION
	
	/**
	 * Add Access-Control-Allow-Origin header to all resources
	 */
	def map(resource, config) {
		// Set up response headers
		resource.requestProcessors << { req, resp ->
			resp.setHeader('Access-Control-Allow-Origin', '*')
		}
	}
}
