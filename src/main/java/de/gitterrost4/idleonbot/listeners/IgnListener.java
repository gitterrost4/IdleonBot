package de.gitterrost4.idleonbot.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import de.gitterrost4.idleonbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class IgnListener extends AbstractMessageListener<ServerConfig> {

  public IgnListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getIgnConfig(), "ign");
    connectionHelper.update(
        "create table if not exists idleonign (id INTEGER PRIMARY KEY not null, discordid text not null UNIQUE, ign text not null);");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage message) {
    if (message.getArg(0).filter(x -> x.equals("display")).isPresent()) {
      event.getChannel()
          .sendMessage(event.getMessage().getMentionedUsers().stream()
              .map(u -> connectionHelper.getFirstResult("select ign from idleonign where discordid=?",
                  rs -> rs.getString("ign"), u.getId()).map(ign -> ign + " is the IGN of: " + u.getAsMention())
                  .orElse(u.getAsMention() + "'s IGN is unknown to me."))
              .collect(Collectors.joining("\n")))
          .queue();
      return;
    }
    if (message.getArg(0).filter(x -> x.equals("lookup")).isPresent()) {
      String ign = message.getArgOrThrow(1, true);
      event.getChannel()
          .sendMessage(connectionHelper.getFirstResult("select discordid from idleonign where ign=?",
              rs -> rs.getString("discordid"), ign)
              .map(discordId -> ign + " is the IGN of: " + guild().getMemberById(discordId).getAsMention())
              .orElse(ign + " is unknown to me."))
          .queue();
      return;
    }
    if (message.getArg(0).filter(x -> x.equals("showall")).isPresent()) {
      splitString(connectionHelper
          .getResults("select discordid,ign from idleonign",
              rs -> rs.getString("ign") + " is the IGN of: "
                  + guild().getMemberById(rs.getString("discordid")).getAsMention())
          .stream().collect(Collectors.joining("\n")), 2000).stream()
              .forEach(msg -> event.getChannel().sendMessage(msg).queue());
      return;
    }
    String ign = message.getArgOrThrow(0, true);
    guild().getTextChannelById(config.getIgnConfig().getIgnMessageChannelId())
        .sendMessage(ign + " is the IGN of: " + event.getAuthor().getAsMention()).queue();
    connectionHelper.update("replace into idleonign (discordid, ign) VALUES (?,?)", event.getAuthor().getId(), ign);
  }

  private static List<String> splitString(String string, Integer limit) {
    if (string.length() < limit) {
      return Stream.of(string).collect(Collectors.toList());
    }
    List<String> result = new ArrayList<>();
    while (string.length() >= limit) {
      result.add(0, string.substring(string.indexOf("\n", string.length() - limit) + 1));
      string = string.substring(0, string.indexOf("\n", string.length() - limit) + 1);
    }
    result.add(0, string);
    return result;
  }
}
