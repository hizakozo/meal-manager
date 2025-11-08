CREATE TABLE IF NOT EXISTS meals (
    meal_id UUID PRIMARY KEY,
    dish_name VARCHAR(100) NOT NULL,
    cooked_at TIMESTAMP NOT NULL,
    memo TEXT NOT NULL
);
