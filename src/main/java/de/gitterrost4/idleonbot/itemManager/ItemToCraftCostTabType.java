package de.gitterrost4.idleonbot.itemManager;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;

class ItemToCraftCostTabType {
  List<ItemToCraftCostItemType> itemTypes;

  public List<ItemToCraftCostItemType> getItemType() {
    return itemTypes;
  }

  @JsonCreator
  public ItemToCraftCostTabType(List<List<List<String>>> input) {
    itemTypes = input.stream().map(ItemToCraftCostItemType::new).collect(Collectors.toList());
  }

}
