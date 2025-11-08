CREATE TABLE IF NOT EXISTS meal_images (
    meal_id UUID PRIMARY KEY,
    image_id UUID NOT NULL,
    CONSTRAINT fk_meal_images_meal FOREIGN KEY (meal_id) REFERENCES meals(meal_id),
    CONSTRAINT fk_meal_images_image FOREIGN KEY (image_id) REFERENCES images(image_id)
);
