CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'TRIAL',
    trial_ends_at TIMESTAMP,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false
);

ALTER TABLE users ADD COLUMN company_id INTEGER;
ALTER TABLE users ADD CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE delivery_tasks ADD COLUMN company_id INTEGER;
ALTER TABLE delivery_tasks ADD CONSTRAINT fk_task_company FOREIGN KEY (company_id) REFERENCES companies(id);

-- Default company for existing data (seed users/tasks are assigned here)
INSERT INTO companies (name, email, status, trial_ends_at)
VALUES ('Default Company', 'admin@delivra.local', 'ACTIVE', NULL);

-- Assign all non-SUPER_ADMIN users to the default company
UPDATE users SET company_id = 1
WHERE id NOT IN (
    SELECT DISTINCT u.id FROM users u
    JOIN users_roles ur ON u.id = ur.user_id
    JOIN roles r ON ur.role_id = r.id
    WHERE r.user_system_role = 'SUPER_ADMIN'
);

-- Assign all existing tasks to the default company
UPDATE delivery_tasks SET company_id = 1;