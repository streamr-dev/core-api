<%@ page import="grails.converters.JSON" %>
<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title>Canvas</title>

		<r:require module='jstree'/>
		<r:require module="bootbox"/>
		<r:require module="bootstrap-contextmenu"/>
		<r:require module="slimscroll"/>
		<r:require module="streamr-search"/>
		<r:require module="signalpath-browser"/>
		<r:require module="signalpath-theme"/>
		<r:require module="hotkeys"/>
		<r:require module="touchpunch"/>
		<r:require module="sharing-dialog"/>
		<r:require module="canvas-controls"/>
		<r:require module="bootstrap-datepicker"/>

		<r:script>

// Make the loadBrowser global to allow apps that use this plugin to extend it by adding tabs
	var loadBrowser
	var saveAndAskName

$(function() {
	$('#moduleTree').bind('loaded.jstree', function() {
		Tour.startableTours([0])
		Tour.autoStart()
	})
	saveAsAndAskName = function() {
		bootbox.prompt({
			title: 'Save As..',
			callback: function(saveAsName) {
				if (!saveAsName)
					return;

				SignalPath.saveAs(saveAsName)
			},
			value: SignalPath.getName(),
			className: 'save-as-name-dialog'
		})
	}
	$("body").keydown(function(e) {
		// ctrl + shift + s || cmd + shift + s
		if (e.shiftKey && (e.ctrlKey || e.metaKey)) {
			if (String.fromCharCode(e.which).toLowerCase() == 's') {
				e.preventDefault()
				saveAsAndAskName()
			}
		}
		// ctrl || cmd
		else if (e.ctrlKey || e.metaKey) {
			switch (String.fromCharCode(e.which).toLowerCase()) {
				case 's':
					e.preventDefault()
					if (!SignalPath.isSaved()) {
						saveAsAndAskName()
					} else {
						SignalPath.save()
					}
					break;
				case 'o':
					e.preventDefault()
					loadBrowser.modal()
					break;
			}
		}
		// alt + r
		else if (e.altKey) {
			if (String.fromCharCode(e.which).toLowerCase() == 'r') {
				e.preventDefault()
				if (!SignalPath.isRunning()) {
					SignalPath.start();
				} else {
					SignalPath.stop()
				}
			}
		}
	})

	function settings() {
		return {
			beginDate: $("#beginDate").val(),
			endDate: $("#endDate").val(),
			speed: $("#speed").val(),
			timeOfDayFilter: {
				timeOfDayStart: $("#timeOfDayStart").val(),
				timeOfDayEnd: $("#timeOfDayEnd").val(),
			},
			editorState: {
				runTab: $("#tab-historical").is(":visible") ? "#tab-historical" : "#tab-realtime"
			}
		}
	}

	SignalPath.init({
		parentElement: $('#canvas'),
		settings: settings,
		errorHandler: function(data) {
			Streamr.showError(data.msg)
		},
		notificationHandler: function(data) {
			Streamr.showInfo(data.msg)
		},
		connectionOptions: {
			url: "${grailsApplication.config.streamr.ui.server}",
			path: "${grailsApplication.config.streamr.ui.serverPath}",
			authKey: "${key.id}",
			autoConnect: true,
			autoDisconnect: true
		}
	});

	$(SignalPath).on('loading', function() {
		$('#modal-spinner').show()
	})

	$(SignalPath).on('loaded', function(event, json) {
		$('#modal-spinner').hide()

		var settings = json.settings
		if (settings.beginDate) {
			$("#beginDate").val(settings.beginDate).trigger("change")
		}
		if (settings.endDate)
			$("#endDate").val(settings.endDate).trigger("change")

		if (settings.timeOfDayFilter) {
			$("#timeOfDayStart").val(settings.timeOfDayFilter.timeOfDayStart).trigger("change")
			$("#timeOfDayEnd").val(settings.timeOfDayFilter.timeOfDayEnd).trigger("change")
		}

		$("#speed").val(settings.speed!=null ? settings.speed : 0).trigger("change")

		if (settings.editorState && settings.editorState.runTab)
			$("a[href="+settings.editorState.runTab+"]").tab('show')

		if (SignalPath.isRunning()) {
			// Show realtime tab when a running SignalPath is loaded
			$("a[href=#tab-realtime]").tab('show')

			// Try to ping a running SignalPath on load, and show error if it can't be reached
			SignalPath.runtimeRequest(SignalPath.getRuntimeRequestURL(), {type:"ping"}, function(response, err) {
				if (err)
					Streamr.showError('${message(code:'canvas.ping.error')}')
			})
		}
		setAddressbarUrl(Streamr.createLink({controller: "canvas", action: "editor", id: json.id}))
	});

	$(SignalPath).on('error', function(error) {
		console.error(error)
		$('#modal-spinner').hide()
	})

	$(SignalPath).on('saving', function() {
		$('#modal-spinner').show()
	})

	$(SignalPath).on('saved', function(event, savedJson) {
		$('#modal-spinner').hide()
		Streamr.showSuccess('${message(code:"signalpath.saved")}: '+savedJson.name)
		setAddressbarUrl(Streamr.createLink({controller: "canvas", action: "editor", id: savedJson.id}))
	})

	function setAddressbarUrl(url) {
		if (window.history && window.history.pushState && window.history.replaceState) {
			// If we haven't set the current url into history, replace the current state so we know to reload the page on back
			if (!window.history.state || !window.history.state.streamr) {
				window.history.replaceState({
					streamr: {
						urlPath: window.location.href
					}
				}, undefined, window.location.href)
			}
			// Push the new state to the history
			if (url !== window.location.href) {
				window.history.pushState({
					streamr: {
						urlPath: url
					}
				}, undefined, url)
			}
		}
	}

	window.onpopstate = function(e) {
		if (e.state && e.state.streamr && e.state.streamr.urlPath) {
			// location.reload() doesn't work because the event is fired before the location change
			window.location = e.state.streamr.urlPath
		}
	}

	// Streamr search for modules and streams
	var streamrSearch = new StreamrSearch('#search', [{
		name: "module",
		limit: 5
	}, {
		name: "stream",
		limit: 3
	}], {
		inBody: true
	}, function(item) {

		if (item.resultType == "stream") { // is stream, specifies module
			SignalPath.addModule(item.feed.module, {
				params: [{
					name: 'stream',
					value: item.id
				}]
			})
		} else { // is module
			SignalPath.addModule(item.id, {})
		}
	})

    $('#main-menu-inner').scroll(function() {
    	streamrSearch.redrawMenu()
    })

	$(document).bind('keydown', 'alt+s', function(e) {
		$("#search").focus()
		e.preventDefault()
	})

	// Bind slimScroll to main menu
    $('#main-menu-inner').slimScroll({
      	height: '100%'
    })

	loadBrowser = new SignalPathBrowser()
		.tab('My Canvases', '${ createLink(controller: "canvas", \
			action: "loadBrowser", params: [ browserId: "archiveLoadBrowser" ]) }')

		.tab('Examples', '${ createLink(controller: "canvas", \
			action: "loadBrowser", params: [ browserId: "examplesLoadBrowser" ]) }')
			
		.onSelect(function(id) {
			SignalPath.load(id)
		})

	<%-- Show examples loader if requested --%>
	<g:if test="${examples}">
		// Use a timeout to allow extensions to register their tabs before the loadBrowser is shown
		setTimeout(function() {
			loadBrowser.modal()
			loadBrowser.show("Examples")
		}, 0)
	</g:if>

	$('#newSignalPath').click(function() {
		SignalPath.clear()
	})

	$('#loadSignalPath').click(function() {
		loadBrowser.modal()
	})

	$(document).bind('keyup', 'alt+r', function() {
		SignalPath.start();
	});

	// Historical run button
	var historicalRunButton = new CanvasStartButton({
		el: $("#run-historical-button"),
		signalPath: SignalPath,
        settings: settings,
        startContent: '<i class="fa fa-play"></i> Run',
        stopContent: '<i class="fa fa-spin fa-spinner"></i> Abort',
        adhoc: true,
        clearState: true
	})

	// Realtime run button
	var realtimeRunButton = new CanvasStartButton({
		el: $("#run-realtime-button"),
		signalPath: SignalPath,
        settings: settings,
        startContent: '<i class="fa fa-play"></i> Start',
        stopContent: '<i class="fa fa-stop"></i> Stop',
        adhoc: false,
        clearState: false
	})
	realtimeRunButton.on('start-confirmed', function() {
		Streamr.showSuccess('${message(code:"canvas.started")}'.replace('{0}', SignalPath.getName()))
	})
	realtimeRunButton.on('start-error', function(err) {
		var msg = '${message(code:"canvas.start.error")}'
		if (err && err.code == "FORBIDDEN") {
			msg = '${message(code:"canvas.start.forbidden")}'
		}
		Streamr.showError(msg)
	})
	realtimeRunButton.on('stop-confirmed', function() {
		Streamr.showSuccess('${message(code:"canvas.stopped")}'.replace('{0}', SignalPath.getName()))
	})
	realtimeRunButton.on('stop-error', function(err) {
		var msg = '${message(code:"canvas.stop.error")}'
		if (err && err.code == "FORBIDDEN") {
			msg = '${message(code:"canvas.stop.forbidden")}'
		}
		Streamr.showError(msg)
	})

	// Run and clear link
	var realtimeRunAndClearButton = new CanvasStartButton({
		el: $("#run-realtime-button"),
		signalPath: SignalPath,
        settings: settings,
        startContent: '<i class="fa fa-play"></i> Start',
        stopContent: '<i class="fa fa-stop"></i> Stop',
        adhoc: false,
        clearState: true,
        clickElement: $("#run-realtime-clear")
	})
	realtimeRunAndClearButton.on('start-confirmed', function() {
		Streamr.showSuccess('${message(code:"canvas.clearAndStarted")}: '.replace('{0}', SignalPath.getName()))
	})

	var nameEditor = new StreamrNameEditor({
		el: $(".name-editor"),
		signalPath: SignalPath,
		opener: $(".rename-canvas-button"),
		name: SignalPath.getName()
	}).on('changed', function(name) {
	    var oldName = SignalPath.getName()
	    if (!SignalPath.isSaved()) {
			SignalPath.saveAs(name)
	    } else {
			SignalPath.saveName(name, function() {}, function() {
				console.log("error")
				// calling silent to prevent event loop
				nameEditor.setName(oldName, {
					silent: true
				})
			})
	    }
	})

	$(".streamr-dropdown li.disabled").click(function(e) {
		e.preventDefault()
	})

	function streamrDropDownSetEnabled(buttons) {
	    var buttonReference = {
	        'share': '.share-button',
	        'rename': '.rename-canvas-button',
	        'delete': '.delete-canvas-button'
	    }
	    var list = Array.isArray(buttons) ? buttons : buttons.split(/[ ,]/)
		for (var btn in buttonReference) {
	        if (list.indexOf(btn) < 0) {
	            $(buttonReference[btn]).addClass('disabled').addClass('forbidden').attr('disabled', 'disabled')
	        } else {
	            $(buttonReference[btn]).removeClass('disabled').removeClass('forbidden').removeAttr('disabled')
	        }
		}
	}

	$(SignalPath).on('new', function() {
	    streamrDropDownSetEnabled('rename')
	    nameEditor.setName(SignalPath.getName(), {
	        silent: true
	    })
		setAddressbarUrl(Streamr.createLink({
		    controller: "canvas",
		    action: "editor"
		}))
	})

	var shareUrl
	var shareName
	$(".share-button").click(function(e) {
	    e.preventDefault()
	    if ($(this).data('url')) {
			sharePopup(shareUrl, shareName)
		}
	})

	$(SignalPath).on('loaded saved', function(e, json) {
		nameEditor.update(json)
		if (!SignalPath.isReadOnly()) {
			var canvasUrl = Streamr.createLink({uri: "api/v1/canvases/" + json.id})
			$.getJSON(canvasUrl + "/permissions/me", function(perm) {
			    var enabled = _.map(perm, function(p) {
			        return p.operation
			    })
				if (enabled.indexOf('share') >= 0) {
					$("#share-button").data("url", canvasUrl).removeAttr("disabled")
				} else {
					$("#share-button").addClass("forbidden")
				}
				if (enabled.indexOf('write') >= 0) {
					enabled.push('delete')
					enabled.push('rename')
				}
                streamrDropDownSetEnabled(enabled)
			})
		}
	})

	<g:if test="${id}">
		SignalPath.load('${id}');
	</g:if>
	<g:elseif test="${json}">
		SignalPath.loadJSON(${raw(json)})
	</g:elseif>

    $(document).unload(function () {
        SignalPath.unload()
    })

	new ConfirmButton("#delete-canvas-button", {
		title: "${ message(code: 'canvas.delete.title') }",
		message: "${ message(code: 'canvas.delete.confirm') }"
	}, function(result) {
		if (result) {
			$.ajax("${ createLink(uri:"/api/v1/canvases", absolute: true)}" + '/' + SignalPath.getId(), {
				method: 'DELETE',
				success: function() {
					Streamr.showSuccess("${ message(code: 'canvas.delete.success') }")
					SignalPath.clear()
				},
				error: function(e, t, msg) {
					Streamr.showError("${ message(code: 'canvas.delete.error') }", "${ message(code: 'canvas.delete.error.title') }")
				}
			})
		}
	})

	$(SignalPath).trigger('new') // For event listeners
})

