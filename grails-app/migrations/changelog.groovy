databaseChangeLog = {
	include file: 'core/2016-01-12-initial-db-state.groovy'

	include file: 'core/2016-01-28-streamr-map-module.groovy'
	include file: 'core/2016-02-05-color-modules.groovy'
	include file: 'core/2016-02-04-rate-limit-module.groovy'
	include file: 'core/2016-02-04-stream-module-js-module-change.groovy'

	include file: 'core/2016-01-13-input-modules-added.groovy'
	include file: 'core/2016-01-13-api-feature.groovy'
	include file: 'core/2016-01-21-replace-running-and-saved-signal-paths-with-canvas.groovy'

	include file: 'core/2016-02-02-mongodb-feed.groovy'
	include file: 'core/2016-02-25-feed-data-range-provider.groovy'

	include file: 'core/2016-01-13-permission-feature.groovy'
	include file: 'core/2016-02-09-permissions-for-signupinvites.groovy'
	include file: 'core/2016-02-10-remove-feeduser-modulepackageuser.groovy'
	include file: 'core/2016-02-19-add-sharespec-test-data.groovy'
	include file: 'core/2016-03-03-add-anonymous-access.groovy'
}
