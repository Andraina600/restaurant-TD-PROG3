    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;

    public class DataRetriever {
        public Dish findDishById(int id) throws SQLException {
            String sqlDish = "SELECT * FROM Dish WHERE id = ?";
            String sqlComp = """
            SELECT di.*, i.name, i.price, i.category
            FROM DishIngredient di
            JOIN Ingredient i ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
            """;

            try (Connection conn = DBConnection.getConnection()) {
                Dish dish = null;
                try (PreparedStatement ps = conn.prepareStatement(sqlDish)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dish = new Dish();
                            dish.setId(rs.getInt("id"));
                            dish.setName(rs.getString("name"));
                            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                            Object priceObj = rs.getObject("selling_price");
                            dish.setSellingPrice(priceObj != null ? rs.getDouble("selling_price") : null);
                        } else {
                            return null;
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlComp)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            DishIngredient di = new DishIngredient();
                            di.setId(rs.getInt("id"));
                            di.setQuantityRequired(rs.getDouble("quantity_required"));
                            di.setUnit(UnitType.valueOf(rs.getString("unit")));

                            Ingredient ing = new Ingredient();
                            ing.setId(rs.getInt("id_ingredient"));
                            ing.setName(rs.getString("name"));
                            ing.setPrice(rs.getDouble("price"));
                            ing.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                            di.setDish(dish);
                            di.setIngredient(ing);
                            dish.getCompositions().add(di);
                        }
                    }
                }
                return dish;
            }
        }

        public Ingredient findIngredientById(int id) throws SQLException {
            String sqlIngredient = """
            SELECT id, name, price, category
            FROM ingredient
            WHERE id = ?
            """;

            String sqlMovements = """
            SELECT id, quantity, unit, type_mouvement, creation_datetime
            FROM stock_mouvement
            WHERE id_ingredient = ?
            ORDER BY creation_datetime ASC
            """;

            try (Connection conn = DBConnection.getConnection()) {
                Ingredient ingredient = null;

                try (PreparedStatement psIng = conn.prepareStatement(sqlIngredient)) {
                    psIng.setInt(1, id);
                    try (ResultSet rs = psIng.executeQuery()) {
                        if (rs.next()) {
                            ingredient = new Ingredient();
                            ingredient.setId(rs.getInt("id"));
                            ingredient.setName(rs.getString("name"));
                            ingredient.setPrice(rs.getDouble("price"));
                            ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                        } else {
                            return null; // ingrédient non trouvé
                        }
                    }
                }

                try (PreparedStatement psMvt = conn.prepareStatement(sqlMovements)) {
                    psMvt.setInt(1, id);
                    try (ResultSet rs = psMvt.executeQuery()) {
                        while (rs.next()) {
                            StockMouvement mvt = new StockMouvement();
                            mvt.setId(rs.getInt("id"));

                            StockValue value = new StockValue();
                            value.setQuantity(rs.getDouble("quantity"));
                            value.setUnit(UnitType.valueOf(rs.getString("unit")));
                            mvt.setValue(value);

                            mvt.setType(MouvementType.valueOf(rs.getString("type_mouvement")));
                            mvt.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

                            ingredient.getStockMouvementList().add(mvt);
                        }
                    }
                }

                return ingredient;
            }
        }

        /*public List<Ingredient> findIngredient (int page, int size) throws SQLException {
            int offset = (page - 1) * size;
            String sql = "SELECT i.id, i.name, i.price, i.category FROM ingredient i " +
                    "left join dish d on d.id = i.id_dish order by i.id asc LIMIT ? OFFSET ? ";
            List<Ingredient> ingredients = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, size);
                ps.setInt(2, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Ingredient ingredient = new Ingredient();
                        ingredient.setId(rs.getInt("id"));
                        ingredient.setName(rs.getString("name"));
                        ingredient.setPrice(rs.getDouble("price"));
                        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                        ingredients.add(ingredient);
                    }
                    conn.close();
                }
            }
            return ingredients;
        }*/

        public List<Ingredient> createIngredients(List<Ingredient> newIngredients) throws SQLException {
            String sql = "INSERT INTO ingredient (name, price, category) " +
                    "VALUES (?, ?, ?::category_enum) RETURNING id";

            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);

                for (Ingredient ing : newIngredients) {
                    if (ing.getId() != 0) {
                        throw new IllegalArgumentException("Nouvel ingrédient doit avoir id = 0");
                    }

                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, ing.getName());
                        ps.setDouble(2, ing.getPrice());
                        ps.setString(3, ing.getCategory().name());

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                ing.setId(rs.getInt(1));
                            }
                        }
                    }
                }

                conn.commit();
                return newIngredients;

            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {

                    }
                }
                throw new RuntimeException("Erreur lors de la création des ingrédients", e);
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ex) {

                    }
                }
            }
        }

        public Dish saveDish(Dish dishToSave) throws SQLException {
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);

                String sqlDish = """
                INSERT INTO dish (id, name, dish_type, selling_price)
                VALUES (?, ?, ?::dish_type_enum, ?)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    dish_type = EXCLUDED.dish_type,
                    selling_price = EXCLUDED.selling_price
                RETURNING id
                """;

                int savedId;
                try (PreparedStatement psDish = conn.prepareStatement(sqlDish)) {
                    psDish.setInt(1, dishToSave.getId() == 0 ? 0 : dishToSave.getId());
                    psDish.setString(2, dishToSave.getName());
                    psDish.setString(3, dishToSave.getDishType().name());

                    if (dishToSave.getPrice() == null) {
                        psDish.setNull(4, java.sql.Types.NUMERIC);
                    } else {
                        psDish.setDouble(4, dishToSave.getPrice());
                    }

                    try (ResultSet rs = psDish.executeQuery()) {
                        if (rs.next()) {
                            savedId = rs.getInt(1);
                            dishToSave.setId(savedId);
                        } else {
                            throw new SQLException("Aucun ID retourné après sauvegarde du plat");
                        }
                    }
                }

                String deleteSql = "DELETE FROM DishIngredient WHERE id_dish = ?";
                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setInt(1, savedId);
                    psDelete.executeUpdate();
                }

                String insertCompSql = """
                INSERT INTO DishIngredient 
                (id_dish, id_ingredient, quantity_required, unit)
                VALUES (?, ?, ?, ?::unit_type)
                RETURNING id
                """;

                for (DishIngredient composition : dishToSave.getCompositions()) {
                    Ingredient ing = composition.getIngredient();

                    if (ing.getId() == 0) {
                        throw new IllegalArgumentException(
                                "L'ingrédient '" + ing.getName() + "' doit avoir un ID existant en base"
                        );
                    }

                    try (PreparedStatement psComp = conn.prepareStatement(insertCompSql)) {
                        psComp.setInt(1, savedId);
                        psComp.setInt(2, ing.getId());
                        psComp.setDouble(3, composition.getQuantityRequired());
                        psComp.setString(4, composition.getUnit().name());

                        try (ResultSet rs = psComp.executeQuery()) {
                            if (rs.next()) {
                                composition.setId(rs.getInt(1));
                            }
                        }
                    }
                }

                conn.commit();
                return findDishById(savedId);

            } catch (SQLException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ignored) {}
                }
                throw new RuntimeException("Erreur lors de la sauvegarde du plat", e);
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ignored) {}
                }
            }
        }

        public Ingredient saveIngredient(Ingredient toSave) throws SQLException {
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);

                int ingredientId = saveOrUpdateIngredientCore(toSave, conn);

                saveStockMovements(ingredientId, toSave.getStockMouvementList(), conn);

                conn.commit();
                return toSave;

            } catch (SQLException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ignored) {}
                }
                throw new RuntimeException("Erreur lors de la sauvegarde de l'ingrédient et ses mouvements", e);
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ignored) {}
                }
            }
        }

        private int saveOrUpdateIngredientCore(Ingredient toSave, Connection conn) throws SQLException {
            String sql = """
            INSERT INTO ingredient (id, name, price, category)
            VALUES (?, ?, ?, ?::category_enum)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                price = EXCLUDED.price,
                category = EXCLUDED.category
            RETURNING id
            """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, toSave.getId());
                ps.setString(2, toSave.getName());
                ps.setDouble(3, toSave.getPrice());
                ps.setString(4, toSave.getCategory().name());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        toSave.setId(newId);
                        return newId;
                    } else {
                        throw new SQLException("Aucun ID retourné après sauvegarde de l'ingrédient");
                    }
                }
            }
        }


        private void saveStockMovements(int ingredientId, List<StockMouvement> movements, Connection conn) throws SQLException {
            if (movements == null || movements.isEmpty()) {
                return;
            }

            String sql = """
            INSERT INTO stock_mouvement 
            (id, id_ingredient, quantity, unit, type_mouvement, creation_datetime)
            VALUES (?, ?, ?, ?::unit_type, ?::type_mvt, ?)
            ON CONFLICT (id) DO NOTHING
            RETURNING id
            """;

            for (StockMouvement mvt : movements) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, mvt.getId());
                    ps.setInt(2, ingredientId);
                    ps.setDouble(3, mvt.getValue().getQuantity());
                    ps.setString(4, mvt.getValue().getUnit().name());
                    ps.setString(5, mvt.getType().name());
                    ps.setTimestamp(6, Timestamp.from(mvt.getCreationDatetime()));

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            mvt.setId(rs.getInt(1));
                        }
                    }
                }
            }
        }

        public Order saveOrder(Order orderToSave) throws SQLException {
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);

                checkStockAvailability(orderToSave, conn);

                String reference = generateOrderReference(conn);
                orderToSave.setReference(reference);

                insertOrder(orderToSave, conn);

                insertDishOrderLines(orderToSave, conn);

                conn.commit();
                return orderToSave;

            } catch (SQLException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ignored) {}
                }
                throw new RuntimeException("Erreur lors de la création de la commande", e);
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ignored) {}
                }
            }
        }

        private void checkStockAvailability(Order order, Connection conn) throws SQLException {
            StringBuilder errorMsg = new StringBuilder();

            for (DishOrder doLine : order.getDishOrders()) {
                Dish dish = findDishById(doLine.getDish().getId());

                for (DishIngredient comp : dish.getCompositions()) {
                    Ingredient ing = findIngredientById(comp.getIngredient().getId());
                    double required = doLine.getQuantity() * comp.getQuantityRequired();

                    try{
                        double requiredKG = UnitConvertion.toKG(ing.getName(), required, comp.getUnit());
                        double currentKG = ing.getCurrentStockInKG();
                        if (currentKG < requiredKG) {
                            errorMsg.append(String.format(
                                    "Ingrédient '%s' insuffisant (besoin: %.2f KG, disponible: %.2f)%n",
                                    ing.getName(), requiredKG, currentKG
                            ));
                        }
                    }catch(IllegalArgumentException e){
                        errorMsg.append("Convertion impossible: ").append(e.getMessage());
                    }

                }
            }

            if (errorMsg.length() > 0) {
                throw new RuntimeException("Stock insuffisant :\n" + errorMsg);
            }
        }

        private String generateOrderReference(Connection conn) throws SQLException {
            String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(reference FROM 4) AS INT)), 0) + 1 FROM \"order\"";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int next = rs.getInt(1);
                    return String.format("ORD%05d", next);
                }
                return "ORD00001";
            }
        }

        private void insertOrder(Order order, Connection conn) throws SQLException {
            String sql = "INSERT INTO \"order\" (reference) VALUES (?) RETURNING id, creation_datetime";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, order.getReference());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        order.setId(rs.getInt("id"));
                        order.setCreationDateTime(rs.getTimestamp("creation_datetime").toInstant());
                    } else {
                        throw new SQLException("Échec insertion commande");
                    }
                }
            }
        }

        private void insertDishOrderLines(Order order, Connection conn) throws SQLException {
            String sql = "INSERT INTO dish_order (id_order, id_dish, quantity) VALUES (?, ?, ?) RETURNING id";
            for (DishOrder line : order.getDishOrders()) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, order.getId());
                    ps.setInt(2, line.getDish().getId());
                    ps.setInt(3, line.getQuantity());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            line.setId(rs.getInt(1));
                        }
                    }
                    line.setOrder(order);
                }
            }
        }

        public Order findOrderByReference(String orderReference) throws SQLException {
            String sqlOrder = "SELECT id, reference, creation_datetime FROM \"order\" WHERE reference = ?";
            String sqlDishOrder = """
                    SELECT dish_ord.id, dish_ord.id_dish, dish_ord.quantity, d.name, d.dish_type, d.selling_price 
                    FROM dish_order dish_ord
                    JOIN dish d ON d.id = dish_ord.id_dish
                    WHERE dish_ord.id_order = ?
                    """;

            try(Connection conn = DBConnection.getConnection();){
                Order order = null;
                try (PreparedStatement ps = conn.prepareStatement(sqlOrder)) {
                    ps.setString(1, orderReference);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            order = new Order();
                            order.setId(rs.getInt("id"));
                            order.setReference(rs.getString("reference"));
                            order.setCreationDateTime(rs.getTimestamp("creation_datetime").toInstant());
                        }else {
                            throw new RuntimeException("Commande introuvable avec la reference: " + orderReference);
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlDishOrder)) {
                    ps.setInt(1, order.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            DishOrder dishOrder = new DishOrder();
                            dishOrder.setId(rs.getInt("id"));
                            dishOrder.setQuantity(rs.getInt("quantity"));

                            Dish dish = new Dish();
                            dish.setId(rs.getInt("id_dish"));
                            dish.setName(rs.getString("name"));
                            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));

                            Object priceObject  = rs.getObject("selling_price");
                            dish.setSellingPrice(priceObject != null ? rs.getDouble("selling_price") : null);

                            dishOrder.setDish(dish);
                            dishOrder.setOrder(order);

                            order.getDishOrders().add(dishOrder);
                        }
                    }
                }
                return order;
            }
        }

        public List<Dish> findDishByIngredientName(String ingredientName) throws SQLException {
            String sql = "SELECT DISTINCT d.id, d.name, d.dish_type FROM dish d JOIN ingredient i on d.id = i.id_dish WHERE LOWER(i.name) LIKE LOWER(?)";
            List<Dish> dishes = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + ingredientName + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Dish dish = new Dish();
                        dish.setId(rs.getInt("id"));
                        dish.setName(rs.getString("name"));
                        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                        dishes.add(dish);
                    }
                }
            }
            return dishes;
        }

        public void associateIngredientToDish(int dishId, int ingredientId, double quantity, UnitType unit) throws SQLException {
            String sql = "INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit) " +
                    "VALUES (?, ?, ?, ?::unit_type)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, dishId);
                ps.setInt(2, ingredientId);
                ps.setDouble(3, quantity);
                ps.setString(4, unit.name());
                ps.executeUpdate();
            }
        }

        public List<Ingredient> findIngredientsByCriteria(
                String ingredientName,
                CategoryEnum category,
                String dishName,
                int page,
                int size) throws SQLException {

            int offset = (page - 1) * size;

            StringBuilder sql = new StringBuilder();
            sql.append("""
            SELECT DISTINCT i.id, i.name, i.price, i.category
            FROM ingredient i
            """);
            boolean hasDishFilter = dishName != null && !dishName.isBlank();
            if (hasDishFilter) {
                sql.append("""
                JOIN DishIngredient di ON di.id_ingredient = i.id
                JOIN Dish d ON d.id = di.id_dish
                """);
            }

            sql.append(" WHERE 1=1");

            List<Object> params = new ArrayList<>();

            if (ingredientName != null && !ingredientName.isBlank()) {
                sql.append(" AND LOWER(i.name) LIKE LOWER(?)");
                params.add("%" + ingredientName + "%");
            }

            if (category != null) {
                sql.append(" AND i.category = ?::category_enum");
                params.add(category.name());
            }

            if (hasDishFilter) {
                sql.append(" AND LOWER(d.name) LIKE LOWER(?)");
                params.add("%" + dishName + "%");
            }

            sql.append(" ORDER BY i.id LIMIT ? OFFSET ?");
            params.add(size);
            params.add(offset);

            List<Ingredient> ingredients = new ArrayList<>();

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Ingredient ing = new Ingredient();
                        ing.setId(rs.getInt("id"));
                        ing.setName(rs.getString("name"));
                        ing.setPrice(rs.getDouble("price"));
                        ing.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                        ingredients.add(ing);
                    }
                }
            }

            return ingredients;
        }
    }
