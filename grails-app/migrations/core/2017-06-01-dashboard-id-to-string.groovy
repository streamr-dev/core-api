package core

databaseChangeLog = {
	changeSet(author: "haanpuu", id: "2017-06-01-dashboard-id-to-string-1") {
		sql(
/* 1. First ask the name of the foreign key in table 'dashboard' referencing table 'dashboard_item', and store in to variable @name */
				"""SET @name = (
	SELECT DISTINCT CONSTRAINT_NAME
	FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
	WHERE REFERENCED_TABLE_NAME = 'dashboard' 
	AND 
	TABLE_NAME = 'dashboard_item'
	AND
	TABLE_SCHEMA = (SELECT DATABASE())
);""" +
/* 2. Drop the foreign key (must be done like this because of the variable) */
						"""				
SET @query = CONCAT('
ALTER TABLE dashboard_item DROP FOREIGN KEY ', @name);
PREPARE stmt FROM @query;
EXECUTE stmt; 
DEALLOCATE PREPARE stmt;""" +
/* 3. Drop the index */
						"""
SET @query = CONCAT('ALTER TABLE dashboard_item DROP INDEX ', @name);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;"""+
/* 4. Change 'dashboard' and 'dashboard_item' columns (and foreign key) to be strings */
						"""
ALTER TABLE dashboard MODIFY COLUMN id VARCHAR(255);
ALTER TABLE dashboard_item MODIFY COLUMN id VARCHAR(255);
ALTER TABLE dashboard_item MODIFY COLUMN dashboard_id VARCHAR(255);""" +
/* Relink the foreign key referencing from 'dashboard_item.dashboard_id' to 'dashboard.id' */
						"""
ALTER TABLE dashboard_item 
ADD CONSTRAINT fk_dashboard_item_dashboard_id 
FOREIGN KEY (dashboard_id) 
REFERENCES dashboard(id);
""")
	}
}
