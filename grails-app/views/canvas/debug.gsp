
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Launch Live</title>
    </head>
    <body>
<%--        <div class="nav">--%>
<%--        </div>--%>
        
        <div class="body">
            <h1>Debug</h1>
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>


		<h2>Runners</h2>            
		<table>
			<thead>
				<tr>
					<th>RunnerId</th>
					<th>Sessions</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${runners}" status="i" var="entry">
                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td>${entry.value.runnerId}</td>
						<td>${entry.value.returnChannels}</td>
					</tr>
				</g:each>
			</tbody>
		</table>


		<h2>Return channels</h2>            
		<table>
			<thead>
				<tr>
					<th>SessionId</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${returnChannels}" status="i" var="entry">
                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td>${entry.value.sessionId}</td>
					</tr>
				</g:each>
			</tbody>
		</table>

		<h2>Broadcasters</h2>            
		<table>
			<thead>
				<tr>
					<th>toString</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${returnChannels}" status="i" var="bc">
                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td>${bc}</td>
					</tr>
				</g:each>
			</tbody>
		</table>

	</div>
    </body>
</html>
