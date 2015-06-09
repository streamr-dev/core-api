<html>
    <head>
        <meta name="layout" content="main" />
        <title>Available modules</title>
        
        <r:script>
        	var moduleBrowser = new ModuleBrowser({
        		url: "${ createLink(uri:"/") }module",
        		sidebarEl: $("#sidebar"),
        		moduleTreeEl: $("#module-help-tree"),
                searchBoxEl: "#moduleSearch"
        	})
        </r:script>
        
        <r:require module="module-browser" />
    </head>
    <body class="module-list">
    
    	<ui:flashMessage/>
    	
    	<div class="row">
    		<div class="col-sm-12 col-md-8 col-md-offset-2">
                <input id="moduleSearch" type="search" class="form-control input-sm"placeholder="Search..." style="position:fixed;right:30px;top:60px;height:35px;width:200px;"/>
		        <div class="col-md-9" id="module-help-tree">
		     		<h1 class="title">All modules by category</h1>
		        </div>
		        <div class="col-xs-0 col-sm-0 col-md-3" id="sidebar">
		        	
		        </div>
        	</div>
        </div>
    </body>
</html>
