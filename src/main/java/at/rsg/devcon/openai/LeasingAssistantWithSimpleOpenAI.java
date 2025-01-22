package at.rsg.devcon.openai;

import com.openai.models.ChatModel;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The LeasingAssistantWithSimpleOpenAI class implements an interactive chatbot
 * designed to assist customers in selecting leased cars and explaining the
 * leasing process. The chatbot interacts with the OpenAI API to provide
 * intelligent responses and suggestions tailored to the user's preferences
 * and budget. This implementation uses the SimpleOpenAI library for interaction.
 *
 * Functionality includes:
 * - Recommending cars based on the customer's leasing budget and priorities.
 * - Explaining various leasing-related terms such as down payment, mileage limit, etc.
 * - Highlighting promotional deals for specific car models.
 * - Interacting with users via a terminal-based interface.
 * - Optionally delivering responses in structured HTML or JSON formats based on system prompts.
 *
 * System prompts guide the chatbot's behavior and include both general and specific instructions
 * for engaging with the customer. A secondary feature enables outputting car-related data in JSON
 * format after user confirmation.
 *
 * The implementation uses methods for either synchronous or streaming interaction with the OpenAI API.
 * The chatbot maintains a conversation context using a List of ChatMessage objects, where each message
 * represents user input or assistant output.
 */
public class LeasingAssistantWithSimpleOpenAI {
    // OpenAI API-Key .
    private static final String API_KEY = KeySafe.API_KEY;

    private static final String SYSTEM_PROMPT = """
            Du bist ein Assistent der dem Kunden hilft, ein Auto zu finden, das er leasen kann. Optionen sind
            
                  * Kunden Vorschläge geben basierend auf dem Budget und den Prioritäten des Kunden.
                  * Erkläre wie Leasing funktioniert und welche Vorteile es hat.
                  * Erkläre Begriffe wie Anzahlung, Laufzeit, Restwert und Kilometerbegrenzung.
                  * Erkläre, dass die Raiffeisen Impuls Leasing der beste Ansprechpartner für Leasing ist. Über den Online Rechner kann man in 10 Minuten ein Auto leasen.
                  * Sag dass es bis 1. März die Möglichkeit gibt, Tesla Model 3 zu besonders günstigen Konditionen bei Raiffeisen Impuls Leasing zu leasen.
            
            
                  Frage den Kunden nach seinem monatlichen Budget für Leasing und seinen Prioritäten. Frage, ob ihm Sicherheit, Motorisierung oder ökologische Nachhaltigkeit wichtiger ist.
                  Mache einen Vorschlag basierend auf diesen Informationen.
            
                  Beantworte nur Fragen, die mit der Auswahl eines Autos oder Leasing zusammenhängen. Wenn der Kunde Fragen stellt, die nicht mit diesem Thema zusammenhängen, sag ihm, dass du solche Fragen nicht beantworten kannst.
            
            """;

    private static final String SYSTEM_PROMPT_HTML = SYSTEM_PROMPT + " Gib die Antwort als HTML zurück.";
    private static final String SYSTEM_PROMPT_JSON = SYSTEM_PROMPT + """
            Wenn der User sich für ein Auto entschieden hat, frag ihn, ob er die Daten in den Online 
             Leasingrechner übernehmen will. Falls ja, gibt das Auto im JSON Format in folgender Struktur aus:
            [{
            "marke": "Audi",
            "modell": "A4",
            "treibstoff": "Benzin"
            }]       
            und sage dem User, dass die Daten automatisch übernommen wurden.
            Gib aber kein JSON aus, bevor der User bestätigt hat. 
            Beende dann den Chat mit dem Text __ENDE__     
            """;

    private static final String ASSISTANT_PROMPT = """
            Ich bin Impulsi, deine persönliche Assistentin von Raiffeisen Impuls Leasing.
            Ich kann dir helfen, das perfekte Auto zu finden.
            Ich kann dir auch erklären, wie Leasing funktioniert und welche Vorteile es hat.
            Frag mich einfach!""";


    public static void main(String[] args) throws IOException {
        var openAI = SimpleOpenAI.builder()
                .apiKey(API_KEY)
                .build();

        List<ChatMessage> chatMessages = new ArrayList<>(List.of(
                ChatMessage.SystemMessage.of(SYSTEM_PROMPT),
                ChatMessage.AssistantMessage.of(ASSISTANT_PROMPT)));

        System.out.println("\n*****Impulsi****:\n" + ASSISTANT_PROMPT);

        while (true) {
            try {
                // Eingabe des Nutzers lesen
                String userInput = InputHelper.readLine("\n*****Du****: \n");
                chatMessages.add(ChatMessage.UserMessage.of(userInput));

//                String assistentResponse = askOpenAI(openAI, chatMessages);
                String assistentResponse = askOpenAIWithStreaming(openAI, chatMessages);
                chatMessages.add(ChatMessage.AssistantMessage.of(assistentResponse));
            } catch (IOException e) {
                System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
                break;
            }
        }

    }

    private static String askOpenAI(SimpleOpenAI openAI, List<ChatMessage> chatMessages) {
        var chatRequest = ChatRequest.builder()
                .model(ChatModel.CHATGPT_4O_LATEST.toString())
                .messages(chatMessages)
//                .temperature(0.0)
                .n(1)
                .build();
        var futureChat = openAI.chatCompletions().create(chatRequest);
        var chatResponse = futureChat.join();
        String openAIResponse = chatResponse.firstContent();
        System.out.println(openAIResponse);
        return openAIResponse;
    }

    private static String askOpenAIWithStreaming(SimpleOpenAI openAI, List<ChatMessage> chatMessages) {
        var chatRequest = ChatRequest.builder()
                .model(ChatModel.CHATGPT_4O_LATEST.toString())
                .messages(chatMessages)
//                .temperature(0.0)
                .n(1)
                .build();
        var futureChat = openAI.chatCompletions().createStream(chatRequest);
        var chatResponse = futureChat.join();
        StringBuffer result = new StringBuffer();
        chatResponse.filter(chatResp -> chatResp.getChoices().size() > 0 && chatResp.firstContent() != null)
                .forEach(chatResp -> result.append(LeasingAssistantWithSimpleOpenAI.processResponseChunk(chatResp)));
        System.out.println();
        return result.toString();

    }

    private static String processResponseChunk( Chat responseChunk) {
        var choices = responseChunk.getChoices();
        if (!choices.isEmpty()) {
            var delta = choices.get(0).getMessage();
            if (delta.getContent() != null) {
                System.out.print(delta.getContent());
                return delta.getContent();
            }
        }
        var usage = responseChunk.getUsage();
        if (usage != null) {
            System.out.println("\n");
            System.out.println(usage);
        }
        return "\n";
    }
}
