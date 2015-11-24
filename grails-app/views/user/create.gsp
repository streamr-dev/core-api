<%@ page import="com.unifina.domain.signalpath.ModulePackage" %>
<%@ page import="com.unifina.domain.data.Feed" %>

<html>

<head>
	<meta name='layout' content='springSecurityUI'/>
	<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
	<title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>

<h3><g:message code="default.create.label" args="[entityName]"/></h3>

<g:form action="save" name='userCreateForm'>

<%
def tabData = []
tabData << [name: 'userinfo', icon: 'icon_user', messageCode: 'spring.security.ui.user.info']
tabData << [name: 'roles',    icon: 'icon_role', messageCode: 'spring.security.ui.user.roles']
tabData << [name: 'packages',    icon: 'icon_role', messageCode: 'unifina.modulePackages.label']
tabData << [name: 'feeds',    icon: 'icon_role', messageCode: 'unifina.feeds.label']
%>

<s2ui:tabs elementId='tabs' height='375' data="${tabData}">

	<s2ui:tab name='userinfo' height='280'>
		<table>
		<tbody>

			<s2ui:textFieldRow name='username' labelCode='user.username.label' bean="${user}"
                            labelCodeDefault='Username' value="${user?.username}"/>

			<s2ui:textFieldRow name='name' labelCode='user.name.label' bean="${user}"
							   labelCodeDefault='Real Name' value="${user?.name}"/>

			<s2ui:passwordFieldRow name='password' labelCode='user.password.label' bean="${user}"
                                labelCodeDefault='Password' value="${user?.password}"/>

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
	</s2ui:tab>

	<s2ui:tab name='roles' height='280'>
		<g:each var="auth" in="${authorityList}">
		<div>
			<g:checkBox name="role" value="${auth.authority}" checked="false" />
			<g:link controller='role' action='edit' id='${auth.id}'>${auth.authority.encodeAsHTML()}</g:link>
		</div>
		</g:each>
	</s2ui:tab>

	<s2ui:tab name='packages' height='280'>
		<g:each var="p" in="${ModulePackage.list()}">
		<div>
			<g:checkBox name="modulePackage" value="${p.id}" checked="false"/>
			${p.name.encodeAsHTML()}
		</div>
		</g:each>
	</s2ui:tab>
	
	<s2ui:tab name='feeds' height='280'>
		<g:each var="f" in="${Feed.list()}">
		<div>
			<g:checkBox name="feed" value="${f.id}" checked="false"/>
			${f.name?.encodeAsHTML() ?: f.id}
		</div>
		</g:each>
	</s2ui:tab>

</s2ui:tabs>

<div style='float:left; margin-top: 10px; '>
<s2ui:submitButton elementId='create' form='userCreateForm' messageCode='default.button.create.label'/>
</div>

</g:form>

<script>
$(document).ready(function() {
	$('#username').focus();
	<s2ui:initCheckboxes/>
});
</script>

</body>
</html>
