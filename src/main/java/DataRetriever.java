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

            // Récup compositions
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
    public List<Ingredient> findIngredient (int page, int size) throws SQLException {
        int offset = (page - 1) * size;
        String sql = "SELECT i.id, i.name, i.price, i.category, i.id_dish FROM ingredient i " +
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
                    conn.close();
                }
            }
        }
        return ingredients;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) throws SQLException {
        String sql = "INSERT INTO ingredient (name, price, category) " +
                "VALUES (?, ?, ?::category_enum) RETURNING id";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            for (Ingredient ing : newIngredients) {
                // Pas de getDish() ici → on n'associe pas encore
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
                    // ignore
                }
            }
            throw new RuntimeException("Erreur lors de la création des ingrédients", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    // ignore
                }
            }
        }
    }

    public Dish saveDish(Dish dishToSave) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Requête UPSERT (ON CONFLICT DO UPDATE)
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

    public List<Ingredient> findIngredientByCriteria(String IngredientName, CategoryEnum category, String dishName, int page, int size) throws SQLException {
        int offset = (page - 1) * size;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT i.* , d.name AS dish_name FROM ingredient i LEFT JOIN dish d ON d.id = i.id_dish WHERE 1=1");
        ArrayList<Object> params = new ArrayList<>();
        if(IngredientName != null && !IngredientName.isEmpty()){
            sql.append(" AND LOWER(i.name) LIKE LOWER(?)");
            params.add("%" + IngredientName + "%");
        }
        if(category != null){
            sql.append(" AND i.category = ?::category_enum");
            params.add(category.name());
        }
        if(dishName != null && !dishName.isEmpty()){
            sql.append(" AND LOWER(d.name) LIKE LOWER(?)");
            params.add("%" + dishName + "%");
        }
        sql.append(" ORDER BY i.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for(int i = 0; i < params.size(); i++){
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("id"));
                    ingredient.setName(rs.getString("name"));
                    ingredient.setPrice(rs.getDouble("price"));
                    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                    if(rs.getString("dish_name") != null){
                        Dish dish = new Dish();
                        dish.setName(rs.getString("dish_name"));
                        ingredient.setDish(dish);
                    }
                    ingredients.add(ingredient);
                    conn.close();
                }
            }
        }
        return ingredients;
    }
}
