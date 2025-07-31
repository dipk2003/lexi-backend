-- Add notification_preferences column to lawyers table
ALTER TABLE lawyers ADD COLUMN notification_preferences TEXT;

-- Update existing records with default preferences
UPDATE lawyers SET notification_preferences = '{"notifications":{"email":true,"desktop":false,"research":true},"preferences":{"theme":"light","language":"en","defaultSortBy":"relevance"}}' WHERE notification_preferences IS NULL;
