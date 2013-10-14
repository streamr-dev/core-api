modules = {
	"jquery-ui-touchpunch" {
		dependsOn 'jquery-ui'
		resource url:'js/touchpunch/jquery.ui.touch-punch.min.js', disposition: 'head'
		//		resource url:'js/touchpunch/jquery.ui.touch-punch.js', disposition: 'head'
	}
	tablesorter {
		dependsOn 'jquery'
		resource url:'js/tablesorter/jquery.tablesorter.min.js'
	}
	highcharts {
		resource url:'js/highcharts-2.3.3/highcharts.src.js'
	}
	highstock {
		resource url:'js/highstock/js/highstock.src.js'
	}
	codemirror {
		resource url:'js/codemirror/codemirror.js'
		resource url:'js/codemirror/groovy.js'
		resource url:'js/codemirror/codemirror.css'
	}
	jsplumb {
		dependsOn 'jquery'
		dependsOn 'jquery-ui'
		resource url:'js/jsPlumb/jquery.jsPlumb-1.3.13-all.js'
	}
	jstree {
		dependsOn 'jquery'
		resource url:'js/jsTree/jquery.jstree.js'
		// If you change the theme, check SignalPathTagLib too
		resource url:'js/jsTree/themes/classic/style.css'
	}
	atmosphere {
		dependsOn 'jquery'
		resource url:'js/atmosphere/jquery.atmosphere.js'
	}
	hotkeys {
		dependsOn 'jquery'
		resource url:'js/hotkeys/jquery.hotkeys.js'
	}
	'signalpath-loadBrowser' {
		resource url:'css/signalPath/widgets/loadBrowser.css'
	}
	'signalpath-core' {
		dependsOn 'jsplumb, jstree, highstock, atmosphere, codemirror, tablesorter, signalpath-loadBrowser'
		resource url:'js/signalPath/core/signalPath.js'
		resource url:'js/timezones/detect_timezone.js'
		resource url:'js/signalPath/generic/emptyModule.js'
		resource url:'js/signalPath/generic/genericModule.js'
		resource url:'js/signalPath/generic/dynamicInputsModule.js'
//		resource url:'js/signalPath/specific/rapidModelModule.js'
		resource url:'js/signalPath/specific/chartModule.js'
		resource url:'css/signalPath/modules/chartModule.css'
		resource url:'js/signalPath/specific/customModule.js'
		resource url:'js/signalPath/specific/tableModule.js'
		resource url:'js/signalPath/specific/commentModule.js'
		resource url:'css/signalPath/modules/commentModule.css'
		resource url:'css/signalPath/signalPath.css'
	}
}
