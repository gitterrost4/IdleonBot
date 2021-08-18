package de.gitterrost4.idleonbot.itemManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ItemList {
  public static final ItemList instance;
  static {
    try {
    FileInputStream fi = new FileInputStream("ItemToCraftCostName.json");
    ObjectMapper mapper = new ObjectMapper();
    ItemToCraftCostName tree = mapper.readValue(fi,ItemToCraftCostName.class);
    FileInputStream costFi = new FileInputStream("ItemToCraftCostType.json");
    ItemToCraftCostType costs = mapper.readValue(costFi,ItemToCraftCostType.class);
    FileInputStream itemNamesFi = new FileInputStream("ItemNames.json");    
    Map<String,String> itemNames = mapper.readValue(itemNamesFi, new TypeReference<Map<String,String>>(){});
    instance = new ItemList(tree, itemNames ,costs);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

  }
  
  Map<String, Item> items;

  public ItemList(ItemToCraftCostName ids, Map<String, String> itemNames, ItemToCraftCostType ingreds) {
    Map<String, Item> craftableItems = IntStream.range(0, ids.getTabNames().size())
        .mapToObj(i -> new ItemTab(ids.getTabNames().get(i), ingreds.getTabTypes().get(i)))
        .flatMap(tab -> IntStream.range(0, tab.tabNames.names.size())
            .mapToObj(j -> new Item(tab.tabNames.getNames().get(j), itemNames.get(tab.tabNames.getNames().get(j)),
                tab.tabTypes.getItemType().get(j).getIngredients().stream()
                    .map(in -> new Ingredient(in.getCount(), in.getItemId())).collect(Collectors.toList()))))
        .collect(Collectors.toMap(i -> i.getId(), i -> i, (a1, a2) -> a1));
    items = itemNames.entrySet().stream()
        .map(e->
          Optional.ofNullable(craftableItems.get(e.getKey()))
            .orElseGet(()->new Item(e.getKey(),e.getValue(),new ArrayList<>()))).collect(Collectors.toMap(i->i.getId(), i->i));
  }

  public List<Item> findByName(String searchString) {
    return items.entrySet().stream().map(Entry::getValue).filter(i -> i.getName().matches("(?i).*"+searchString+".*"))
        .collect(Collectors.toList());
  }

  public Map<String, Item> getItems() {
    return items;
  }

  private class ItemTab {
    ItemToCraftCostTabName tabNames;
    ItemToCraftCostTabType tabTypes;

    public ItemTab(ItemToCraftCostTabName tabNames, ItemToCraftCostTabType tabTypes) {
      super();
      this.tabNames = tabNames;
      this.tabTypes = tabTypes;
    }

  }

  @Override
  public String toString() {
    return "ItemList [items=\n" + items + "\n]";
  }

}
