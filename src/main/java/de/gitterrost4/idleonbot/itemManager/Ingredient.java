package de.gitterrost4.idleonbot.itemManager;

public class Ingredient {
  private Integer count;
  private String itemId;
  
  public Ingredient(Integer count, String itemId) {
    super();
    this.count = count;
    this.itemId = itemId;
  }
  
  public Integer getCount() {
    return count;
  }
  
  public String getItemId() {
    return itemId;
  }
  
  @Override
  public String toString() {
    return "Ingredient [\ncount=" + count + ", \nitemId=" + itemId + "\n]";
  }
  
  
}
