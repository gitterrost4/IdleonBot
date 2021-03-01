package de.gitterrost4.idleonbot.listeners;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.gitterrost4.botlib.containers.ChoiceMenu;
import de.gitterrost4.botlib.containers.ChoiceMenu.ChoiceMenuBuilder;
import de.gitterrost4.botlib.containers.ChoiceMenu.MenuEntry;
import de.gitterrost4.botlib.containers.CommandMessage;
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

  public Map<String, ChoiceMenu> activeMenus = new HashMap<>();

  public RecipeListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getRecipeConfig(), "recipe");
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage message) {
    String name = message.getArgOrThrow(0, true);
    String baseUrl = "https://idleon.info";
    String url = baseUrl + "/w/index.php?sort=relevance&fulltext=1&search=" + name.replaceAll(" ", "+");
    try {
      Document searchDoc = Jsoup.connect(url).get();
      Elements h2 = searchDoc.select(".searchresults h2");
      if (h2.size() == 0) {
        event.getChannel().sendMessage("Item '" + name + "' not found.").queue();
        return;
      }
      Elements anchors = searchDoc.selectFirst(".searchresults ul").select("a");
      if (anchors.size() == 1) {
        showRecipe(event, baseUrl, new MenuEntry(anchors.get(0).text(), anchors.get(0).attr("href")));
      } else {
        ChoiceMenuBuilder menuBuilder = ChoiceMenu.builder();
        anchors.stream().forEach(anchor->menuBuilder.addEntry(new MenuEntry(anchor.text(), anchor.attr("href"))));
        menuBuilder.setChoiceHandler(menuEntry -> {
          showRecipe(event, baseUrl, menuEntry);
        });
        menuBuilder.setTitle("Recipe Search");

        ChoiceMenu menu = menuBuilder.build();
        activeMenus.put(menu.display(event.getChannel()), menu);
      }
    } catch (IOException e) {
      getLogger().error("Could not connect to the wiki at " + url,e);
    }
  }

  private static void showRecipe(MessageReceivedEvent event, String baseUrl, MenuEntry menuEntry) {
    String itemUrl = baseUrl + menuEntry.getValue();
    Document doc = Catcher.wrap(() -> Jsoup.connect(itemUrl).get());
    String itemName = doc.selectFirst("#firstHeading").text();
    Elements rows = doc.select(".forgeslot tr");
    if (rows.size() == 0) {
      event.getChannel().sendMessage("Item '" + menuEntry.getDisplay() + "' does not appear to have a recipe.").queue();
      return;
    }
    Map<String, String> ingredients = rows.stream().map(row -> row.select("td")).filter(tds -> tds.size() == 2)
        .filter(tds -> !tds.get(0).text().startsWith("Anvil Tab") && !tds.get(0).text().startsWith("Recipe From"))
        .collect(Collectors.toMap(tds -> tds.get(0).text(), tds -> tds.get(1).text()));
    String from = rows.stream().map(row -> row.select("td")).filter(tds -> tds.size() == 2)
        .filter(tds -> tds.get(0).text().startsWith("Recipe From")).findFirst().map(tds->tds.get(1).text()).orElse("");
    String imageUrl = doc.select(".infobox-image img").attr("src");
    MessageEmbed mainEmbed = new EmbedBuilder()
        .addField("Ingredients", ingredients.entrySet().stream()
            .map(e -> "- " + e.getKey() + " " + e.getValue()).collect(Collectors.joining("\n")), false)
        .addField("Recipe From", from, false)
        .setAuthor(itemName, itemUrl, imageUrl)
        .build();
    event.getChannel()
        .sendMessage(mainEmbed)
        .queue();
  }

  @Override
  protected void messageReactionAdd(MessageReactionAddEvent event) {
    super.messageReactionAdd(event);
    if (activeMenus.containsKey(event.getMessageId())) {
      activeMenus.get(event.getMessageId()).handleReaction(event);
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
