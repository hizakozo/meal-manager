-- Add user_id column to meals table
ALTER TABLE meals ADD COLUMN user_id UUID;

-- Initially allow NULL for existing data migration
-- In production, you would migrate existing data first, then add NOT NULL constraint

-- Add foreign key constraint
ALTER TABLE meals ADD CONSTRAINT fk_meals_user_id 
    FOREIGN KEY (user_id) REFERENCES users(user_id);

-- Create index for faster queries
CREATE INDEX idx_meals_user_id ON meals(user_id);
