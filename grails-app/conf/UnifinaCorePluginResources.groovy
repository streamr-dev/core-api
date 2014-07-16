modules = {
	tablesorter {
		dependsOn 'jquery'
		resource url:[dir:'js/tablesorter', file:'jquery.tablesorter.min.js', plugin: 'unifina-core']
	}
	highcharts {
		resource url:[dir:'js/highcharts-2.3.3', file:'highcharts.src.js', plugin: 'unifina-core']
	}
	highstock {
		resource url:[dir:'js/highstock-1.3.9/js', file:'highstock.js', plugin: 'unifina-core']
		resource url:[dir:'js/highstock-1.3.9/js', file:'highcharts-more.js', plugin: 'unifina-core']
	}
	bootstrap {
		dependsOn 'jquery'
		resource url:[dir:'js/bootstrap-3.2.0-dist/js', file:'bootstrap.min.js', plugin: 'unifina-core']
		resource url:[dir:'js/bootstrap-3.2.0-dist/css', file:'bootstrap.min.css', plugin: 'unifina-core']
		resource url:[dir:'js/bootstrap-3.2.0-dist/css', file:'bootstrap-theme.min.css', plugin: 'unifina-core']
	}
	'bootstrap-datepicker' {
		dependsOn 'bootstrap'
		resource url:[dir:'js/bootstrap-datepicker/js', file:'bootstrap-datepicker.js', plugin: 'unifina-core']
		resource url:[dir:'js/bootstrap-datepicker/css', file:'datepicker.css', plugin: 'unifina-core']
	}
	codemirror {
//		resource url:[dir:'js/codemirror-3.21', file:'codemirror.js']
		resource url:[dir:'js/codemirror-3.21', file:'codemirror-compressed.js', plugin: 'unifina-core']
		resource url:[dir:'js/codemirror-3.21', file:'codemirror.css', plugin: 'unifina-core']
//		resource url:[dir:'js/codemirror', file:'codemirror.js']
//		resource url:[dir:'js/codemirror', file:'groovy.js']
//		resource url:[dir:'js/codemirror', file:'codemirror.css']
	}
	superfish {
		dependsOn 'jquery'
		resource url:[dir:'js/superfish/js', file:'superfish.min.js', plugin: 'unifina-core']
		resource url:[dir:'js/superfish/css', file:'superfish.css', plugin: 'unifina-core']
		resource url:[dir:'js/superfish/js', file:'supposition.js', plugin: 'unifina-core']
	}

	jsplumb {
		resource url:[dir:'js/jsPlumb', file:'dom.jsPlumb-1.6.2.js', plugin: 'unifina-core']
	}
	jstree {
		dependsOn 'jquery'
		resource url:[dir:'js/jsTree', file:'jquery.jstree.js', plugin: 'unifina-core']
		// If you change the theme, check SignalPathTagLib too
		resource url:[dir:'js/jsTree/themes/classic', file:'style.css', plugin: 'unifina-core']
	}
	atmosphere {
		dependsOn 'jquery'
		resource url:[dir:'js/atmosphere', file:'jquery.atmosphere.js', plugin: 'unifina-core']
	}
	hotkeys {
		dependsOn 'jquery'
		resource url:[dir:'js/hotkeys', file:'jquery.hotkeys.js', plugin: 'unifina-core']
	}
	joyride {
		dependsOn 'jquery'
		resource url:[ dir: 'js/joyride-2.1', file: 'joyride-2.1.css', plugin: 'unifina-core']
		resource url:[ dir: 'js/joyride-2.1', file: 'modernizr.mq.js', plugin: 'unifina-core']
		resource url:[ dir: 'js/joyride-2.1', file: 'jquery.cookie.js', plugin: 'unifina-core']
		resource url:[ dir: 'js/joyride-2.1', file: 'jquery.joyride-2.1.js', plugin: 'unifina-core']
	}
	pnotify {
		dependsOn 'jquery'
//		resource url:[dir:'js/pnotify-1.2.0', file:'jquery.pnotify.min.js']
		resource url:[dir:'js/pnotify-1.2.0', file:'jquery.pnotify.1.2.2-snapshot.js', plugin: 'unifina-core']
		resource url:[dir:'js/pnotify-1.2.0', file:'jquery.pnotify.default.css', plugin: 'unifina-core']
	}
	'global-error-handler' {
		resource url:[dir:'js', file:'globalErrorHandler.js', plugin: 'unifina-core']
	}
	'signalpath-loadBrowser' {
		resource url:[dir:'css/signalPath/widgets', file:'loadBrowser.css', plugin: 'unifina-core']
	}
	'signalpath-core' {
		dependsOn 'jsplumb, jstree, highstock, atmosphere, codemirror, tablesorter, signalpath-loadBrowser'
		resource url:[dir:'js/signalPath/core', file:'signalPath.js', plugin: 'unifina-core']
		resource url:[dir:'js/timezones', file:'detect_timezone.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/generic', file:'emptyModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/generic', file:'genericModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/core', file:'IOSwitch.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/core', file:'Endpoint.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/core', file:'Input.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/core', file:'Parameter.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/core', file:'Output.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/specific', file:'chartModule.js', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/modules', file:'chartModule.css', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/specific', file:'gaugeModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/specific', file:'customModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/specific', file:'tableModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/specific', file:'commentModule.js', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/modules', file:'commentModule.css', plugin: 'unifina-core']
		resource url:[dir:'js/signalPath/specific', file:'labelModule.js', plugin: 'unifina-core']
	}
	'signalpath-theme' {
		dependsOn 'signalpath-core'
		resource url:[dir:'css/signalPath', file:'signalPath.css', plugin: 'unifina-core']
		
		resource url:[dir:'css/signalPath/themes/light', file:'light.css', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/themes/light', file:'light.js', plugin: 'unifina-core']
		
//		resource url:[dir:'css/signalPath/themes/dark', file:'dark.css']
//		resource url:[dir:'css/signalPath/themes/dark', file:'dark.js']
	}
}
