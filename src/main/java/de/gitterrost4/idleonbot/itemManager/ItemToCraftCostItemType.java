package de.gitterrost4.idleonbot.itemManager;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;

class ItemToCraftCostItemType {
  List<ItemToCraftCostIngredientType> ingredients;
  
  @JsonCreator
  public ItemToCraftCostItemType(List<List<String>> input) {
    ingredients = input.stream().map(ItemToCraftCostIngredientType::new).collect(Collectors.toList());
  }

  public List<ItemToCraftCostIngredientType> getIngredients() {
    return ingredients;
  }
  
  

}
