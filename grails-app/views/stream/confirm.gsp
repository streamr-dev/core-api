<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="stream.confirm.label" /></title>
        
    </head>
    <body>    
		<ui:flashMessage/>

		<ui:panel title="${message(code:"stream.confirm.label")}">
			<g:form action="confirmUpload">
				<g:hiddenField name="id" value="${stream.id}" />
				<g:hiddenField name="file" value="${file}" />
				<div class="form-group">
					<label class="control-label">The column of the timestamp</label>
					<select class="form-control" name="timestampIndex">
						<g:each in="${ schema.headers }" status="i" var="header">
							<option value="${i}">${i+1}: ${header}</option>
						</g:each>
					</select>
				</div>
				<div class="form-group"> 
					<g:hiddenField name="formats" value='${formats = ["yyyy-MM-dd HH:mm:ss.SSS":"yyyy-MM-dd HH:mm:ss.SSS", "dd-MM-yyyy HH:mm:ss.SSS":"dd-MM-yyyy HH:mm:ss.SSS", "MM-dd-yyyy HH:mm:ss.SSS":"MM-dd-yyyy HH:mm:ss.SSS", "unix":"Unix timestamp(milliseconds)"]}'/>
					<label class="control-label">Date standard</label>
					<g:each in="${ formats }" status="i" var="format">
						<div class="radio">
						  <label>
						    <input type="radio" name="format" value="${format.key}" class="px">
						    <span class="lbl">${ format.value }</span>
						  </label>
						</div>
					</g:each>
					<div class="radio">
					  	<label>
					    	<input type="radio" name="format" value="custom" class="px">
					    	<span class="lbl">Custom date format (in java standard)</span>
					    	<input type="text" name="customFormat" class="form-control"></input>
					  	</label>
					</div>
				</div>
				<g:submitButton name="submit" class="save btn btn-primary" value="${message(code: "stream.button.confirm.label")}" />	
			</g:form>
		</ui:panel>
		
    </body>
</html>
