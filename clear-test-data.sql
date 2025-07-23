-- SQL script to remove all test data from the database
-- Run this before collecting real balise data

-- Disable foreign key constraints temporarily
SET session_replication_role = 'replica';

-- Clear all data from tables
TRUNCATE balise_customer_assignments CASCADE;
TRUNCATE balises CASCADE;
TRUNCATE customers CASCADE;
TRUNCATE containers CASCADE;

-- Re-enable foreign key constraints
SET session_replication_role = 'origin';

-- Display status
SELECT 'All test data removed successfully!' as status;
