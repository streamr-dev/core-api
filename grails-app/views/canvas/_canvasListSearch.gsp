<div class="form-inline search-form">
	<button type="button" class="filter-toggle-button btn btn-xs btn-default btn-outline running-filter ${stateFilter.contains('running') ? 'active' : ''}" data-state="running" data-toggle="button" aria-pressed="false" autocomplete="off">
		running
	</button>
	<button type="button" class="filter-toggle-button btn btn-xs btn-default btn-outline stopped-filter ${stateFilter.contains('stopped') ? 'active' : ''}" data-state="stopped" data-toggle="button" aria-pressed="false" autocomplete="off">
		stopped
	</button>
	<div class="input-group input-group-sm search-input-group">
		<input id="search-term" name="term" value="${params.term}" placeholder="Search by name" class="form-control" />
		<span class="input-group-btn">
			<button id="search-button" class="btn" type="submit">
				<span class="fa fa-search"></span>
			</button>
		</span>
	</div>
</div>