modules = {
	"jquery-ui-touchpunch" {
		dependsOn 'jquery-ui'
		resource url:'js/touchpunch/jquery.ui.touch-punch.min.js', disposition: 'head'
		//		resource url:'js/touchpunch/jquery.ui.touch-punch.js', disposition: 'head'
	}
	"jquery.ui-contextmenu" {
		dependsOn 'jquery-ui'
		resource url:'js/jquery.ui-contextmenu/jquery.ui-contextmenu.js'
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
		resource url:'js/jsPlumb/jquery.jsPlumb-1.5.3.js'
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
	pnotify {
		dependsOn 'jquery, jquery-ui'
		resource url:'js/pnotify-1.2.0/jquery.pnotify.min.js'
		resource url:'js/pnotify-1.2.0/jquery.pnotify.default.css'
	}
	chosen {
		dependsOn 'jquery'
		resource url:'js/chosen-1.0.0/chosen.jquery.min.js'
		resource url:'js/chosen-1.0.0/chosen.min.css'
		resource url:'js/chosen-1.0.0/chosen-sprite.png'
		resource url:'js/chosen-1.0.0/chosen-sprite@2x.png'
	}
//	waypoints {
//		dependsOn 'jquery'
//		resource url:'js/waypoints-2.0.3/waypoints.min.js'
//	}
	jscroll {
		dependsOn 'jquery'
		resource url:'js/jscroll-2.1.1/jquery.jscroll.min.js'
	}
	'signalpath-loadBrowser' {
		resource url:'css/signalPath/widgets/loadBrowser.css'
	}
	'signalpath-core' {
		dependsOn 'jsplumb, jstree, highstock, atmosphere, codemirror, tablesorter, chosen, jquery.ui-contextmenu, signalpath-loadBrowser'
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
	}
	'signalpath-theme' {
		dependsOn 'signalpath-core'
		resource url:'css/signalPath/signalPath.css'
		
		resource url:'css/signalPath/themes/light/light.css'
		resource url:'css/signalPath/themes/light/light.js'
		resource url:'css/signalPath/themes/light/jquery-ui-theme/jquery-ui-1.10.3.custom.min.css'
		
//		resource url:'css/signalPath/themes/dark/dark.css'
//		resource url:'css/signalPath/themes/dark/dark.js'
//		resource url:'css/signalPath/themes/dark/jquery-ui-theme/jquery-ui.min.css'
//		resource url:'css/signalPath/themes/dark/jquery-ui-theme/jquery.ui.theme.css'
	}
	overrides {
		'jquery-ui' {
			resource id:'js', url:'js/jquery-ui-1.9.2/jquery-ui.js', nominify: true, disposition: 'head'
		}
	}
}
