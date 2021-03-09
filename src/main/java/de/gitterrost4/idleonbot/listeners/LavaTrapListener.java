package de.gitterrost4.idleonbot.listeners;

import de.gitterrost4.botlib.listeners.AbstractListener;
import de.gitterrost4.idleonbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;

public class LavaTrapListener extends AbstractListener<ServerConfig> {

  public LavaTrapListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getLavaTrapConfig());
  }

  @Override
  public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
    if(event.getUser().getId().equals("282657635067494410") || event.getUser().getId().equals("603659239428194317")) {
      guild().getTextChannelById("813059416424448030").sendMessage(event.getMember().getAsMention()+" We caught you, Lava! Now @everyone knows you're here!").queue(); 
    }
  }

  @Override
  public void onUserTyping(UserTypingEvent event) {
    super.onUserTyping(event);
  }
  
  
  
}
