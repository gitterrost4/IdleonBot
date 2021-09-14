package de.gitterrost4.idleonbot.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import de.gitterrost4.botlib.containers.ChoiceMenu;
import de.gitterrost4.botlib.containers.ChoiceMenu.ChoiceMenuBuilder;
import de.gitterrost4.botlib.containers.ChoiceMenu.MenuEntry;
import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.containers.PagedEmbed;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import de.gitterrost4.idleonbot.config.containers.ServerConfig;
import de.gitterrost4.idleonbot.itemManager.Item;
import de.gitterrost4.idleonbot.itemManager.ItemList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RecipeListener extends AbstractMessageListener<ServerConfig> {

  public RecipeListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getRecipeConfig(), "recipe");
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage message) {
    Optional<String> oCount=message.getArg(0).filter(StringUtils::isNumeric);
    Integer count = oCount.map(Integer::parseInt).orElse(1);
    Integer startIndex = oCount.map(x->1).orElse(0);
    String name = message.getArgOrThrow(startIndex, true);
    List<Item> possibleItems = ItemList.instance.findByName(name);
    if(possibleItems.size()==0) {
      event.getChannel().sendMessage("Item '" + name + "' not found.").queue();
      return;      
    } else if(possibleItems.size()==1) {
      showRecipe(event, possibleItems.get(0),count);      
    } else {
      ChoiceMenuBuilder menuBuilder = ChoiceMenu.builder();
      possibleItems.stream().forEach(item -> menuBuilder.addEntry(new MenuEntry(item.getName(), item.getId())));
      menuBuilder.setChoiceHandler(menuEntry -> {
        showRecipe(event, ItemList.instance.getItems().get(menuEntry.getValue()),count);
      });
      menuBuilder.setTitle("Recipe Search");

      ChoiceMenu menu = menuBuilder.build();
      menu.display(event.getChannel());
    }
  }
  
  private void showRecipe(MessageReceivedEvent event, Item item, Integer count) {
    de.gitterrost4.idleonbot.itemManager.Ingredient ing = new de.gitterrost4.idleonbot.itemManager.Ingredient(count, item.getId());
    PagedEmbed pagedEmbed = new PagedEmbed(getItemEmbed(ing),getRawIngredientsEmbed(ing),getIngredientTreeEmbed(ing));
    pagedEmbed.display(event.getChannel());
  }
  
  MessageEmbed getItemEmbed(de.gitterrost4.idleonbot.itemManager.Ingredient item) {
    return new EmbedBuilder()
        .addField("Ingredients","```"+
            ItemList.instance.getItems().get(item.getItemId()).getIngredients().stream().map(e -> {
              System.err.println(e.getItemId());
                  return "- " + 
            ItemList.instance.getItems().get(e.getItemId()).getName() 
            + " x" + 
            item.getCount()*e.getCount();
                })
            .collect(Collectors.joining("\n"))+"```", false)
        //.addField("Recipe From", baseIngredient.recipeFrom, false)
        .setAuthor((item.getCount()>1?item.getCount()+"x ":"")+ItemList.instance.getItems().get(item.getItemId()).getName(), null,null)
        .build();
  }
  
  private List<de.gitterrost4.idleonbot.itemManager.Ingredient> getRawMaterials(de.gitterrost4.idleonbot.itemManager.Ingredient item) {
    System.err.println(ItemList.instance.getItems().get(item.getItemId()));
    if (ItemList.instance.getItems().get(item.getItemId()).getIngredients().isEmpty()) {
      return Stream.of(item).collect(Collectors.toList());
    }
    return ItemList.instance.getItems().get(item.getItemId()).getIngredients().stream().map(x->new de.gitterrost4.idleonbot.itemManager.Ingredient(item.getCount()*x.getCount(),x.getItemId())).map(x->getRawMaterials(x)).flatMap(List::stream).collect(Collectors.toList());
  }
  
  MessageEmbed getRawIngredientsEmbed(de.gitterrost4.idleonbot.itemManager.Ingredient item) {
    String lines = getRawMaterials(item).stream().collect(Collectors.groupingBy(i -> ItemList.instance.getItems().get(i.getItemId()).getName())).entrySet().stream()
        .map(e -> "- " + e.getKey() + " x" + e.getValue().stream().map(i -> i.getCount()).reduce(0, (a, b) -> a + b))
        .collect(Collectors.joining("\n"));
    return new EmbedBuilder().addField("Raw ingredients","```"+
        (lines.length()>1000?lines.substring(0, 1000)+"...":lines)+"```",
        false).setAuthor((item.getCount()>1?item.getCount()+"x ":"")+ItemList.instance.getItems().get(item.getItemId()).getName(), null, null).build();
  }

  private List<String> getTreeStringLines(String prefix, de.gitterrost4.idleonbot.itemManager.Ingredient item) {
    List<String> result = new ArrayList<>();
    for (int index = 0; index < ItemList.instance.getItems().get(item.getItemId()).getIngredients().size(); index++) {
      de.gitterrost4.idleonbot.itemManager.Ingredient currentIngredient = ItemList.instance.getItems().get(item.getItemId()).getIngredients().get(index);
      de.gitterrost4.idleonbot.itemManager.Ingredient ingred = new de.gitterrost4.idleonbot.itemManager.Ingredient(item.getCount()*currentIngredient.getCount(), currentIngredient.getItemId());
      if (index == ItemList.instance.getItems().get(item.getItemId()).getIngredients().size() - 1) {
        result.add(prefix + "└── " + ItemList.instance.getItems().get(ingred.getItemId()).getName() + " x" + ingred.getCount());
        if (ItemList.instance.getItems().get(ingred.getItemId()).getIngredients().size() > 0) {
          result.addAll(getTreeStringLines(prefix + "    ",ingred));
        }
      } else {
        result.add(prefix + "├── " + ItemList.instance.getItems().get(ingred.getItemId()).getName() + " x" + ingred.getCount());
        if (ItemList.instance.getItems().get(ingred.getItemId()).getIngredients().size() > 0) {
          result.addAll(getTreeStringLines(prefix + "│   ",ingred));
        }
      }
    }
    return result;
  }

  MessageEmbed getIngredientTreeEmbed(de.gitterrost4.idleonbot.itemManager.Ingredient item) {
    String treelines = getTreeStringLines("",item).stream().collect(Collectors.joining("\n"));
    
    return new EmbedBuilder()
        .addField("Ingredient Tree",
            "```" + (treelines.length()>1000?treelines.substring(0, 1000)+"...":treelines) + "```", false)
        .setAuthor((item.getCount()>1?item.getCount()+"x ":"")+ItemList.instance.getItems().get(item.getItemId()).getName(), null, null).build();
  }

  @Override
  protected String shortInfoInternal() {
    return "Display an item's recipe";
  }

  @Override
  protected String usageInternal() {
    return commandString("[SEARCHSTRING]");
  }

  @Override
  protected String descriptionInternal() {
    return "Display the recipe for an item as found on the idleon wiki. If SEARCHSTRING matches multiple items, you can interactively choose which one you want.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("Iron Boots") + "\n" + "Show the recipe for Iron Boots.\n" + commandString("Boots") + "\n"
        + "Show a selection of Boots to choose from.";
  }

}
