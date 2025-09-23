-- Drop unique constraint on customer_id in customer_week_meals table
-- This constraint was created when relationship was OneToOne, but now it's ManyToOne
-- allowing multiple CustomerWeekMeal records per customer

SET @constraint_name = (
    SELECT CONSTRAINT_NAME
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_NAME = 'customer_week_meals'
    AND CONSTRAINT_TYPE = 'UNIQUE'
    AND CONSTRAINT_NAME LIKE '%customer_id%'
    LIMIT 1
);

SET @sql = IF(@constraint_name IS NOT NULL,
    CONCAT('ALTER TABLE customer_week_meals DROP INDEX ', @constraint_name),
    'SELECT "No constraint found to drop"'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
