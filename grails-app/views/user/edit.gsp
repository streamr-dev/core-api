<html>
<%@ page import="com.unifina.domain.signalpath.ModulePackage" %>

<sec:ifNotSwitched>
	<sec:ifAllGranted roles='ROLE_SWITCH_USER'>
	<g:if test='${user.username}'>
	<g:set var='canRunAs' value='${true}'/>
	</g:if>
	</sec:ifAllGranted>
</sec:ifNotSwitched>

<head>
	<meta name='layout' content='springSecurityUI'/>
	<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
	<title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>

<h3><g:message code="default.edit.label" args="[entityName]"/></h3>

<g:form action="update" name='userEditForm' class="button-style">
<g:hiddenField name="id" value="${user?.id}"/>
<g:hiddenField name="version" value="${user?.version}"/>

<%
def tabData = []
tabData << [name: 'userinfo', icon: 'icon_user', messageCode: 'spring.security.ui.user.info']
tabData << [name: 'roles',    icon: 'icon_role', messageCode: 'spring.security.ui.user.roles']
tabData << [name: 'packages',    icon: 'icon_role', messageCode: 'unifina.modulePackages.label']
boolean isOpenId = grails.util.Holders.getPluginManager().hasGrailsPlugin('springSecurityOpenid')
if (isOpenId) {
	tabData << [name: 'openIds', icon: 'icon_role', messageCode: 'spring.security.ui.user.openIds']
}
%>


		<table>
		<tbody>

			<s2ui:textFieldRow name='username' labelCode='user.username.label' bean="${user}"
                            labelCodeDefault='Username' value="${user?.username}"/>

			<s2ui:textFieldRow name='name' labelCode='user.name.label' bean="${user}"
							   labelCodeDefault='Real Name' value="${user?.name}"/>

			<s2ui:passwordFieldRow name='password' labelCode='user.password.label' bean="${user}"
                                labelCodeDefault='Password' value="${user?.password}"/>

			<s2ui:checkboxRow name='enabled' labelCode='user.enabled.label' bean="${user}"
                           labelCodeDefault='Enabled' value="${user?.enabled}"/>

			<s2ui:checkboxRow name='accountExpired' labelCode='user.accountExpired.label' bean="${user}"
                           labelCodeDefault='Account Expired' value="${user?.accountExpired}"/>

			<s2ui:checkboxRow name='accountLocked' labelCode='user.accountLocked.label' bean="${user}"
                           labelCodeDefault='Account Locked' value="${user?.accountLocked}"/>

			<s2ui:checkboxRow name='passwordExpired' labelCode='user.passwordExpired.label' bean="${user}"
                           labelCodeDefault='Password Expired' value="${user?.passwordExpired}"/>
            
             <s2ui:textFieldRow name='timezone' labelCode='user.timezone.label' bean="${user}" 
                            labelCodeDefault='Timezone' value="${user?.timezone}"/>
                           
            <g:render template="/user/projectExtras" model="[user:user]"/>
		</tbody>
		</table>

	<h2>Roles</h2>
		<g:each var="auth" in="${authorityList}">
			<div>
				<g:checkBox name="role" value="${auth.authority}" checked="${user.authorities.contains(auth)}" />
				<g:link controller='role' action='edit' id='${auth.id}'>${auth.authority.encodeAsHTML()}</g:link>
			</div>
		</g:each>

	<h2>Module Packages</h2>
		<g:each var="p" in="${ModulePackage.list()}">
		<div>
			<g:checkBox name="modulePackage" value="${p.id}" checked="${userModulePackages.contains(p)}" />
			${p.name.encodeAsHTML()}
		</div>
		</g:each>

	<g:if test='${isOpenId}'>
	<g:if test='${user?.openIds}'>
		<ul>
		<g:each var="openId" in="${user.openIds}">
		<li>${openId.url}</li>
		</g:each>
		</ul>
	</g:if>
	<g:else>
	No OpenIDs registered
	</g:else>
	</g:if>


<div style='float:left; margin-top: 10px;'>
<s2ui:submitButton elementId='update' form='userEditForm' messageCode='default.button.update.label'/>

<g:if test='${canRunAs}'>
<a id="runAsButton">${message(code:'spring.security.ui.runas.submit')}</a>
</g:if>

</div>

</g:form>

<g:if test='${user}'>
<s2ui:deleteButtonForm instanceId='${user.id}'/>
</g:if>

<g:if test='${canRunAs}'>
	<form name='runAsForm' action='${request.contextPath}/j_spring_security_switch_user' method='POST'>
		<g:hiddenField name='j_username' value="${user.username}"/>
		<input type='submit' class='s2ui_hidden_button' />
	</form>
</g:if>

</body>
</html>
