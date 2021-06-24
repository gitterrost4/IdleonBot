package de.gitterrost4.idleonbot.listeners;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.gitterrost4.botlib.containers.ChoiceMenu;
import de.gitterrost4.botlib.containers.ChoiceMenu.ChoiceMenuBuilder;
import de.gitterrost4.botlib.containers.ChoiceMenu.MenuEntry;
import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.containers.PagedEmbed;
import de.gitterrost4.botlib.helpers.Catcher;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import de.gitterrost4.idleonbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class RecipeListener extends AbstractMessageListener<ServerConfig> {

  private static final String BASE_URL = "https://idleon.info";
  public Map<String, ChoiceMenu> activeMenus = new HashMap<>();
  public Map<String, PagedEmbed> activePagedEmbeds = new HashMap<>();
  public static AsyncLoadingCache<String, BaseIngredient> ingredientCache = Caffeine.newBuilder().refreshAfterWrite(Duration.ofHours(1)).buildAsync(name->new BaseIngredient(name));
  
  
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
    String url = BASE_URL + "/w/index.php?sort=relevance&fulltext=1&search=" + name.replaceAll(" ", "+");
    try {
      Document searchDoc = Jsoup.connect(url).get();
      Elements h2 = searchDoc.select(".searchresults h2");
      if (h2.size() == 0) {
        event.getChannel().sendMessage("Item '" + name + "' not found.").queue();
        return;
      }
      Elements anchors = searchDoc.selectFirst(".searchresults ul").select("a");
      if (anchors.size() == 1) {
        showRecipe(event, new MenuEntry(anchors.get(0).text(), anchors.get(0).attr("href")),count);
      } else {
        ChoiceMenuBuilder menuBuilder = ChoiceMenu.builder();
        anchors.stream().forEach(anchor -> menuBuilder.addEntry(new MenuEntry(anchor.text(), anchor.attr("href"))));
        menuBuilder.setChoiceHandler(menuEntry -> {
          showRecipe(event, menuEntry,count);
        });
        menuBuilder.setTitle("Recipe Search");

        ChoiceMenu menu = menuBuilder.build();
        activeMenus.put(menu.display(event.getChannel()), menu);
      }
    } catch (IOException e) {
      getLogger().error("Could not connect to the wiki at " + url, e);
    }
  }

  private void showRecipe(MessageReceivedEvent event, MenuEntry menuEntry, Integer count) {
    String itemName = menuEntry.getDisplay();
    ingredientCache.get(itemName).thenAccept(ing->{
    Ingredient item = new Ingredient(count,ing);
    PagedEmbed pagedEmbed = new PagedEmbed(item.getItemEmbed(), item.getRawIngredientsEmbed(),
        item.getIngredientTreeEmbed());
    activePagedEmbeds.put(pagedEmbed.display(event.getChannel()), pagedEmbed);
    });
  }

  static class Ingredient{
    private final Integer count;
    private final BaseIngredient baseIngredient;
    
    public Ingredient(Integer count, BaseIngredient ingredient) {
      super();
      this.count = count;
      this.baseIngredient = ingredient;
    }
    
    private List<Ingredient> getRawMaterials() {
      if (baseIngredient.ingredients.isEmpty()) {
        return Stream.of(this).collect(Collectors.toList());
      }
      return baseIngredient.ingredients.stream().map(x->new Ingredient(count*x.count,x.baseIngredient)).map(x->x.getRawMaterials()).flatMap(List::stream).collect(Collectors.toList());
    }
    
    MessageEmbed getItemEmbed() {
      return new EmbedBuilder()
          .addField("Ingredients","```"+
              baseIngredient.ingredients.stream().map(e -> "- " + e.baseIngredient.name + " x" + count*e.count).collect(Collectors.joining("\n"))+"```", false)
          .addField("Recipe From", baseIngredient.recipeFrom, false).setAuthor((count>1?count+"x ":"")+baseIngredient.name, baseIngredient.itemUrl, baseIngredient.iconUrl).build();
    }

    MessageEmbed getRawIngredientsEmbed() {
      return new EmbedBuilder().addField("Raw ingredients","```"+
          getRawMaterials().stream().collect(Collectors.groupingBy(i -> i.baseIngredient.name)).entrySet().stream()
              .map(e -> "- " + e.getKey() + " x" + e.getValue().stream().map(i -> i.count).reduce(0, (a, b) -> a + b))
              .collect(Collectors.joining("\n"))+"```",
          false).setAuthor((count>1?count+"x ":"")+baseIngredient.name, baseIngredient.itemUrl, baseIngredient.iconUrl).build();
    }

    MessageEmbed getIngredientTreeEmbed() {
      return new EmbedBuilder()
          .addField("Ingredient Tree",
              "```" + getTreeStringLines("").stream().collect(Collectors.joining("\n")) + "```", false)
          .setAuthor((count>1?count+"x ":"")+baseIngredient.name, baseIngredient.itemUrl, baseIngredient.iconUrl).build();
    }

    private List<String> getTreeStringLines(String prefix) {
      List<String> result = new ArrayList<>();
      for (int index = 0; index < baseIngredient.ingredients.size(); index++) {
        Ingredient currentIngredient = baseIngredient.ingredients.get(index);
        Ingredient ingred = new Ingredient(count*currentIngredient.count, currentIngredient.baseIngredient);
        if (index == baseIngredient.ingredients.size() - 1) {
          result.add(prefix + "└── " + ingred.baseIngredient.name + " x" + ingred.count);
          if (ingred.baseIngredient.ingredients.size() > 0) {
            result.addAll(ingred.getTreeStringLines(prefix + "    "));
          }
        } else {
          result.add(prefix + "├── " + ingred.baseIngredient.name + " x" + ingred.count);
          if (ingred.baseIngredient.ingredients.size() > 0) {
            result.addAll(ingred.getTreeStringLines(prefix + "│   "));
          }
        }
      }
      return result;
    }

    @Override
    public String toString() {
      return "Ingredient [count=" + count + ", ingredient=" + baseIngredient + "]";
    }
  }
  
  static class BaseIngredient {
    private final String name;
    private final String itemUrl;
    private final String iconUrl;
    private final String recipeFrom;
    private final List<Ingredient> ingredients;

    public BaseIngredient(String name) {
      super();
      this.name = name;
      this.itemUrl = BASE_URL + "/wiki/" + name.replaceAll(" ", "_");
      Document doc = Catcher.wrap(() -> Jsoup.connect(itemUrl).get());
      Elements rows = doc.select(".forgeslot tr");
      this.ingredients = rows.stream().map(row -> row.select("td")).filter(tds -> tds.size() == 2)
          .filter(tds -> !tds.get(0).text().startsWith("Anvil Tab") && !tds.get(0).text().startsWith("Recipe From"))
          .map(tds -> new Ingredient(Integer.parseInt(tds.get(1).text().replaceAll("x", "")),
              Catcher.wrap(() -> ingredientCache.get(tds.get(0).text()).get())))
          .collect(Collectors.toList());
      recipeFrom = rows.stream().map(row -> row.select("td")).filter(tds -> tds.size() == 2)
          .filter(tds -> tds.get(0).text().startsWith("Recipe From")).findFirst().map(tds -> tds.get(1).text())
          .orElse("");
      iconUrl = Optional.ofNullable(doc.select(".infobox-image img").attr("src")).map(String::trim).filter(x->!x.isEmpty()).orElseGet(() -> Optional.ofNullable(doc.select(".HeaderImage img").attr("src")).orElse(null));
    }

    @Override
    public String toString() {
      return "BaseIngredient [name=" + name + ", ingredients=" + ingredients + "]";
    }


  }

  @Override
  protected void messageReactionAdd(MessageReactionAddEvent event) {
    super.messageReactionAdd(event);
    if (activeMenus.containsKey(event.getMessageId())) {
      activeMenus.get(event.getMessageId()).handleReaction(event);
    }
    if (activePagedEmbeds.containsKey(event.getMessageId())) {
      activePagedEmbeds.get(event.getMessageId()).handleReaction(event);
    }
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
