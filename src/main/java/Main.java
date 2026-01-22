import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever retriever = new DataRetriever();

        try {
            System.out.println("=== TEST 1 : Sauvegarde d'un ingrédient EXISTANT avec nouveaux mouvements ===");

            // 1. Récupérer un ingrédient existant (ex: ID 1 = Laitue)
            Ingredient existing = retriever.findIngredientById(1); // ou par nom/critères
            if (existing == null) {
                System.out.println("Ingrédient ID 1 non trouvé → on en crée un nouveau");
                existing = new Ingredient();
                existing.setId(0); // nouveau
                existing.setName("Laitue Test");
                existing.setPrice(850.00);
                existing.setCategory(CategoryEnum.VEGETABLE);
            }

            // 2. Ajouter des mouvements de stock (certains avec ID existant pour tester ON CONFLICT)
            StockMouvement mvtNew1 = new StockMouvement();
            mvtNew1.setId(0); // nouveau mouvement
            mvtNew1.setValue(new StockValue(3.5, UnitType.KG));
            mvtNew1.setType(MouvementType.IN);
            mvtNew1.setCreationDatetime(Instant.now().minus(2, ChronoUnit.DAYS));

            StockMouvement mvtNew2 = new StockMouvement();
            mvtNew2.setId(999); // ID fictif qui n'existe pas → sera inséré
            mvtNew2.setValue(new StockValue(1.2, UnitType.KG));
            mvtNew2.setType(MouvementType.OUT);
            mvtNew2.setCreationDatetime(Instant.now());

            StockMouvement mvtExisting = new StockMouvement();
            mvtExisting.setId(6); // ID déjà existant dans les données de test (0.2 OUT)
            mvtExisting.setValue(new StockValue(0.2, UnitType.KG));
            mvtExisting.setType(MouvementType.OUT);
            mvtExisting.setCreationDatetime(Instant.parse("2024-01-06T12:00:00Z"));

            // Ajout à la liste
            existing.getStockMouvementList().add(mvtNew1);
            existing.getStockMouvementList().add(mvtNew2);
            existing.getStockMouvementList().add(mvtExisting);

            // 3. Sauvegarde
            System.out.println("Avant sauvegarde : " + existing.getStockMouvementList().size() + " mouvements");
            Ingredient saved = retriever.saveIngredient(existing);
            System.out.println("Après sauvegarde : ingrédient ID = " + saved.getId());

            // 4. Vérification : recharger depuis la base
            Ingredient reloaded = retriever.findIngredientById(saved.getId());
            System.out.println("Mouvements après rechargement : " + reloaded.getStockMouvementList().size());

            // 5. Affichage des stocks
            Instant now = Instant.now();
            Instant pastDate = Instant.parse("2024-01-06T12:00:00Z");

            System.out.println("\nStock actuel (maintenant) : " + reloaded.getCurrentStock());
            System.out.println("Stock au 2024-01-06 12:00 : " + reloaded.getStockValueAt(pastDate));

            // Bonus : afficher tous les mouvements
            System.out.println("\nListe des mouvements enregistrés :");
            for (StockMouvement mvt : reloaded.getStockMouvementList()) {
                System.out.println(mvt);
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("Erreur générale : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}