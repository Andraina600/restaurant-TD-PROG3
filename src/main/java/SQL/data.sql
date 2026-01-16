INSERT INTO Dish (id, name, dish_type) values (1 , 'Salade fraîche', 'START'),
(2, 'Poulet grillé', 'MAIN'),
(3, 'Riz aux légumes', 'MAIN'),
(4, 'Gâteau au chocolat', 'DESSERT'),
(5, 'Salade de fruits', 'DESSERT')

INSERT INTO Ingredient (id, name, price, category, id_dish) values
(1, 'Laitue', 800.00, 'VEGETABLE', 1),
(2, 'Tomate', 600.00, 'VEGETABLE',1),
(3, 'Poulet', 4500.00, 'ANIMAL', 2),
(4, 'Chocolat', 3000.00, 'OTHER', 4),
(5, 'Beurre', 2500.00, 'DAIRY', 4)

INSERT INTO DishIngredient (id, id_dish, id_ingredient, quantity_required, unit) VALUES
    (1, 1, 1, 0.20, 'KG'),
    (2, 1, 2, 0.15, 'KG'),
    (3, 2, 3, 1.00, 'KG'),
    (4, 4, 4, 0.30, 'KG'),
    (5, 4, 5, 0.20, 'KG');

SELECT setval('dish_id_seq', COALESCE((SELECT MAX(id) FROM dish), 1));
SELECT setval('ingredient_id_seq', COALESCE((SELECT MAX(id) FROM ingredient), 1));

update dish set price = 2000.00 where name = 'Salade fraîche';
update dish set price = 6000.00 where name = 'Poulet grillé';
update dish set price = null where name = 'Riz aux légumes';
update dish set price = null where name = 'Gâteau au chocolat';
update dish set price = null where name = 'Salade de fruit';

