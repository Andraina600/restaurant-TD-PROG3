CREATE TABLE "order" (
    id serial primary key,
    reference varchar(50) unique not null,
    creation_datetime timestamp not null default current_timestamp
);

CREATE TABLE dishorder (
    id serial primary key,
    id_order int unique not null references "order"(id) on delete cascade,
    id_dish int unique not null references dish(id),
    quantity int not null check (quantity > 0)
);