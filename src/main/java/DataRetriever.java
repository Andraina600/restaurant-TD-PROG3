import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    public Dish findDishById(int id) throws SQLException {
        String sqlDish = "SELECT id, name, dish_type FROM dish WHERE id = ?";
        String sqlIngredients = "SELECT id, name, price, category, id_dish FROM ingredient i WHERE i.id_dish = ?";
        try (Connection conn = DBConnection.getConnection();){
            Dish dish = null;
            try (PreparedStatement psDish = conn.prepareStatement(sqlDish)) {
                psDish.setInt(1, id);

                try (ResultSet rs = psDish.executeQuery()) {
                    if (rs.next()) {
                        dish = new Dish();
                        dish.setId(rs.getInt("id"));
                        dish.setName(rs.getString("name"));
                        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                    }
                    else {
                        throw new RuntimeException("plat non trouver");
                    }
                }
            }

            try (PreparedStatement psIng = conn.prepareStatement(sqlIngredients)) {
                psIng.setInt(1, id);
                try (ResultSet rs = psIng.executeQuery()) {
                    while (rs.next()) {
                        Ingredient ingredient = new Ingredient();
                        ingredient.setId(rs.getInt("id"));
                        ingredient.setName(rs.getString("name"));
                        ingredient.setPrice(rs.getDouble("price"));
                        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                        ingredient.setDish(dish);
                        dish.getIngredients().add(ingredient);
                        conn.close();
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
        try{
            conn.setAutoCommit(false);
            String sqlDish;
            if(dishToSave.getId() == 0){
                sqlDish = "INSERT INTO dish (name, dish_type) VALUES (?,?::dish_type_enum) RETURNING id";
            }
            else{
                sqlDish = "UPDATE dish SET name = ?, dish_type = ? WHERE id = ? RETURNING id";
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlDish)) {
                ps.setString(1, dishToSave.getName());
                ps.setString(2, dishToSave.getDishType().name());
                if(dishToSave.getId() != 0){
                    ps.setInt(3, dishToSave.getId());
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dishToSave.setId(rs.getInt(1));
                    }
                }
            }

            try(PreparedStatement psDelete = conn.prepareStatement("DELETE FROM ingredient WHERE id_dish = ?")) {
                psDelete.setInt(1, dishToSave.getId());
                psDelete.executeUpdate();
            }

            String sqlIngredient = "INSERT INTO ingredient (name,price,category,id_dish) VALUES (?,?,?::category_enum,?) RETURNING id)";
            for(Ingredient ingredient : dishToSave.getIngredients()){
                try (PreparedStatement psIng = conn.prepareStatement(sqlIngredient)) {
                    psIng.setString(1, ingredient.getName());
                    psIng.setDouble(2, ingredient.getPrice());
                    psIng.setString(3, ingredient.getCategory().name());
                    psIng.setInt(4, dishToSave.getId());

                    try (ResultSet rs = psIng.executeQuery()) {
                        if (rs.next()) {
                            ingredient.setId(rs.getInt(1));
                        }
                    }
                    ingredient.setDish(dishToSave);
                }
            }
            conn.commit();
            return  dishToSave;
        }catch(SQLException e){
            conn.rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde du plat", e);
        }finally {
            conn.setAutoCommit(true);
            conn.close();
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
