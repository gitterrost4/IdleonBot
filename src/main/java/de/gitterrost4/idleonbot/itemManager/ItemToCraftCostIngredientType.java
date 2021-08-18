package de.gitterrost4.idleonbot.itemManager;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

class ItemToCraftCostIngredientType {
  Integer count;
  String itemId;
  
  @JsonCreator
  public ItemToCraftCostIngredientType(List<String> input) {
    itemId=input.get(0);
    count = Integer.parseInt(input.get(1));
  }

  public Integer getCount() {
    return count;
  }

  public String getItemId() {
    return itemId;
  }
  
  
}
