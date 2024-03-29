package de.gitterrost4.idleonbot.listeners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

public class WikiListener extends AbstractMessageListener<ServerConfig> {

  public WikiListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getWikiConfig(), "wiki");
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
        showWiki(event, baseUrl, new MenuEntry(anchors.get(0).text(), anchors.get(0).attr("href")));
      } else {
        ChoiceMenuBuilder menuBuilder = ChoiceMenu.builder();
        anchors.stream().forEach(anchor -> menuBuilder.addEntry(new MenuEntry(anchor.text(), anchor.attr("href"))));
        menuBuilder.setChoiceHandler(menuEntry -> {
          showWiki(event, baseUrl, menuEntry);
        });
        menuBuilder.setTitle("Recipe Search");

        ChoiceMenu menu = menuBuilder.build();
        menu.display(event.getChannel());
      }
    } catch (IOException e) {
      getLogger().error("Could not connect to the wiki at " + url, e);
    }
  }

  private static void showWiki(MessageReceivedEvent event, String baseUrl, MenuEntry menuEntry) {
    String itemUrl = baseUrl + menuEntry.getValue();
    Document doc = Catcher.wrap(() -> Jsoup.connect(itemUrl).get());
    String itemName = doc.selectFirst("#firstHeading").text();
    Element infoBox = doc.selectFirst(".infobox");
    if (infoBox == null) {
      event.getChannel().sendMessage("Couldn't parse wiki page for " + itemName).queue();
      return;
    }
    String imageUrl = Optional.ofNullable(infoBox.select(".infobox-image img").attr("src")).map(String::trim)
        .filter(x -> !x.isEmpty()).orElseGet(() -> infoBox.select(".HeaderImage img").attr("src"));
    Elements rows = infoBox.select("tr");
    if (rows.size() == 0) {
      event.getChannel().sendMessage("Item '" + menuEntry.getDisplay() + "' does not appear to have any info.").queue();
      return;
    }
    List<MessageEmbed> pages = new ArrayList<>();
    EmbedBuilder builder = new EmbedBuilder();
    rows.forEach(row -> {
      Optional.ofNullable(row.selectFirst("th.subheader")).ifPresent(th -> {
        if (!builder.isEmpty()) {
          pages.add(builder.build());
        }
        String description = th.text();
        startNewEmbed(builder, itemUrl, itemName, imageUrl, description);
      });
      Optional.ofNullable(row.selectFirst("th")).filter(th -> th.classNames().size() == 0)
          .ifPresent(th -> Optional.ofNullable(row.selectFirst("td")).ifPresent(td -> {
            if (builder.isEmpty()) {
              startNewEmbed(builder, itemUrl, itemName, imageUrl, "General");
            }
            builder.addField(th.text(), td.text(), true);
          }));
    });
    if (!builder.isEmpty()) {
      pages.add(builder.build());
    }
    PagedEmbed pagedEmbed = new PagedEmbed(pages);
    pagedEmbed.display(event.getChannel());
  }

  private static void startNewEmbed(EmbedBuilder builder, String itemUrl, String itemName, String imageUrl,
      String description) {
    builder.clear()
        .setAuthor(itemName, Optional.ofNullable(itemUrl).map(String::trim).filter(x -> !x.isEmpty()).orElse(null),
            Optional.ofNullable(imageUrl).map(String::trim).filter(x -> !x.isEmpty()).orElse(null))
        .setDescription("__***" + description + "***__");
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
