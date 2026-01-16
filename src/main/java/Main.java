import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {

        DataRetriever dataRetriever = new DataRetriever();
        Dish dish1 =  dataRetriever.findDishById(1);
        System.out.println(dish1);

        //-----------------------------------------------------------
        Dish dishToUpdate = dataRetriever.findDishById(3);
        System.out.println(dishToUpdate.getPrice());
        dishToUpdate.setPrice(5000.0);
        System.out.println("Marge de dishToUpdate: " + dishToUpdate.getPrice());
        Dish saved = dataRetriever.saveDish(dishToUpdate);
        System.out.println("plat mis à jour: " + saved);
        System.out.println("Nouvelle marge : " + saved.getCrossMargin());

        //--------------------------------------------------
       /*

        System.out.println("Marge de dish1: " + dish1.getCrossMargin());
        Dish dish2 =  dataRetriever.findDishById(2);
        System.out.println("Marge de dish2: " + dish2.getCrossMargin());
        System.out.println(dataRetriever.findIngredient(3,5));
        dataRetriever.findDishByIngredientName("crème fraiche").forEach(System.out::println);
        dataRetriever.findIngredientByCriteria("poulet", CategoryEnum.ANIMAL, "poulet grillé", 1, 1).forEach(System.out::println);


       List<Ingredient> newIngredients = new ArrayList<>();
        Dish existingDish = dataRetriever.findDishById(1);
        if (existingDish != null) {
            System.out.println("Plat trouvé: " + existingDish.getName());
        }
        else {
            System.out.println("Le plat n'existe pas");
        }

        Ingredient i1 = new Ingredient();
        i1.setName("Crème fraiche");
        i1.setPrice(50.00);
        i1.setCategory(CategoryEnum.DAIRY);
        i1.setDish(existingDish);
        Ingredient i2 = new Ingredient();
        i2.setName("Huile d'olive");
        i2.setPrice(80.00);
        i2.setCategory(CategoryEnum.DAIRY);
        i2.setDish(existingDish);
        newIngredients.add(i2);
        List<Ingredient> created = dataRetriever.createIngredients(newIngredients);
        created.forEach(System.out::println);
        //-----------------------------------------------------------------

        */
        //----------------------------------------------------

        /* Dish newDish = new Dish();
        newDish.setName("Pizza Margherita");
        newDish.setDishType(DishTypeEnum.MAIN);  // Assure-toi que ton enum s'appelle comme ça

        List<Ingredient> ingredients = new ArrayList<>();
        Ingredient ing1 = new Ingredient();
        ing1.setName("Tomate");
        ing1.setPrice(2.5);
        ing1.setCategory(CategoryEnum.VEGETABLE);
        ing1.setDish(newDish);
        ingredients.add(ing1);
        Ingredient ing2 = new Ingredient();
        ing2.setName("Mozzarella");
        ing2.setPrice(4.0);
        ing2.setCategory(CategoryEnum.DAIRY);
        ing2.setDish(newDish);
        ingredients.add(ing2);
        newDish.setIngredients(ingredients);

        Dish savedDish = dataRetriever.saveDish(newDish);
        System.out.println("Plat créé avec ID : " + savedDish.getId());
        System.out.println("Ingrédients sauvegardés : " + savedDish.getIngredients().size());

        // Test 2 : Modification d'un plat existant
        savedDish.setName("Pizza Margherita Modifiée");
        savedDish.setDishType(DishTypeEnum.MAIN);

        // Modifier/ajouter/supprimer des ingrédients
        savedDish.getIngredients().remove(0);  // Supprime la tomate
        Ingredient ing3 = new Ingredient();
        ing3.setName("Basilic");
        ing3.setPrice(1.5);
        ing3.setCategory(CategoryEnum.VEGETABLE);
        ing3.setDish(savedDish);
        savedDish.getIngredients().add(ing3);

        Dish updatedDish = dataRetriever.saveDish(savedDish);
        System.out.println("Plat mis à jour, ID : " + updatedDish.getId());
        System.out.println("Nouveau nom : " + updatedDish.getName());
        System.out.println("Ingrédients après mise à jour : " + updatedDish.getIngredients().size());*/

    }
}
