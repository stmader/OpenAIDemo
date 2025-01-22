package at.rsg.devcon.openai.openaiapi4java;

import at.rsg.devcon.openai.InputHelper;
import at.rsg.devcon.openai.KeySafe;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeasingAssistantWithOpenAIJavaAPI {

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

    public static void main(String[] args) throws IOException {
        /* Initialisierung des OpenAI-Clients mit API-Key */
        OpenAIClient openAiClient = OpenAIOkHttpClient.builder()
                .apiKey(API_KEY)
                .build();


        List<ChatCompletionMessageParam> chatMessages = new ArrayList<>(List.of(
                createSystemMessageParam(SYSTEM_PROMPT_JSON),
                createAssistantMessageParam(ASSISTANT_PROMPT)));

        System.out.println("\n*****Impulsi****:\n" + ASSISTANT_PROMPT);


        //************************

        while (true) {
            try {
                // Eingabe des Nutzers lesen
                String userInput = InputHelper.readLine("\n*****Du****: \n");

                // Eingabe des Nutzer zu den Messages hinzufügen
                chatMessages.add(createUserMessageParam(userInput));

                // alle Messages an OpenAI API senden
                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .messages(chatMessages)
                        .model(ChatModel.CHATGPT_4O_LATEST)
                        .n(1)
                        .build();
                ChatCompletion chatCompletion = openAiClient.chat().completions().create(params);


                //Antwort von OpenAI auswerten. Falls mehrere Antworten, einfach die erste nehmen. (Defaultmäßig gibt es nur eine)
                String choice0 = chatCompletion.choices().get(0)
                        .message().content().get();

                // Antwort anzeigen
                System.out.println("\n*****Impulsi****:\n" + choice0);

                // Antwort von OpenAI zu den Messages hinzufügen
                chatMessages.add(createAssistantMessageParam(choice0));

            } catch (IOException e) {
                System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
                break; // Beenden der Schleife im Fehlerfall
            }
        }
    }



    private static ChatCompletionMessageParam createUserMessageParam(String userPrompt) {
        return ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
                ChatCompletionUserMessageParam.builder()
                        .role(ChatCompletionUserMessageParam.Role.USER)
                        .content(ChatCompletionUserMessageParam.Content.ofTextContent(userPrompt))
                        .build()
        );
    }

    private static ChatCompletionMessageParam createSystemMessageParam(String systemPrompt) {
        return ChatCompletionMessageParam.ofChatCompletionSystemMessageParam(
                ChatCompletionSystemMessageParam.builder()
                        .role(ChatCompletionSystemMessageParam.Role.SYSTEM)
                        .content(ChatCompletionSystemMessageParam.Content.ofTextContent(systemPrompt))
                        .build()
        );
    }

    private static ChatCompletionMessageParam createAssistantMessageParam(String assistantPrompt) {
        return ChatCompletionMessageParam.ofChatCompletionAssistantMessageParam(
                ChatCompletionAssistantMessageParam.builder()
                        .role(ChatCompletionAssistantMessageParam.Role.ASSISTANT)
                        .content(ChatCompletionAssistantMessageParam.Content.ofTextContent(assistantPrompt))
                        .build()
        );
    }



}