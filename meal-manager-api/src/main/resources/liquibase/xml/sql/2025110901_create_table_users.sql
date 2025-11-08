-- Create users table
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    auth0_sub VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on auth0_sub for faster lookups
CREATE INDEX idx_users_auth0_sub ON users(auth0_sub);
