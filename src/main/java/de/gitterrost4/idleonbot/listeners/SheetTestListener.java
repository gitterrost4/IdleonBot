package de.gitterrost4.idleonbot.listeners;

import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import de.gitterrost4.idleonbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SheetTestListener extends AbstractMessageListener<ServerConfig>{

  public SheetTestListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getGibConfig(), "sheettest"); 
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage message){
//    try {
//      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//      final String spreadsheetId = "1N2lByw4weVWUQQS6DPjObVP7tb2xUS_u_hp49HrYg5Y";
//      Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//          .setApplicationName(APPLICATION_NAME)
//          .build();
//      service.spreadsheets().values().append(spreadsheetId, "TESTSHEET!A1", new ValueRange().setValues(Arrays.asList(Arrays.asList("a","b","c","d","e")))).setValueInputOption("USER_ENTERED").execute();
//      ValueRange response = service.spreadsheets().values().get(spreadsheetId, "Testsheet!A1:F5").execute();
//      Optional.ofNullable(response.getValues()).filter(x->!x.isEmpty()).ifPresent(data-> {
//        for(List row: data) {
//          System.err.println(row);
//        }
//      }
//        
//          );
//      
//      
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
  }
  
//  private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
//  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//  private static final String TOKENS_DIRECTORY_PATH = "tokens";
//  private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
//  private static final String CREDENTIALS_FILE_PATH = "sheets-credentials.json";
//  /**
//   * Creates an authorized Credential object.
//   * @param HTTP_TRANSPORT The network HTTP Transport.
//   * @return An authorized Credential object.
//   * @throws IOException If the credentials.json file cannot be found.
//   */
//  private static HttpRequestInitializer getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//    
//      InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
////      GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//      ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(in);
//      return new HttpCredentialsAdapter(credentials);
//  }
}