</r:script>

<!-- mustache templates -->
<g:render template="/templates/remote-tabs" plugin="unifina-core"/>

<!-- extension point for apps using the core plugin -->
<g:render template="/canvas/signalPathExtensions"/>
<g:render template="/canvas/buildHeadExtensions"/>

</head>

<body class="canvas-editor-page main-menu-fixed ${embedded ? 'embedded' : ''}">
	<div id="main-menu" role="navigation">
		<div id="main-menu-inner">
			<div id="toolbar-buttons" class="menu-content" style="overflow: visible;">
				<div class="btn-group load-save-group">
					<button id="newSignalPath" title="New Canvas" class="btn btn-default">
						<i class="fa fa-file-o"></i>
					</button>

					<button id="loadSignalPath" title="Load Canvas" class="btn btn-default">
						<i class="fa fa-folder-open"></i>
					</button>

					<sp:saveButtonDropdown/>
				</div>
			</div>

			<div class="menu-content">

				<ul class="nav nav-tabs nav-justified nav-tabs-xs run-mode-tabs">
					<li class="active">
						<div class="nav-tab-white-background nav-tab-background"></div>
						<div class="nav-tab-orange-background nav-tab-background"></div>
						<a href="#tab-historical" role="tab" data-toggle="tab">Historical</a>
					</li>
					<li class="">
						<div class="nav-tab-white-background nav-tab-background"></div>
						<div class="nav-tab-orange-background nav-tab-background"></div>
						<a href="#tab-realtime" id="open-realtime-tab-link" role="tab" data-toggle="tab">Realtime</a>
					</li>
				</ul>

				<div class="tab-content">
					<!-- Historical run controls -->
					<div role="tabpanel" class="tab-pane active" id="tab-historical">

						<form onsubmit="return false;">
							<g:hiddenField name="defaultBeginDate" value="${formatDate(date:beginDate,format:"yyyy-MM-dd")}"/>
							<g:hiddenField name="defaultEndDate" value="${formatDate(date:endDate,format:"yyyy-MM-dd")}"/>

							<div class="form-group form-group-period">
								<div class="input-group">
									<span class="input-group-addon">From</span>
									<ui:datePicker name="beginDate" value="${beginDate}" class="form-control"/>
								</div>
								<div class="input-group">
									<span class="input-group-addon">To</span>
									<ui:datePicker name="endDate" value="${endDate}" class="form-control"/>
								</div>
								<a href="#" id="historical-options-button" class="btn btn-primary btn-outline dark btn-xs pull-right" title="Historical Run Options" data-toggle="modal" data-target="#historicalOptionsModal">
									<i class="fa fa-cog"></i>
									Options
								</a>
							</div>

							<div class="btn-group btn-block run-group">
								<button id="run-historical-button" class="btn btn-primary col-xs-12 run-button">
									<i class="fa fa-play"></i>
									Run
								</button>
							</div>
						</form>
					</div>

					<!-- Realtime run controls -->
					<div role="tabpanel" class="tab-pane" id="tab-realtime">
						<div class="menu-content-header">
							%{--Uncomment and remove &nbsp; when gets content--}%
							&nbsp;
							%{--<!--<label>Realtime Run Options</label>-->--}%
							%{--<a href="#" id="realtime-options-button" class="btn btn-primary btn-outline dark btn-xs pull-right" title="Realtime Run Options" data-toggle="modal" data-target="#realtimeOptionsModal">--}%
								%{--<i class="fa fa-cog"></i>--}%
								%{--Options--}%
							%{--</a>--}%
						</div>
						<div class="btn-group btn-block run-group">
							<button id="run-realtime-button" class="btn btn-primary col-xs-10 run-button">
								<i class="fa fa-play"></i>
								<g:message code="canvas.start.label"/>
							</button>
							<button id="runDropdown" type="button" class="btn btn-primary col-xs-2 dropdown-toggle"
									data-toggle="dropdown">
								<span class="caret"></span>
								<span class="sr-only">Toggle Dropdown</span>
							</button>
							<ul class="dropdown-menu" role="menu">
								<li><a id="run-realtime-clear" href="#"><g:message code="canvas.clearAndStart.label"/></a></li>
							</ul>
						</div>
					</div>
				</div>
			</div>
			
			<div id="search-control" class="menu-content" style="overflow: visible">
				<label for="search">Add Stream / Module</label><br>
				<input type="text" class="typeahead form-control" id="search" placeholder="Type to search"/>
			</div>

			<div class="menu-content">
				<div class="menu-content-header">
					<label for="moduleTree">Module Browser</label>
				</div>

				<sp:moduleBrowser id="moduleTree" buttonId="addModule" />

				<sp:moduleAddButton buttonId="addModule" browserId="moduleTree" class="btn-block">
					<i class="fa fa-plus"></i>
					<g:message code="canvas.addModule.label" />
				</sp:moduleAddButton>
			</div>

			<div class="menu-content">
				<button id="share-button" class="btn share-button btn-block">
					<i class='fa fa-user'></i> Share
				</button>
			</div>

		</div> <!-- / #main-menu-inner -->
	</div> <!-- / #main-menu -->


	<div id="content-wrapper">
		<ui:breadcrumb>
			<li>
				<g:link controller="canvas" action="list">
					<g:message code="canvas.list.label"/>
				</g:link>
			</li>
			<li class="active">
				<span class="name-editor"></span>
			</li>
			<div class="streamr-dropdown">
				<button class="canvas-menu-toggle dropdown-toggle btn btn-xs btn-outline" data-toggle="dropdown">
					<i class="fa fa-cog"></i> <i class="navbar-icon fa fa-caret-down"></i>
				</button>
				<ul class="dropdown-menu">
					<li class="share-canvas-button share-button">
						<a id="share-canvas-button">
							<i class='fa fa-user'></i> Share
						</a>
					</li>
					<li class="rename-canvas-button">
						<a id="rename-canvas-button">
							<i class="fa fa-pencil"></i> Rename
						</a>
					</li>
					<li class="delete-canvas-button">
						<a id="delete-canvas-button">
							<i class="fa fa-trash-o"></i> Delete
						</a>
					</li>
				</ul>
			</div>
		</ui:breadcrumb>

		<div id="canvas" class="streamr-canvas scrollable embeddable"></div>
	</div>

	<div id="main-menu-bg"></div>
	
	<div id="historicalOptionsModal" class="modal fade">
	  <div class="modal-dialog">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	        <h4 class="modal-title">Historical Run Options</h4>
	      </div>
	      <div class="modal-body">
				<div class="form-group">
					<label>Speed</label>
					<select id="speed" class="form-control">
						<option value="0">Full</option>
						<option value="1">1x</option>
						<option value="10">10x</option>
						<option value="100">100x</option>
						<option value="1000">1000x</option>
					</select>
				</div>
				
				<div class="form-group">
					<label>Speed time of day</label>
					<input id="timeOfDayStart" type="text" name="timeOfDayStart" value="00:00:00" class="form-control">
					<input id="timeOfDayEnd" type="text" name="timeOfDayEnd" value="23:59:00" class="form-control">
				</div>

	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>
	      </div>
	    </div><!-- /.modal-content -->
	  </div><!-- /.modal-dialog -->
	</div><!-- /.modal -->

	%{--Uncomment when gets content--}%

	%{--<div id="realtimeOptionsModal" class="modal fade">--}%
		%{--<div class="modal-dialog">--}%
			%{--<div class="modal-content">--}%
				%{--<div class="modal-header">--}%
					%{--<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>--}%
					%{--<h4 class="modal-title">Realtime Run Options</h4>--}%
				%{--</div>--}%
				%{--<div class="modal-body">--}%

				%{--</div>--}%
				%{--<div class="modal-footer">--}%
					%{--<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>--}%
				%{--</div>--}%
			%{--</div><!-- /.modal-content -->--}%
		%{--</div><!-- /.modal-dialog -->--}%
	%{--</div><!-- /.modal -->--}%

	<ul id="save-dropdown-menu" class="dropdown-menu" role="menu">
		<li><a href="#" id="saveButton">Save</a></li>
		<li><a href="#" id="saveAsButton">Save as..</a></li>
	</ul>
	
	<!-- extension point for apps using the core plugin -->
	<g:render template="/canvas/buildBodyExtensions"/>

</body>
</html>

