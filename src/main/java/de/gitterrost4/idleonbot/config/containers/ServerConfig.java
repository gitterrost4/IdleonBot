package de.gitterrost4.idleonbot.config.containers;

import java.util.Optional;

import de.gitterrost4.botlib.config.containers.modules.ModuleConfig;
import de.gitterrost4.idleonbot.config.containers.modules.RecipeConfig;
import de.gitterrost4.idleonbot.listeners.RecipeListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class ServerConfig extends de.gitterrost4.botlib.config.containers.ServerConfig {
  
  private RecipeConfig recipeConfig;
  
  @Override
  public String toString() {
    return "ServerConfig [name=" + getName() + ", serverId=" + getServerId() + ", databaseFileName=" + getDatabaseFileName()
        + ", recipeConfig=" + recipeConfig + "]";
  }

  public RecipeConfig getRecipeConfig() {
    return recipeConfig;
  }

  @Override
  protected void addServerModules(JDA jda, Guild guild, de.gitterrost4.botlib.listeners.ListenerManager manager) {
    if (Optional.ofNullable(getRecipeConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new RecipeListener(jda, guild, this));
    }
  }

}
