package de.gitterrost4.idleonbot.config.containers;

import java.util.Optional;

import de.gitterrost4.botlib.config.containers.modules.ModuleConfig;
import de.gitterrost4.idleonbot.config.containers.modules.IgnConfig;
import de.gitterrost4.idleonbot.config.containers.modules.LavaTrapConfig;
import de.gitterrost4.idleonbot.config.containers.modules.RecipeConfig;
import de.gitterrost4.idleonbot.config.containers.modules.WikiConfig;
import de.gitterrost4.idleonbot.listeners.IgnListener;
import de.gitterrost4.idleonbot.listeners.LavaTrapListener;
import de.gitterrost4.idleonbot.listeners.RecipeListener;
import de.gitterrost4.idleonbot.listeners.SheetTestListener;
import de.gitterrost4.idleonbot.listeners.WikiListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class ServerConfig extends de.gitterrost4.botlib.config.containers.ServerConfig {
  
  private RecipeConfig recipeConfig;
  private WikiConfig wikiConfig;
  private LavaTrapConfig lavaTrapConfig;
  private IgnConfig ignConfig;
  
  @Override
  public String toString() {
    return "ServerConfig [recipeConfig=" + recipeConfig + ",ignConfig=" + ignConfig + ", wikiConfig=" + wikiConfig + ", alarmConfig=" + getAlarmConfig()
        + ", getSuperUserRoles()=" + getSuperUserRoles() + ", getName()=" + getName() + ", getServerId()="
        + getServerId() + ", getBotPrefixes()=" + getBotPrefixes() + ", getDatabaseFileName()=" + getDatabaseFileName()
        + ", getHelpConfig()=" + getHelpConfig() + "]";
  }

  public RecipeConfig getRecipeConfig() {
    return recipeConfig;
  }

  public WikiConfig getWikiConfig() {
    return wikiConfig;
  }

  public LavaTrapConfig getLavaTrapConfig() {
    return lavaTrapConfig;
  }
  
  public IgnConfig getIgnConfig() {
    return ignConfig;
  }

  @Override
  protected void addServerModules(JDA jda, Guild guild, de.gitterrost4.botlib.listeners.ListenerManager manager) {
    if (Optional.ofNullable(getRecipeConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new RecipeListener(jda, guild, this));
    }
    if (Optional.ofNullable(getWikiConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new WikiListener(jda, guild, this));
    }
    if (Optional.ofNullable(getLavaTrapConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new LavaTrapListener(jda, guild, this));
    }
    if (Optional.ofNullable(getIgnConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new IgnListener(jda, guild, this));
    }
    manager.addEventListener(new SheetTestListener(jda, guild, this));
  }

}
