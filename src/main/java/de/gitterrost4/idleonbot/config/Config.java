// $Id $
// (C) cantamen/Paul Kramer 2019
package de.gitterrost4.idleonbot.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import de.gitterrost4.botlib.listeners.BotJoinListener;
import de.gitterrost4.idleonbot.config.containers.MainConfig;
import net.dv8tion.jda.api.JDA;

/**
 * global config
 */
public class Config {
  private static Config instance = new Config();
  private final String token;
  public MainConfig config;

  public Config() {
    try (InputStream input = new FileInputStream("token.secret")) {
//    try (InputStream input=this.getClass().getClassLoader().getResourceAsStream("config.properties")) {
      byte[] buf = new byte[59];
      input.read(buf);
      token = new String(buf, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("couldn't read token", e);
    }
    try (InputStream input = new FileInputStream("config.yaml")) {
      ObjectMapper mapper = yamlObjectMapper();
      config = mapper.readValue(input, MainConfig.class);
    } catch (@SuppressWarnings("unused")IOException e) {
      LoggerFactory.getLogger(this.getClass()).warn("could not find config.yaml! Falling back to config.json.");
      try (InputStream input = new FileInputStream("config.json")) {
        ObjectMapper mapper = objectMapper();
        config = mapper.readValue(input, MainConfig.class);
      } catch (IOException e2) {
        throw new IllegalStateException("couldn't read config", e2);
      }
    }
  }

  private void saveConfigI() {
    try (OutputStream output = new FileOutputStream("config.yaml")) {
      ObjectMapper mapper = yamlObjectMapper();
      mapper.writeValue(output, config);
    } catch (IOException e) {
      throw new IllegalStateException("couldn't read config", e);
    }
  }

  public static void saveConfig() {
    instance.saveConfigI();
  }

  public static ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    return setMapperOptions(mapper);
  }

  public static ObjectMapper yamlObjectMapper() {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
    return setMapperOptions(mapper);
  }

  private static ObjectMapper setMapperOptions(ObjectMapper mapper) {
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.registerModule(new Jdk8Module());
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper;
  }

  public static String getToken() {
    return instance.token;
  }

  public static MainConfig getConfig() {
    return instance.config;
  }
  
  public static void addListeners(JDA jda) {
    jda.addEventListener(new BotJoinListener<>(jda, ()->getConfig(), ()->saveConfig())); // Listener for new servers
    getConfig().getServers().stream().forEach(config -> {
      config.iAddServerModules(jda);
    });    
  }

}

// end of file
