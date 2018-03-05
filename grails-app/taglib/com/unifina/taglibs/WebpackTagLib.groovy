package com.unifina.taglibs

/**
 * Helper taglib for including webpack-generated js and css files.
 * Webpack adds chunk hashes to production versions of bundles. The filenames of
 * these bundles are inspected and saved to application.properties upon war creation
 * (in _Events.groovy). The key-value-pairs in application.properties appear as
 * grailsApplication.metadata, from which they can be read.
 *
 * If the filenames are not found in metadata, a fallback name of name.bundle.js is assumed.
 */
class WebpackTagLib {

	static namespace = 'webpack'

	/**
	 * Includes a webpack js bundle.
	 *
	 * @attr name REQUIRED the bundle name
	 */
	def jsBundle = { attrs ->
		String name = attrs.name
		def config = grailsApplication.config

		// Check grailsApplication metadata for a filename that corresponds to this bundle, fallback to name.bundle.js
		String fileName = grailsApplication.metadata[config.webpack.jsFiles.metadataKey]?.split(',').find {it.startsWith("${name}.")} ?: "${name}.bundle.js"
		out << r.external(uri: "${config.webpack.bundle.dir}/${fileName}")
	}

	/**
	 *  Includes a webpack css bundle.
	 *
	 * @attr name REQUIRED the bundle name
	 */
	def cssBundle = { attrs ->
		String name = attrs.name
		def config = grailsApplication.config

		// Check grailsApplication metadata for a filename that corresponds to this bundle, fallback to name.bundle.js
		String fileName = grailsApplication.metadata[config.webpack.cssFiles.metadataKey]?.split(',').find {it.startsWith("${name}.")} ?: "${name}.bundle.css"
		out << r.external(uri: "${config.webpack.bundle.dir}/${fileName}")
	}
}
