import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private String reference;
    private Instant creationDateTime;
    private List<DishOrder> dishOrders = new ArrayList<>();

    public Order() {}

    public Order(int id, String reference, Instant creationDateTime) {
        this.id = id;
        this.reference = reference;
        this.creationDateTime = creationDateTime;
    }

    public int getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", creationDateTime=" + creationDateTime +
                ", dishOrders=" + dishOrders +
                '}';
    }
}

