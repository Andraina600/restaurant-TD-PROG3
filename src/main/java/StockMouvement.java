import java.time.Instant;

public class StockMouvement {
    private int id;
    private StockValue value;
    private MouvementType type;
    private Instant creationDatetime;

    public StockMouvement() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public StockValue getValue() { return value; }
    public void setValue(StockValue value) { this.value = value; }

    public MouvementType getType() { return type; }
    public void setType(MouvementType type) { this.type = type; }

    public Instant getCreationDatetime() { return creationDatetime; }
    public void setCreationDatetime(Instant creationDatetime) { this.creationDatetime = creationDatetime; }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", value=" + value +
                ", type=" + type +
                ", date=" + creationDatetime +
                '}';
    }
}

