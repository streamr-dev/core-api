<ui:labeled label="${message(code: "stream.name.label")}">
	<input name="name" type="text" class="form-control input-lg" value="${stream?.name}" required>

	<g:hasErrors bean="${stream}" field="name">
		<span class="text-danger">
			<g:renderErrors bean="${stream}" field="name" as="list" />
		</span>
	</g:hasErrors>
</ui:labeled>

<ui:labeled label="${message(code: "stream.description.label")}">
	<input name="description" type="text" class="form-control input-lg" value="${stream?.description}">

	<g:hasErrors bean="${stream}" field="description">
		<span class="text-danger">
			<g:renderErrors bean="${stream}" field="description" as="list" />
		</span>
	</g:hasErrors>
</ui:labeled>

<ui:labeled label="${message(code: 'stream.feed.label')}">
	<g:select
			class="form-control input-lg"
			name="feed"
			optionValue="name"
			optionKey="id"
			from="${feeds}"
			value="${defaultFeed.id}"
	/>

	<g:hasErrors bean="${stream}" field="feed">
		<span class="text-danger">
			<g:renderErrors bean="${stream}" field="feed" as="list"/>
		</span>
	</g:hasErrors>
</ui:labeled>