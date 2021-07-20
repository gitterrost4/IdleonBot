package de.gitterrost4.idleonbot.listeners;

import java.util.stream.Collectors;

import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import de.gitterrost4.idleonbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
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
    String ign = message.getArgOrThrow(0, true);
    guild().getTextChannelById(config.getIgnConfig().getIgnMessageChannelId())
        .sendMessage(ign + " is the IGN of: " + event.getAuthor().getAsMention()).queue();
    connectionHelper.update("replace into idleonign (discordid, ign) VALUES (?,?)", event.getAuthor().getId(), ign);
  }

}
