package de.gitterrost4.idleonbot.itemManager;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;

class ItemToCraftCostName {
  List<ItemToCraftCostTabName> tabNames;

  @JsonCreator
  ItemToCraftCostName(List<List<String>> input){
    tabNames=input.stream().map(ItemToCraftCostTabName::new).collect(Collectors.toList());
  }
  
  public List<ItemToCraftCostTabName> getTabNames() {
    return tabNames;
  }
  
  
}
