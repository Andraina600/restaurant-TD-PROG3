import java.util.HashMap;
import java.util.Map;

public class UnitConvertion {
    private static final Map<String, Map<UnitType, Double>> convertionToKG = new HashMap<>();

    static {

        Map<UnitType, Double> tomate = new HashMap<>();
        tomate.put(UnitType.KG, 1.0);
        tomate.put(UnitType.PCS, 0.1);
        convertionToKG.put("Tomate", tomate);

        Map<UnitType, Double> laitue = new HashMap<>();
        laitue.put(UnitType.KG, 1.0);
        laitue.put(UnitType.PCS, 0.5);
        convertionToKG.put("Laitue", laitue);

        Map<UnitType, Double> chocolat = new HashMap<>();
        chocolat.put(UnitType.KG, 1.0);
        chocolat.put(UnitType.PCS, 0.1);
        chocolat.put(UnitType.L, 0.4);
        convertionToKG.put("Chocolat", chocolat);

        Map<UnitType, Double> poulet = new HashMap<>();
        poulet.put(UnitType.KG, 1.0);
        poulet.put(UnitType.PCS, 0.125);
        convertionToKG.put("Poulet", poulet);

        Map<UnitType, Double> beurre = new HashMap<>();
        beurre.put(UnitType.KG, 1.0);
        beurre.put(UnitType.PCS, 0.25);
        beurre.put(UnitType.L, 0.2);
        convertionToKG.put("Beurre", beurre);

    }

    private UnitConvertion() {}

    public static double toKG(String ingredientName, double quantity, UnitType fromUnit) {
        if (fromUnit == UnitType.KG) {
            return quantity;
        }

        Map<UnitType, Double> map = convertionToKG.get(ingredientName);
        if (map == null || !map.containsKey(fromUnit)) {
            throw new IllegalArgumentException(
                    "Conversion impossible pour l'ingrédient '" + ingredientName +
                            "' depuis l'unité " + fromUnit
            );
        }
        return quantity * map.get(fromUnit);
    }

    public static double fromKG(String ingredientName, double quantityKG, UnitType toUnit) {
        if (toUnit == UnitType.KG) {
            return quantityKG;
        }

        Map<UnitType, Double> map = convertionToKG.get(ingredientName);
        if (map == null || !map.containsKey(toUnit)) {
            throw new IllegalArgumentException(
                    "Conversion impossible vers l'unité " + toUnit +
                            " pour l'ingrédient '" + ingredientName + "'"
            );
        }
        return quantityKG / map.get(toUnit);
    }

    public static boolean isConversionSupported(String ingredientName, UnitType unit) {
        if (unit == UnitType.KG) return true;
        Map<UnitType, Double> map = convertionToKG.get(ingredientName);
        return map != null && map.containsKey(unit);
    }
}