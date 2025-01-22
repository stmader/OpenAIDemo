package at.rsg.devcon.openai;

import com.openai.models.Completion;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * https://github.com/sashirestela/simple-openai?tab=readme-ov-file#chat-completion-example
 * https://github.com/sashirestela/simple-openai/blob/main/src/demo/java/io/github/sashirestela/openai/demo/ChatDemo.java
 */
public class LeasingAssistantWithSimpleOpenAI {
    // OpenAI API-Key (ersetze den Platzhalter durch deinen Schlüssel).
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

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";


    public static void main(String[] args) throws IOException {
        var openAI = SimpleOpenAI.builder()
                .apiKey(KeySafe.API_KEY)
                .build();

        askOpenAI(openAI);

        askOpenAIWithStreaming(openAI);

    }

    private static String askOpenAI(SimpleOpenAI openAI) {
        var chatRequest = ChatRequest.builder()
                .model("gpt-4o-mini")
                .message(ChatMessage.SystemMessage.of("You are an expert in AI."))
                .message(ChatMessage.UserMessage.of("Write a technical article about ChatGPT, no more than 100 words."))
                .temperature(0.0)
                .maxCompletionTokens(3000)
                .build();
        var futureChat = openAI.chatCompletions().create(chatRequest);
        var chatResponse = futureChat.join();
        String openAIResponse = chatResponse.firstContent();
        System.out.println(openAIResponse);
        return openAIResponse;
    }

    private static String askOpenAIWithStreaming(SimpleOpenAI openAI) {
        var chatRequest = ChatRequest.builder()
                .model("gpt-4o-mini")
                .message(ChatMessage.SystemMessage.of("You are an expert in AI."))
                .message(ChatMessage.UserMessage.of("Write a technical article about ChatGPT, no more than 100 words."))
                .temperature(0.0)
                .maxCompletionTokens(300)
                .build();
        var futureChat = openAI.chatCompletions().createStream(chatRequest);
        var chatResponse = futureChat.join();
        chatResponse.filter(chatResp -> chatResp.getChoices().size() > 0 && chatResp.firstContent() != null)
//                .map(Chat::firstContent)
                .forEach(LeasingAssistantWithSimpleOpenAI::processResponseChunk);
        System.out.println();
        return "TODO"
                ;
    }

    private static void processResponseChunk(Chat responseChunk) {
        var choices = responseChunk.getChoices();
        if (!choices.isEmpty()) {
            var delta = choices.get(0).getMessage();
            if (delta.getContent() != null) {
                System.out.print(delta.getContent());
            }
        }
        var usage = responseChunk.getUsage();
        if (usage != null) {
            System.out.println("\n");
            System.out.println(usage);
        }
    }
}
