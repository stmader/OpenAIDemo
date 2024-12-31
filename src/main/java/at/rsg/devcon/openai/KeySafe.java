package at.rsg.devcon.openai;

/**
 * The KeySafe class serves as a centralized location for storing sensitive
 * information, such as API keys, used by the application.
 *
 * This class ensures that sensitive credentials are separated from other
 * application logic for better security and maintainability.
 * In Production use, the key should be read from an environment variable.
 * Preferably, use Azure OpenAI which needs no API keys.
 */
public class KeySafe {
    //add your own OpenAI API Key here
    public static final String API_KEY = "xxx";

}
