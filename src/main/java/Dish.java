import java.util.ArrayList;
import java.util.List;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;  // nullable
    private List<DishIngredient> compositions = new ArrayList<>();

    public Dish() {
        this.compositions = new ArrayList<>();
    }

    public Dish(int id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.compositions = new ArrayList<>();
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

    public List<DishIngredient> getIngredients() {
        return compositions;
    }

    public Double getPrice() {
        return sellingPrice;
    }

    public List<DishIngredient> getCompositions() {
        return compositions;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setCompositions(List<DishIngredient> compositions) {
        this.compositions = compositions;
    }

    public double getDishCost() {
        return compositions.stream()
                .mapToDouble(DishIngredient::getCostForDish)
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

    public void setPrice(Double sellingPrice) {
        this.sellingPrice = Dish.this.sellingPrice;
    }

    public void setIngredients(List<Ingredient> compositions) {
        this.compositions = Dish.this.compositions != null ? Dish.this.compositions : new ArrayList<>();
    }

    public Double getCrossMargin(){
        if(sellingPrice == null){
            throw new RuntimeException("Impossible de calculer la marge du plat " + name + " car le prix de vente n'est pas définie");
        }
        return sellingPrice - getDishCost();
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", sellingPrice=" +  sellingPrice +
                ", crossinMarge=" + (sellingPrice != null ? getCrossMargin() : "N/A" )+
                ", compositions=" + compositions +
                '}';
    }

}
