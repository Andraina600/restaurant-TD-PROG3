import java.util.ArrayList;
import java.util.List;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredients;
    private Double price;

    public Dish() {
        this.ingredients = new ArrayList<>();
    }

    public Dish(int id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public Double getPrice() {
        return price;
    }

    public double getDishCost() {
        return ingredients.stream()
                .mapToDouble(ingredient -> ingredient.getPrice())
                .sum();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }

    public Double getCrossMargin(){
        if(price == null){
            throw new RuntimeException("Impossible de calculer la marge du plat " + name + " car le prix de vente n'est pas définie");
        }
        return price - getDishCost();
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", price=" +  price +
                ", crossinMarge=" + (price != null ? getCrossMargin() : "N/A" )+
                ", ingredients=" + ingredients +
                '}';
    }

}
