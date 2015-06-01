<html>
    <head>
        <meta name="layout" content="main" />
        <title>Available modules</title>
        
        <r:script>
        	var moduleHelpUrl = "${createLink(action: 'jsonGetModuleHelp')}"
        	$.getJSON("${ createLink(action:'jsonGetModuleTree') }", {}, function(data){
        		renderSidebar($("#sidebar"), data)
        		renderModules($("#module-help-tree"), data)
        		var offset = 70
        		$('body').scrollspy({
        			offset: offset
        		})
        		$('#sidebar li a').click(function(event) {
        			event.preventDefault()
				    $($(this).attr('href'))[0].scrollIntoView()
				    scrollBy(0, -(offset-30))
                    this.blur()
				});
        	})
        </r:script>
        
        <r:require module="module-browser" />
    </head>
    <body class="module-list">
    
    	<ui:flashMessage/>
    	
    	<div class="row">
    		<div class="col-sm-12 col-md-8 col-md-offset-2">
		        <div class="col-md-9" id="module-help-tree">
		     		<h1 class="title">All modules by category</h1>
		        </div>
		        <div class="col-xs-0 col-sm-0 col-md-3" id="sidebar">
		        	
		        </div>
        	</div>
        </div>
    </body>
</html>
