package core
databaseChangeLog = {
	changeSet(author: "haanpuu", id: "2017-06-01-dashboard-id-to-string-1") {
		sql("""

SET @name = (
	SELECT DISTINCT CONSTRAINT_NAME
	FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
	WHERE REFERENCED_TABLE_NAME = 'dashboard' 
	AND 
	TABLE_NAME = 'dashboard_item'
	AND
	TABLE_SCHEMA = (SELECT DATABASE())
);
SET @query = CONCAT('
ALTER TABLE dashboard_item DROP FOREIGN KEY ', @name);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @query = CONCAT('ALTER TABLE dashboard_item DROP INDEX ', @name);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE dashboard MODIFY COLUMN id VARCHAR(255);
ALTER TABLE dashboard_item MODIFY COLUMN id VARCHAR(255);
ALTER TABLE dashboard_item MODIFY COLUMN dashboard_id VARCHAR(255);

ALTER TABLE dashboard_item 
ADD CONSTRAINT fk_dashboard_item_dashboard_id 
FOREIGN KEY (dashboard_id) 
REFERENCES dashboard(id);
		""")
	}
}

