<html>
    <head>
        <meta name="layout" content="main" />
        <title>Available modules</title>
        
        <r:script>
            var spinnerImg = "${resource(dir:'images', file: 'spinner.gif') }"
            var CKEDITORConfigUrl = "${resource(dir:'js/ckeditor-config') }"
        	var moduleBrowser = new ModuleBrowser({
        		sidebarEl: $("#sidebar"),
        		moduleTreeEl: $("#module-help-tree"),
                searchBoxEl: "#moduleSearch"
        	})
        </r:script>
        
        <r:require module="module-browser" />
    </head>
    <body class="module-list">
    
    	<ui:flashMessage/>

        <ui:breadcrumb>
            <g:render template="/module/breadcrumbList"/>
        </ui:breadcrumb>
    	
    	<div class="row">
    		<div class="col-sm-12 col-md-8 col-md-offset-2">
				<div class="panel col-md-9">
					<div class="panel-body" id="module-help-tree">

					</div>
				</div>
                <div class="col-xs-0 col-sm-0 col-md-3">
                    <div id="sidebar">
                        <div class="hidden-sm hidden-xs moduleSearch">
                            <input id="moduleSearch" type="text" class="form-control input-md" placeholder="Search..." />
                            <span class="search-message" style="margin-top:10px;"></span>
                        </div>
                    </div>
                </div>

        	</div>
        </div>
    </body>
</html>
