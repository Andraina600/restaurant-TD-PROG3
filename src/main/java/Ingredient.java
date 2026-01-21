import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private int id;
    private String name;
    private double price;
    private CategoryEnum category;
    private List<StockMouvement> stockMouvementList  = new ArrayList<>();

    public Ingredient() {}

    public Ingredient(int id, String name, double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public List<StockMouvement> getStockMouvementList() { return stockMouvementList; }
    public void setStockMouvementList(List<StockMouvement> stockMouvementList) {
        this.stockMouvementList = stockMouvementList != null ? stockMouvementList : new ArrayList<>();
    }

    // Calcul du stock à une date donnée (instant)
    public StockValue getStockValueAt(Instant instant) {
        double total = 0.0;
        UnitType unit = null;  // On suppose que toutes les unités sont identiques (KG dans les données)

        for (StockMouvement mvt : stockMouvementList) {
            if (mvt.getCreationDatetime().isBefore(instant) || mvt.getCreationDatetime().equals(instant)) {
                if (unit == null) unit = mvt.getValue().getUnit();
                double qty = mvt.getValue().getQuantity();
                if (mvt.getType() == MouvementType.IN) {
                    total += qty;
                } else {
                    total -= qty;
                }
            }
        }

        return new StockValue(total < 0 ? 0 : total, unit != null ? unit : UnitType.KG);
    }

    // Stock actuel (à maintenant)
    public StockValue getCurrentStock() {
        return getStockValueAt(Instant.now());
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", currentStock=" + getCurrentStock() +
                '}';
    }
}
