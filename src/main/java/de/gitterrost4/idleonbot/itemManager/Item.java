package de.gitterrost4.idleonbot.itemManager;

import java.util.List;
import java.util.Optional;

public class Item {
  private String id;
  private String name;
  private List<Ingredient> ingredients;
  
  public Item(String id, String name, List<Ingredient> ingredients) {
    super();
    this.id = id;
    this.name = Optional.ofNullable(name).map(n->n.replaceAll("[_|]", " ")).orElseGet(()->{ System.err.println("Item "+id+" has no name"); return null;});
    this.ingredients = ingredients;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<Ingredient> getIngredients() {
    return ingredients;
  }

  @Override
  public String toString() {
    return "Item [\nid=" + id + ", \nname=" + name + ", \ningredients=" + ingredients + "\n]";
  }
  
  
  
}
