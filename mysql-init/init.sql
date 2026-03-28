CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS restaurant_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS notification_db;

-- Explicitly grant privileges to the default app user on all 5 databases.
GRANT ALL PRIVILEGES ON auth_db.* TO 'fooduser'@'%';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'fooduser'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'fooduser'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'fooduser'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'fooduser'@'%';

FLUSH PRIVILEGES;
