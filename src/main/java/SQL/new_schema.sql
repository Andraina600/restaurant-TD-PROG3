DROP TABLE IF EXISTS DishIngredient CASCADE;
DROP TABLE IF EXISTS Ingredient CASCADE;
DROP TABLE IF EXISTS Dish CASCADE;

CREATE TYPE unit_type AS ENUM ('PCS', 'KG', 'L');

CREATE TABLE Dish (
     id SERIAL PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     dish_type dish_type_enum NOT NULL,
     selling_price NUMERIC NULL
);

CREATE TABLE Ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC NOT NULL,
    category category_enum NOT NULL
);

CREATE TABLE DishIngredient (
    id SERIAL PRIMARY KEY,
    id_dish INT REFERENCES Dish(id) ON DELETE CASCADE,
    id_ingredient INT REFERENCES Ingredient(id) ON DELETE CASCADE,
    quantity_required NUMERIC NOT NULL,
    unit unit_type NOT NULL,
    UNIQUE (id_dish, id_ingredient)
);