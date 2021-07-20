package de.gitterrost4.idleonbot.listeners;

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
    String ign = message.getArgOrThrow(0, true);
    guild().getTextChannelById(config.getIgnConfig().getIgnMessageChannelId())
        .sendMessage(ign + " is the IGN of: " + event.getAuthor().getAsMention()).queue();
    connectionHelper.update("replace into idleonign (discordid, ign) VALUES (?,?)", event.getAuthor().getId(), ign );
  }

}
