CREATE TYPE dish_type_enum AS ENUM(
    'STAR','MAIN','DESSERT'
)

CREATE TYPE category_enum AS ENUM(
    'VEGETABLE','ANIMAL','MARINE','DAIRY','OTHER'
)


CREATE TABLE Dish(
    id serial primary key,
    name varchar(200) not null,
    dish_type dish_type_enum not null
)

CREATE TABLE Ingredient(
    id serial primary key,
    name varchar(200) not null,
    price numeric(10 , 2) not null,
    category category_enum not null,
    id_dish int,
    constraint fk_dish foreign key (id_dish) references Dish(id) on delete set null
)