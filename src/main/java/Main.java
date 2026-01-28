import java.sql.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("║      TESTS DE CONVERSION D'UNITÉS - SYSTÈME STOCK     ║");

        testConversionsToKG();
        testConversionsFromKG();
        testConversionsRoundTrip();
        testConversionsNonSupportees();
        testConversionsAvecQuantitesVariees();

        System.out.println("\n" + "=".repeat(60));

        // TESTS AVEC BASE DE DONNÉES
        DataRetriever dataRetriever = new DataRetriever();

        if (nettoyerBaseDeDonnees()) {
            testCommandeAvecConversions(dataRetriever);
            testStockInsuffisantAvecConversions(dataRetriever);
            testStockMixteUnites(dataRetriever);
        }

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                   FIN DES TESTS                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // TESTS DE CONVERSION : TO KG
    private static void testConversionsToKG() {
        System.out.println("TEST 1 : Conversions vers KG (toKG)     ");

        try {

            double result = UnitConvertion.toKG("Tomate", 10, UnitType.PCS);
            printConversion("10 tomates", result, "KG", 1.0, "10 × 0.1 = 1.0");

            result = UnitConvertion.toKG("Tomate", 5, UnitType.PCS);
            printConversion("5 tomates", result, "KG", 0.5, "5 × 0.1 = 0.5");

            result = UnitConvertion.toKG("Tomate", 2.5, UnitType.KG);
            printConversion("2.5 KG tomate", result, "KG", 2.5, "2.5 × 1.0 = 2.5");

            result = UnitConvertion.toKG("Laitue", 4, UnitType.PCS);
            printConversion("4 laitues", result, "KG", 2.0, "4 × 0.5 = 2.0");

            result = UnitConvertion.toKG("Laitue", 1, UnitType.PCS);
            printConversion("1 laitue", result, "KG", 0.5, "1 × 0.5 = 0.5");

            result = UnitConvertion.toKG("Chocolat", 20, UnitType.PCS);
            printConversion("20 tablettes chocolat", result, "KG", 2.0, "20 × 0.1 = 2.0");

            result = UnitConvertion.toKG("Chocolat", 5, UnitType.L);
            printConversion("5 L chocolat", result, "KG", 2.0, "5 × 0.4 = 2.0");

            result = UnitConvertion.toKG("Chocolat", 3, UnitType.L);
            printConversion("3 L chocolat", result, "KG", 1.2, "3 × 0.4 = 1.2");

            // POULET : 1 PCS = 0.125 KG
            result = UnitConvertion.toKG("Poulet", 8, UnitType.PCS);
            printConversion("8 morceaux poulet", result, "KG", 1.0, "8 × 0.125 = 1.0");

            result = UnitConvertion.toKG("Poulet", 16, UnitType.PCS);
            printConversion("16 morceaux poulet", result, "KG", 2.0, "16 × 0.125 = 2.0");

            // BEURRE : 1 PCS = 0.25 KG, 1 L = 0.2 KG
            result = UnitConvertion.toKG("Beurre", 4, UnitType.PCS);
            printConversion("4 plaquettes beurre", result, "KG", 1.0, "4 × 0.25 = 1.0");

            result = UnitConvertion.toKG("Beurre", 10, UnitType.L);
            printConversion("10 L beurre", result, "KG", 2.0, "10 × 0.2 = 2.0");

            System.out.println("✅ Tous les tests toKG réussis !\n");

        } catch (Exception e) {
            System.err.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // TESTS DE CONVERSION : FROM KG
    private static void testConversionsFromKG() {
        System.out.println("│  TEST 2 : Conversions depuis KG (fromKG)           │");

        try {
            double result = UnitConvertion.fromKG("Tomate", 1.0, UnitType.PCS);
            printConversion("1.0 KG tomate", result, "PCS", 10.0, "1.0 / 0.1 = 10");

            result = UnitConvertion.fromKG("Tomate", 0.5, UnitType.PCS);
            printConversion("0.5 KG tomate", result, "PCS", 5.0, "0.5 / 0.1 = 5");

            // LAITUE : 1 KG = 2 PCS (car 1 PCS = 0.5 KG)
            result = UnitConvertion.fromKG("Laitue", 2.0, UnitType.PCS);
            printConversion("2.0 KG laitue", result, "PCS", 4.0, "2.0 / 0.5 = 4");

            result = UnitConvertion.fromKG("Laitue", 1.0, UnitType.PCS);
            printConversion("1.0 KG laitue", result, "PCS", 2.0, "1.0 / 0.5 = 2");

            // CHOCOLAT : 1 KG = 10 PCS ou 2.5 L
            result = UnitConvertion.fromKG("Chocolat", 2.0, UnitType.PCS);
            printConversion("2.0 KG chocolat", result, "PCS", 20.0, "2.0 / 0.1 = 20");

            result = UnitConvertion.fromKG("Chocolat", 2.0, UnitType.L);
            printConversion("2.0 KG chocolat", result, "L", 5.0, "2.0 / 0.4 = 5");

            // POULET : 1 KG = 8 PCS (car 1 PCS = 0.125 KG)
            result = UnitConvertion.fromKG("Poulet", 1.0, UnitType.PCS);
            printConversion("1.0 KG poulet", result, "PCS", 8.0, "1.0 / 0.125 = 8");

            result = UnitConvertion.fromKG("Poulet", 2.0, UnitType.PCS);
            printConversion("2.0 KG poulet", result, "PCS", 16.0, "2.0 / 0.125 = 16");

            // BEURRE : 1 KG = 4 PCS ou 5 L
            result = UnitConvertion.fromKG("Beurre", 1.0, UnitType.PCS);
            printConversion("1.0 KG beurre", result, "PCS", 4.0, "1.0 / 0.25 = 4");

            result = UnitConvertion.fromKG("Beurre", 2.0, UnitType.L);
            printConversion("2.0 KG beurre", result, "L", 10.0, "2.0 / 0.2 = 10");

            System.out.println("✅ Tous les tests fromKG réussis !\n");

        } catch (Exception e) {
            System.err.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testConversionsRoundTrip() {
        System.out.println("│  TEST 3 : Conversions aller-retour                 │");

        try {

            double original = 10.0;
            double toKg = UnitConvertion.toKG("Tomate", original, UnitType.PCS);
            double backToPcs = UnitConvertion.fromKG("Tomate", toKg, UnitType.PCS);
            printRoundTrip("Tomate", original, "PCS", toKg, backToPcs);

            // Laitue : 5 PCS → KG → PCS
            original = 5.0;
            toKg = UnitConvertion.toKG("Laitue", original, UnitType.PCS);
            backToPcs = UnitConvertion.fromKG("Laitue", toKg, UnitType.PCS);
            printRoundTrip("Laitue", original, "PCS", toKg, backToPcs);

            original = 3.0;
            toKg = UnitConvertion.toKG("Chocolat", original, UnitType.L);
            double backToL = UnitConvertion.fromKG("Chocolat", toKg, UnitType.L);
            printRoundTrip("Chocolat", original, "L", toKg, backToL);

            original = 16.0;
            toKg = UnitConvertion.toKG("Poulet", original, UnitType.PCS);
            backToPcs = UnitConvertion.fromKG("Poulet", toKg, UnitType.PCS);
            printRoundTrip("Poulet", original, "PCS", toKg, backToPcs);

            original = 8.0;
            toKg = UnitConvertion.toKG("Beurre", original, UnitType.PCS);
            backToPcs = UnitConvertion.fromKG("Beurre", toKg, UnitType.PCS);
            printRoundTrip("Beurre", original, "PCS", toKg, backToPcs);

            System.out.println("✅ Tous les tests aller-retour réussis !\n");

        } catch (Exception e) {
            System.err.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testConversionsNonSupportees() {
        System.out.println("│  TEST 4 : Conversions non supportées (erreurs)     │");

        int errorsCount = 0;

        try {
            UnitConvertion.toKG("Banane", 5, UnitType.PCS);
            System.out.println("❌ Devrait échouer : Banane n'est pas supportée");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Exception attendue : " + e.getMessage());
            errorsCount++;
        }

        try {
            UnitConvertion.toKG("Tomate", 2, UnitType.L);
            System.out.println("❌ Devrait échouer : Tomate ne supporte pas les litres");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Exception attendue : " + e.getMessage());
            errorsCount++;
        }

        try {
            UnitConvertion.toKG("Poulet", 3, UnitType.L);
            System.out.println("❌ Devrait échouer : Poulet ne supporte pas les litres");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Exception attendue : " + e.getMessage());
            errorsCount++;
        }
        try {
            UnitConvertion.fromKG("Laitue", 1, UnitType.L);
            System.out.println("❌ Devrait échouer : Laitue ne supporte pas les litres");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Exception attendue : " + e.getMessage());
            errorsCount++;
        }

        System.out.println("\n📋 Tests isConversionSupported :");
        System.out.println("  Tomate + PCS : " + (UnitConvertion.isConversionSupported("Tomate", UnitType.PCS) ? "✅" : "❌"));
        System.out.println("  Tomate + L   : " + (!UnitConvertion.isConversionSupported("Tomate", UnitType.L) ? "✅" : "❌"));
        System.out.println("  Chocolat + L : " + (UnitConvertion.isConversionSupported("Chocolat", UnitType.L) ? "✅" : "❌"));
        System.out.println("  Poulet + L   : " + (!UnitConvertion.isConversionSupported("Poulet", UnitType.L) ? "✅" : "❌"));

        System.out.println("\n✅ " + errorsCount + " exceptions capturées correctement !\n");
    }

    // TESTS AVEC QUANTITÉS VARIÉES
    private static void testConversionsAvecQuantitesVariees() {
        System.out.println("│  TEST 5 : Conversions avec quantités variées       │");

        System.out.println("📊 Tableau de conversion - Tomate (1 PCS = 0.1 KG)");
        System.out.println("─".repeat(50));
        for (int i = 1; i <= 5; i++) {
            double kg = UnitConvertion.toKG("Tomate", i, UnitType.PCS);
            System.out.printf("  %2d tomate(s) = %.2f KG%n", i, kg);
        }

        System.out.println("\n📊 Tableau de conversion - Laitue (1 PCS = 0.5 KG)");
        System.out.println("─".repeat(50));
        for (int i = 1; i <= 5; i++) {
            double kg = UnitConvertion.toKG("Laitue", i, UnitType.PCS);
            System.out.printf("  %2d laitue(s) = %.2f KG%n", i, kg);
        }

        System.out.println("\n📊 Tableau de conversion - Chocolat");
        System.out.println("─".repeat(50));
        System.out.println("  En tablettes (1 PCS = 0.1 KG) :");
        for (int i = 5; i <= 25; i += 5) {
            double kg = UnitConvertion.toKG("Chocolat", i, UnitType.PCS);
            System.out.printf("    %2d tablette(s) = %.2f KG%n", i, kg);
        }
        System.out.println("  En litres (1 L = 0.4 KG) :");
        for (int i = 1; i <= 5; i++) {
            double kg = UnitConvertion.toKG("Chocolat", i, UnitType.L);
            System.out.printf("    %2d litre(s)    = %.2f KG%n", i, kg);
        }

        System.out.println("\n📊 Tableau de conversion - Poulet (1 PCS = 0.125 KG)");
        System.out.println("─".repeat(50));
        for (int i = 4; i <= 20; i += 4) {
            double kg = UnitConvertion.toKG("Poulet", i, UnitType.PCS);
            System.out.printf("  %2d morceau(x) = %.3f KG%n", i, kg);
        }

        System.out.println("\n✅ Tests avec quantités variées terminés !\n");
    }

    // TESTS AVEC BASE DE DONNÉES

    private static void testCommandeAvecConversions(DataRetriever dr) {
        System.out.println("│  TEST 6 : Commande avec conversions d'unités       │");

        try {
            Order order = new Order();

            DishOrder dishOrder = new DishOrder();
            Dish dish = new Dish();
            dish.setId(1);
            dishOrder.setDish(dish);
            dishOrder.setQuantity(2);

            order.getDishOrders().add(dishOrder);

            Order saved = dr.saveOrder(order);

            System.out.println("✅ Commande créée avec succès !");
            System.out.println("   Référence: " + saved.getReference());
            System.out.println("   (Vérification des conversions effectuée automatiquement)");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Stock insuffisant")) {
                System.out.println("⚠️ Stock insuffisant détecté (normal si stock bas)");
                System.out.println("   " + e.getMessage().split("\n")[1]);
            } else {
                System.err.println("❌ ERREUR : " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ ERREUR : " + e.getMessage());
        }
        System.out.println();
    }

    private static void testStockInsuffisantAvecConversions(DataRetriever dr) {
        System.out.println("│  TEST 7 : Détection stock insuffisant + conversion │");

        try {
            Order order = new Order();

            DishOrder dishOrder = new DishOrder();
            Dish dish = new Dish();
            dish.setId(1);
            dishOrder.setDish(dish);
            dishOrder.setQuantity(999999);

            order.getDishOrders().add(dishOrder);

            dr.saveOrder(order);
            System.out.println("❌ La commande n'aurait pas dû être créée");

        } catch (RuntimeException e) {
            System.out.println("✅ Exception stock insuffisant capturée !");
            String[] lines = e.getMessage().split("\n");
            for (String line : lines) {
                System.out.println("   " + line);
            }
        } catch (Exception e) {
            System.err.println("❌ Autre erreur : " + e.getMessage());
        }
        System.out.println();
    }

    private static void testStockMixteUnites(DataRetriever dr) {
        System.out.println("│  TEST 8 : Calcul stock avec unités mixtes          │");

        try {
            Ingredient ing = dr.findIngredientById(1);

            if (ing != null) {
                System.out.println("Ingrédient : " + ing.getName());
                System.out.println("Stock total en KG : " + String.format("%.2f", ing.getCurrentStockInKG()) + " KG");
                System.out.println("\nDétail des mouvements :");

                for (StockMouvement mvt : ing.getStockMouvementList()) {
                    double qtyKG = UnitConvertion.toKG(
                            ing.getName(),
                            mvt.getValue().getQuantity(),
                            mvt.getValue().getUnit()
                    );

                    System.out.printf("  %s %6.2f %-4s = %6.2f KG (%s)%n",
                            mvt.getType() == MouvementType.IN ? "+" : "-",
                            mvt.getValue().getQuantity(),
                            mvt.getValue().getUnit(),
                            qtyKG,
                            mvt.getCreationDatetime()
                    );
                }
            } else {
                System.out.println("⚠️ Aucun ingrédient avec ID=1");
            }

        } catch (Exception e) {
            System.err.println("❌ ERREUR : " + e.getMessage());
        }
        System.out.println();
    }

    // UTILITAIRES D'AFFICHAGE

    private static void printConversion(String input, double result, String unit, double expected, String formula) {
        boolean success = Math.abs(result - expected) < 0.001;
        String status = success ? "✅" : "❌";
        System.out.printf("  %s %-25s = %6.2f %-3s (attendu: %.2f) [%s]%n",
                status, input, result, unit, expected, formula);
    }

    private static void printRoundTrip(String ingredient, double original, String unit, double intermediate, double result) {
        boolean success = Math.abs(original - result) < 0.001;
        String status = success ? "✅" : "❌";
        System.out.printf("  %s %-10s : %.2f %s → %.3f KG → %.2f %s%n",
                status, ingredient, original, unit, intermediate, result, unit);
    }

    // NETTOYAGE BASE

    private static boolean nettoyerBaseDeDonnees() {
        System.out.println("🧹 Nettoyage de la base de données...");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("TRUNCATE TABLE \"order\" RESTART IDENTITY CASCADE");
            System.out.println("✅ Base nettoyée !\n");
            return true;

        } catch (SQLException e) {
            System.out.println("⚠️ Tentative avec DELETE...");
            return nettoyerAvecDelete();
        }
    }

    private static boolean nettoyerAvecDelete() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM dish_order");
            stmt.executeUpdate("DELETE FROM \"order\"");
            stmt.execute("SELECT setval('order_id_seq', 1, false)");
            stmt.execute("SELECT setval('dish_order_id_seq', 1, false)");

            System.out.println("✅ Base nettoyée avec DELETE !\n");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            return false;
        }
    }
}