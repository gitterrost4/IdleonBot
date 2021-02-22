package de.gitterrost4.idleonbot.config.containers;

import java.io.IOException;

import de.gitterrost4.idleonbot.config.Config;

public class MainConfig extends de.gitterrost4.botlib.config.containers.MainConfig<ServerConfig>{
  private ServerConfig defaultConfig;

  @Override
  public ServerConfig getDefaultConfig() {
    try {
      ServerConfig config = Config.objectMapper().treeToValue(Config.objectMapper().valueToTree(defaultConfig),
          ServerConfig.class); // this is a dirty, dirty hack... deep-copying the whole object by
                               // serializing/deserializing it
      return config;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
