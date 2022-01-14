databaseChangeLog = {
	include file: 'core/2020-11-23-rename-subscription-classes.groovy'
	include file: 'core/2020-12-18-remove-user-password.groovy'
	include file: 'core/2021-04-15-remove-dashboard-item.groovy'
	include file: 'core/2021-04-15-remove-module-category.groovy'
	include file: 'core/2021-04-15-remove-serialization.groovy'
	include file: 'core/2021-04-20-remove-hostconfig.groovy'
	include file: 'core/2021-04-20-remove-task.groovy'
	include file: 'core/2021-04-20-remove-canvas-dashboard-module.groovy'
	include file: 'core/2021-05-14-stream-fk-storage-node.groovy'
	include file: 'core/2021-10-04-stream-fulltext-search.groovy'
	include file: 'core/2021-10-06-permission-index.groovy'
	include file: 'core/2021-06-10-user-is-ethereum-address-rm-integration-key.groovy'
	include file: 'core/2021-11-01-delete-integration-key.groovy'
	include file: 'core/2021-11-02-unique-username-index.groovy'
	include file: 'core/2021-11-19-rm-data-union-versions.groovy'
	include file: 'core/2021-12-15-rm-registration-code.groovy'
	include file: 'core/2022-01-13-add-stream-migration-fields.groovy'
	include file: 'core/2021-11-22-add-product-chain-field.groovy'
	include file: 'core/2021-12-27-rm-stream.groovy'
}
