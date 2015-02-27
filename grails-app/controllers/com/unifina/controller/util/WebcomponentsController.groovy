package com.unifina.controller.util

class WebcomponentsController {
	def index() {
		header 'Access-Control-Allow-Origin', '*'
		
		// Strip ".html" from requested view
		render(view: params.view.replace(".html",""))
	}
}
