<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Task worker threads</title>
        
        <script>
        $(document).ready(function() {
            setTimeout("location.reload()",30*1000);
        });
        </script>
    </head>
    <body>
<%--        <div class="nav">--%>
<%--            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>--%>
<%--        </div>--%>
        <div class="body">
            <h1>Task workers</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            
            <div class="list">
                <table id="task-worker-table">
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'worker.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="alive" title="${message(code: 'worker.alive.label', default: 'Alive')}" />
                        
                            <g:sortableColumn property="state" title="${message(code: 'worker.state.label', default: 'State')}" />
                        
                            <th><g:message code="worker.lastKnownTaskId.label" default="Last Task" /></th>
                        
                            <th><g:message code="worker.status.label" default="Status" /></th>
                            
							<th><g:message code="worker.lastError.label" default="Last Error" /></th>
                            
                            <th><g:message code="worker.user.label" default="User" /></th>
                            
                            <th><g:message code="worker.quit.label" default="Quit" /></th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${workers}" status="i" var="worker">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                        	<td>${fieldValue(bean: worker, field: "id")}</td>
                            
                            <td><g:formatBoolean boolean="${worker.isAlive()}" /></td>
                        
                        	<td>${fieldValue(bean: worker, field: "stateCode")}</td>
                        	
                        	<td>${fieldValue(bean: worker, field: "lastKnownTaskId")}</td>
                        	
                        	<td>${fieldValue(bean: worker, field: "lastKnownStatus")}</td>

                        	<td>${fieldValue(bean: worker, field: "lastError")}</td>
                        	
                        	<td>${worker.priorityUser?.username}</td>
                            
                            <td><g:link action="quitWorker" id="${worker.workerId}">Quit</g:link></td>
                            
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                
                <g:form action="startWorker">
                	Priority user: <g:select name="user" from="${users}" noSelection="${['':'(none)']}" optionKey="id" optionValue="username"/>
                	<g:submitButton name="startWorker" value="Start worker thread"/>
                </g:form>
            </div>
        </div>
    </body>
</html>
