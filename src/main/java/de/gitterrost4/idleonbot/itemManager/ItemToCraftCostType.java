package de.gitterrost4.idleonbot.itemManager;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;

class ItemToCraftCostType {
  List<ItemToCraftCostTabType> tabTypes;
  
  @JsonCreator
  public ItemToCraftCostType(List<List<List<List<String>>>> input) {
    tabTypes = input.stream().map(ItemToCraftCostTabType::new).collect(Collectors.toList());
  }

  public List<ItemToCraftCostTabType> getTabTypes() {
    return tabTypes;
  }
  
  
}
