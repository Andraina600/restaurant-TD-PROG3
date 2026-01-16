import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    public Dish findDishById(int id) throws SQLException {
        String sqlDish = "SELECT id, name, dish_type, price FROM dish WHERE id = ?";
        String sqlIngredients = "SELECT id, name, price, category, id_dish FROM ingredient WHERE id_dish = ?";

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

                        Object priceObj = rs.getObject("price");
                        dish.setPrice(priceObj != null ? rs.getDouble("price") : null);

                        dish.setIngredients(new ArrayList<>());
                    } else {
                        throw new RuntimeException("Plat non trouvé avec l'ID : " + id);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlIngredients)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Ingredient ing = new Ingredient();
                        ing.setId(rs.getInt("id"));
                        ing.setName(rs.getString("name"));
                        ing.setPrice(rs.getDouble("price"));
                        ing.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                        ing.setDish(dish);
                        dish.getIngredients().add(ing);
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
        String sql = "INSERT INTO ingredient (name,price,category,id_dish) VALUES (? ?,?::category_enum,?) RETURNING id";
        try (Connection conn = DBConnection.getConnection();){
            conn.setAutoCommit(false);
            try{
                for(Ingredient ing : newIngredients){
                    if(ing.getDish() == null || ing.getDish().getId() == 0){
                        return null;
                    }
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, ing.getName());
                        ps.setDouble(2, ing.getPrice());
                        ps.setString(3, ing.getCategory().name());
                        ps.setInt(4, ing.getDish().getId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                ing.setId(rs.getInt(1));
                            }
                        }
                    }
                }
                conn.commit();
                return newIngredients;
            }catch(SQLException e){
                conn.rollback();
                throw new RuntimeException("Erreur lors de la création des ingrédients", e);
            }finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public Dish saveDish(Dish dishToSave) throws SQLException {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            String sqlDish;
            if (dishToSave.getId() == 0) {
                sqlDish = "INSERT INTO dish (name, dish_type, price) VALUES (?, ?, ?) RETURNING id";
            } else {
                sqlDish = "UPDATE dish SET name = ?, dish_type = ?, price = ? WHERE id = ? RETURNING id";
            }

            int savedId;

            try (PreparedStatement ps = conn.prepareStatement(sqlDish)) {
                ps.setString(1, dishToSave.getName());
                ps.setString(2, dishToSave.getDishType().name());

                if (dishToSave.getPrice() == null) {
                    ps.setNull(3, java.sql.Types.NUMERIC);
                } else {
                    ps.setDouble(3, dishToSave.getPrice());
                }

                if (dishToSave.getId() != 0) {
                    ps.setInt(4, dishToSave.getId());
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        savedId = rs.getInt(1);
                        dishToSave.setId(savedId);
                    } else {
                        throw new SQLException("Aucun ID retourné après sauvegarde du plat");
                    }
                }
            }

            String sqlIngredient = "INSERT INTO ingredient (name, price, category, id_dish) VALUES (?, ?, ?, ?) RETURNING id";

            for (Ingredient ingredient : dishToSave.getIngredients()) {
                try (PreparedStatement psIng = conn.prepareStatement(sqlIngredient)) {
                    psIng.setString(1, ingredient.getName());
                    psIng.setDouble(2, ingredient.getPrice());
                    psIng.setString(3, ingredient.getCategory().name());
                    psIng.setInt(4, savedId);

                    try (ResultSet rs = psIng.executeQuery()) {
                        if (rs.next()) {
                            ingredient.setId(rs.getInt(1));
                        }
                    }
                    ingredient.setDish(dishToSave);
                }
            }

            conn.commit();
            return findDishById(savedId);  // Utilise ta méthode existante !

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException("Erreur lors de la sauvegarde du plat", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
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
