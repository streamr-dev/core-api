modules = {
	// jquery-migrate can be removed when there are no longer dependencies on pre-1.9 jquery
	"jquery-migrate" {
		dependsOn 'jquery'
		resource url:[dir:'js/jquery-migrate-1.2.1', file:'jquery-migrate-1.2.1.min.js', plugin: 'unifina-core']
	}
	"touchpunch" {
		dependsOn 'jquery-ui'
		resource url:[dir:'js/touchpunch/', file:'jquery.ui.touch-punch.min.js', disposition: 'head', plugin: 'unifina-core']
	}
	"password-meter" {
		dependsOn 'jquery'
		resource url:[dir:'js/unifina/password-meter', file:'password-meter.js', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/widgets', file:'password-meter.css', plugin: 'unifina-core']
		resource url:[dir:'js/zxcvbn', file:'zxcvbn-async.min.js', plugin: 'unifina-core']
	}
	tablesorter {
		dependsOn 'jquery'
		resource url:[dir:'js/tablesorter', file:'jquery.tablesorter.min.js', plugin: 'unifina-core']
	}
	highstock {
		resource url:[dir:'js/highstock-2.0.3', file:'highstock.js', plugin: 'unifina-core']
		resource url:[dir:'js/highstock-2.0.3', file:'highcharts-more.js', plugin: 'unifina-core']
	}
	bootstrap {
		dependsOn 'jquery'
		resource url:[dir:'js/bootstrap-3.2.0-dist/js', file:'bootstrap.js', plugin: 'unifina-core']
		resource url:[dir:'js/bootstrap-3.2.0-dist/css', file:'bootstrap.min.css', plugin: 'unifina-core']
	}
	'font-awesome' {
		resource url: "https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"
	}
	'bootstrap-docs' {
		dependsOn "bootstrap"
		resource url:[dir:'js/bootstrap-3.2.0-assets/css', file:'docs.min.css', plugin: 'unifina-core']
	}
	'bootstrap-contextmenu' {
		dependsOn 'bootstrap'
		resource url:[dir:'js/bootstrap-contextmenu', file:'contextmenu.js', plugin: 'unifina-core']
	}
	bootbox {
		dependsOn 'bootstrap'
		resource url:[dir:'js/bootbox', file:'bootbox.js', plugin: 'unifina-core']
	}
	'bootstrap-datepicker' {
		dependsOn 'bootstrap'
		// Current version is from: https://raw.github.com/n9/bootstrap-datepicker/6deee4ec7fa22bd1dee78913e0340f3841f58982/js/bootstrap-datepicker.js
		// due to this issue not yet fixed: https://github.com/eternicode/bootstrap-datepicker/issues/775
		resource url:[dir:'js/bootstrap-datepicker/js', file:'bootstrap-datepicker.js', plugin: 'unifina-core']
	}
	hopscotch {
		resource url:[dir:'js/hopscotch', file:'hopscotch.css', plugin: 'unifina-core']
		resource url:[dir:'js/hopscotch', file:'hopscotch.js', plugin: 'unifina-core']
	}
	typeahead {
		dependsOn 'jquery'
		resource url:[dir:'js/typeahead', file:'typeahead.bundle.js', plugin: 'unifina-core']
	}
	mustache {
		resource url:[dir:'js/mustache-0.8.2', file:'mustache.js', plugin: 'unifina-core']
	}
	codemirror {
		resource url:[dir:'js/codemirror-3.21', file:'codemirror-compressed.js', plugin: 'unifina-core']
		resource url:[dir:'js/codemirror-3.21', file:'codemirror.css', plugin: 'unifina-core']
	}
	jsplumb {
		dependsOn 'jquery'
		dependsOn 'jquery-ui'
		resource url:[dir:'js/jsPlumb/', file:'jquery.jsPlumb-1.5.3.js', plugin: 'unifina-core']
	}
	jstree {
		dependsOn 'jquery'
		resource url:[dir:'js/jsTree', file:'jquery.jstree.js', plugin: 'unifina-core']
		// If you change the theme, check SignalPathTagLib too
		resource url:[dir:'js/jsTree/themes/classic', file:'style.css', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/widgets', file:'jstree-overrides.css', plugin: 'unifina-core']
	}
	atmosphere {
		dependsOn 'jquery, jquery-migrate'
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
	slimscroll {
		dependsOn 'jquery'
		resource url:[dir:'js/slimScroll-1.3.0/', file:'jquery.slimscroll.min.js', plugin: 'unifina-core']
	}
	'raf-polyfill' {
		resource url:[dir:'js/raf-polyfill', file:'raf-polyfill.js', plugin: 'unifina-core']
	}
	webcomponents {
		resource url:[dir:'js/webcomponentsjs', file:'webcomponents.min.js', plugin: 'unifina-core'], disposition:'head'
	}
	lodash {
		resource url:[dir:'js/lodash-3.10.1', file:'lodash.min.js', plugin: 'unifina-core']
	}
	backbone {
		dependsOn 'lodash,jquery'
		resource url:[dir:'js/backbone', file:'backbone.js', plugin: 'unifina-core']
	}
	'backbone-associations' {
		dependsOn 'backbone'
		resource url:[dir:'js/backbone-associations', file:'backbone-associations-min.js', plugin: 'unifina-core']
	}
	leaflet {
		resource url:[dir:'js/leaflet', file:'leafletGlobalOptions.js', plugin: 'unifina-core']
		resource url:[dir:'js/leaflet', file:'leaflet-src.js', plugin: 'unifina-core']
		resource url:[dir:'js/leaflet', file:'leaflet_canvas_layer.js', plugin: 'unifina-core']
		resource url:[dir:'js/leaflet', file:'leaflet.css', plugin: 'unifina-core']
		resource url:[dir:'js/Leaflet.RotatedMarker', file:'leaflet.rotatedMarker.js', plugin: 'unifina-core']
	}
	dropzone {
		resource url:[dir:'js/dropzone', file:'dropzone.js', plugin: 'unifina-core']
	}
	mathjax {
		resource url:'https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.2.0/MathJax.js?config=TeX-AMS_HTML'
	}
	switcher {
		resource url:[dir:'js/pixel-admin', file:'switcher.js', plugin:'unifina-core']
	}
	// color picker (for search)
	spectrum {
		resource url:[dir:'js/spectrum', file:'spectrum.js', plugin:'unifina-core']
		resource url:[dir:'js/spectrum', file:'spectrum.css', plugin:'unifina-core']
	}
	moment {
		resource url:[dir:'js/moment', file:'moment.js', plugin:'unifina-core']
	}
	'moment-timezone' {
		dependsOn 'moment'
		resource url:[dir:'js/moment', file:'moment-timezone-with-data-2010-2020.js', plugin:'unifina-core']
	}
	clipboardjs {
		resource url:[dir:'js/clipboardjs', file:'clipboard.js', plugin:'unifina-core']
	}

	/**
	 * In-house widgets and resources
	 */
	streamr {
		dependsOn 'pnotify, lodash'
		resource url:[dir:'js/unifina', file:'streamr.js', plugin: 'unifina-core']
	}
	tour {
		dependsOn 'hopscotch, streamr'
		resource url:[dir:'js/unifina/tour', file:'tour.js', plugin: 'unifina-core']
	}
	'dashboard-editor' {
		dependsOn 'backbone, backbone-associations, jquery-ui, streamr, confirm-button, bootstrap, name-editor'
		resource url:[dir:'js/unifina/dashboard', file:'dashboard-editor.js', plugin: 'unifina-core']
	}
	'webcomponent-resources' {
		dependsOn 'streamr-client, streamr-chart, streamr-heatmap, streamr-table, streamr-button, streamr-switcher, streamr-text-field, streamr-map'
	}
	'stream-fields' {
		dependsOn 'jquery, backbone'
		resource url:[dir:'js/unifina/stream-fields', file:'stream-fields.js', plugin: 'unifina-core']
	}
	'streamr-search' {
		dependsOn 'typeahead'
		resource url:[dir:'js/unifina/streamr-search', file:'streamr-search.js', plugin: 'unifina-core']
	}
	'remote-tabs' {
		dependsOn 'bootbox, mustache'
		resource url:[dir:'js/unifina/remote-tabs', file:'remote-tabs.js', plugin: 'unifina-core']
	}
	'sharing-dialog' {
		dependsOn 'bootbox, backbone, switcher'
		resource url: [dir: 'js/unifina/sharing-dialog', file: 'sharing-dialog.js', plugin: 'unifina-core']
	}
	'signalpath-browser' {
		dependsOn 'remote-tabs'
		resource url:[dir:'js/unifina/signalpath-browser', file:'signalpath-browser.js', plugin: 'unifina-core']
	}
	'module-browser' {
		dependsOn 'mathjax, bootstrap, lodash, streamr'
		resource url:[dir:'js/unifina/module-browser', file:'module-browser.js', plugin:'unifina-core']
	}
	'key-value-editor' {
		dependsOn 'bootstrap, backbone, mustache, list-editor'
		resource url:[dir:'js/unifina/key-value-editor', file:'key-value-editor.js', plugin:'unifina-core']
	}
	'list-editor' {
		dependsOn 'bootstrap, backbone, mustache'
		resource url:[dir:'js/unifina/list-editor', file:'list-editor.js', plugin:'unifina-core']
	}
	toolbar {
		dependsOn 'jquery'
		resource url:[dir:'js/unifina/toolbar', file:'toolbar.js', plugin:'unifina-core']
	}
	'global-error-handler' {
		dependsOn 'jquery, bootbox'
		resource url:[dir:'js/unifina', file:'globalJavascriptExceptionHandler.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina', file:'globalAjaxSessionExpirationHandler.js', plugin: 'unifina-core']
	}
	'signalpath-widgets' {
		resource url:[dir:'css/signalPath/widgets', file:'loadBrowser.css', plugin: 'unifina-core']
	}
	'streamr-client' {
		resource url:[dir:'js/unifina/streamr-socketio-client', file:'streamr-client.js', plugin: 'unifina-core']
	}
	'streamr-chart' {
		dependsOn 'jquery,highstock'
		resource url:[dir:'js/unifina/streamr-chart', file:'streamr-chart.js', plugin: 'unifina-core']
	}
	'streamr-button' {
		dependsOn 'jquery'
		resource url:[dir:'js/unifina/streamr-button', file:'streamr-button.js', plugin: 'unifina-core']
	}
	'streamr-switcher' {
		dependsOn 'jquery'
		resource url:[dir:'js/unifina/streamr-switcher', file:'streamr-switcher.js', plugin: 'unifina-core']
	}
	'streamr-text-field' {
		dependsOn 'jquery'
		resource url:[dir:'js/unifina/streamr-text-field', file:'streamr-text-field.js', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/modules', file:'textFieldModule.css', plugin: 'unifina-core']
	}
	'streamr-heatmap' {
		dependsOn 'jquery, leaflet'
		resource url:[dir:'js/unifina/streamr-heatmap', file:'heatmap.min.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/streamr-heatmap', file:'leaflet-heatmap.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/streamr-heatmap', file:'streamr-heatmap.js', plugin: 'unifina-core']
	}
	'streamr-map' {
		dependsOn 'jquery, leaflet, font-awesome'
		resource url:[dir:'css/signalPath/widgets/', file:'streamr-map.css', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/streamr-map', file:'streamr-map.js', plugin: 'unifina-core']
	}
	'streamr-table' {
		resource url:[dir:'js/unifina/streamr-table', file:'streamr-table.js', plugin: 'unifina-core']
	}
	'streamr-credentials-control' {
		dependsOn 'backbone, streamr, clipboardjs, bootbox'
		resource url:[dir:'js/unifina/streamr-credentials-control', file:'streamr-credentials-control.js', plugin:'unifina-core']
	}
	'scrollspy-helper' {
		resource url:[dir:'js/unifina/scrollspy-helper', file:'scrollspy-helper.js', plugin: 'unifina-core']
	}
	'canvas-controls' {
		dependsOn 'signalpath-core, backbone'
		resource url:[dir:'js/unifina/signalPath/controls', file:'canvas-start-button.js', plugin: 'unifina-core']
	}
	'name-editor' {
		resource url:[dir:'js/unifina/streamr-name-editor', file:'streamr-name-editor.js', plugin: 'unifina-core']
	}
	'confirm-button' {
		resource url:[dir:'js/unifina/confirm-button', file:'confirm-button.js', plugin: 'unifina-core']
	}
	'webpack-commons-bundle' {
		resource url:[dir:'js/unifina/webpack-bundles', file:'commons.bundle.js']
	}
	'profile-page-webpack-bundle' {
		dependsOn 'webpack-commons-bundle, confirm-button'
		resource url: [dir: 'js/unifina/webpack-bundles', file: 'profilePage.bundle.js', plugin: 'unifina-core']
		resource url: [dir: 'js/unifina/webpack-bundles', file: 'profilePage.bundle.css', plugin: 'unifina-core']
	}
	'dashboard-page-webpack-bundle' {
		dependsOn 'webpack-commons-bundle, confirm-button'
		resource url: [dir: 'js/unifina/webpack-bundles', file: 'dashboardPage.bundle.js', plugin: 'unifina-core']
		resource url: [dir: 'js/unifina/webpack-bundles', file: 'dashboardPage.bundle.css', plugin: 'unifina-core']
	}
	'signalpath-core' {
		// Easier to merge if dependencies are one-per-row instead of comma-separated list
		dependsOn 'streamr'
		dependsOn 'streamr-client'
		dependsOn 'streamr-chart'
		dependsOn 'streamr-table'
		dependsOn 'streamr-heatmap'
		dependsOn 'streamr-map'
		dependsOn 'streamr-button'
		dependsOn 'streamr-switcher'
		dependsOn 'streamr-text-field'
		dependsOn 'jsplumb'
		dependsOn 'jstree'
		dependsOn 'highstock'
		dependsOn 'codemirror'
		dependsOn 'tablesorter'
		dependsOn 'bootstrap-contextmenu'
		dependsOn 'typeahead'
		dependsOn 'raf-polyfill'
		dependsOn 'signalpath-widgets'
		dependsOn 'mathjax'
		dependsOn 'spectrum'
		dependsOn 'lodash'
		dependsOn 'key-value-editor'
		dependsOn 'list-editor'
		dependsOn 'confirm-button'
		dependsOn 'name-editor'
		resource url:[dir:'js/unifina/signalPath/core', file:'signalPath.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/generic', file:'emptyModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/generic', file:'genericModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/generic', file:'uiChannelModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/core', file:'IOSwitch.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/core', file:'Endpoint.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/core', file:'Input.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/core', file:'VariadicInput.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/core', file:'VariadicOutput.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/core', file:'Parameter.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/core', file:'Output.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'chartModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'heatmapModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'mapModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'imageMapModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'inputModule.js', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/modules', file:'switcherModule.css', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/modules', file:'buttonModule.css', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/modules', file:'chartModule.css', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/modules', file:'eventTable.css', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'gaugeModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'customModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'solidityModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'ethereumContractInput.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'tableModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'commentModule.js', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/modules', file:'commentModule.css', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'labelModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'schedulerModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'scheduler.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'streamModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'filterModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/generic', file:'subCanvasModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'canvasModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'forEachModule.js', plugin: 'unifina-core']
		resource url:[dir:'js/unifina/signalPath/specific', file:'exportCSVModule.js', plugin: 'unifina-core']
	}
	'signalpath-theme' {
		dependsOn 'signalpath-core'
		resource url:[dir:'css/signalPath/themes/light', file:'light.css', plugin: 'unifina-core']
		resource url:[dir:'css/signalPath/themes/light', file:'light.js', plugin: 'unifina-core']
	}
	'main-theme' {
		dependsOn 'bootstrap, font-awesome'
		resource url: "https://fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&subset=latin", attrs: [type: "css"]
		resource url:[dir:'css/compiled-less', file:'main.css', plugin: 'unifina-core']
	}
	'marked' {
		resource url:[dir: 'js/marked/', file: 'marked.min.js', plugin: 'unifina-core']
	}
	'swagger' {
		dependsOn 'jquery, lodash, jquery-migrate, marked'
		resource url:[dir: 'js/swagger-ui/dist/lib/', file: 'jquery.slideto.min.js', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/lib/', file: 'jquery.wiggle.min.js', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/lib/', file: 'jquery.ba-bbq.min.js', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/lib/', file: 'handlebars-2.0.0.js', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/lib/', file: 'backbone-min.js', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/', file: 'swagger-ui.min.js', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/lib/', file: 'highlight.7.3.pack.js', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/lib/', file: 'jsoneditor.min.js', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/lib/', file: 'swagger-oauth.js', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/css/', file: 'reset.css', plugin: 'unifina-core']
		resource url:[dir: 'js/swagger-ui/dist/css/', file: 'screen.css', plugin: 'unifina-core']
	}

}
