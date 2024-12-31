package at.rsg.devcon.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.*;
import okhttp3.*;
import okio.Buffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeasingAssistant {

    // OpenAI API-Key (ersetze den Platzhalter durch deinen Schlüssel).
    private static final String API_KEY = "XXX";

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
    private static Interceptor interceptor = chain ->  {
        Request request = chain.request();
        System.out.println("Request: " + request.url());
        if (request.body() != null && request.body() instanceof RequestBody) {
            okio.Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            System.out.println("Body: " + buffer.readUtf8());
        }
        Response response = chain.proceed(request);
        System.out.println("Response: " + response.code());
        // Überprüfe, ob der Body vorhanden ist
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            // ResponseBody in einen Buffer schreiben (kann dann erneut verwendet werden)
            Buffer buffer = new Buffer();
            responseBody.source().request(Long.MAX_VALUE); // Lese den kompletten Body
            buffer.writeAll(responseBody.source());

            // Body-Inhalt als String auslesen
            String responseBodyString = buffer.clone().readUtf8();
            System.out.println("Response Body: " + responseBodyString);

            // Erstelle einen neuen ResponseBody, da der alte konsumiert wurde
            ResponseBody newResponseBody =
                    ResponseBody.create(responseBody.contentType(), buffer.size(), buffer);

            // Erstelle einen neuen Response mit dem neuen Body
            return response.newBuilder().body(newResponseBody).build();
        }

        // Gib den ursprünglichen Response zurück, wenn kein Body vorhanden ist
        System.out.println("Response Body war LEER " );
        return response;
    };

    public static void main(String[] args) throws IOException {
        /* Initialisierung des OpenAI-Clients mit API-Key */
        OpenAIClient openAiClient = OpenAIOkHttpClient.builder()
                .apiKey(API_KEY)
                .build();


//        List<ChatCompletionMessageParam> chatMessages = new ArrayList<>(List.of(
//                createSystemMessageParam(SYSTEM_PROMPT_JSON),
//                createAssistantMessageParam(ASSISTANT_PROMPT)));

        // Build chat messages
        JSONArray messages = new JSONArray();
        messages.put(createSystemMessage(SYSTEM_PROMPT));
        messages.put(createAssistantMessage(ASSISTANT_PROMPT));


        System.out.println("\n*****Impulsi****:\n" + ASSISTANT_PROMPT);


        //************************

        while (true) {
            try {
                // Eingabe des Nutzers lesen
                String userInput = InputHelper.readLine("\n*****Du****: \n");

                // Eingabe des Nutzer zu den Messages hinzufügen
                messages.put(createUserMessage(userInput));

                // alle Messages an OpenAI API senden
//                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
//                        .messages(chatMessages)
//                        .model(ChatModel.CHATGPT_4O_LATEST)
//                        .n(1)
////                        .streamOptions(ChatCompletionStreamOptions.builder().includeUsage(true).build())
//                        .build();
//                ChatCompletion chatCompletion = openAiClient.chat().completions().create(params);
                ResponseBody result = sendChatCompletionRequest(messages);


                //Antwort von OpenAI auswerten. Falls mehrere Antworten, einfach die erste nehmen. (Defaultmäßig gibt es nur eine)
//                String choice0 = chatCompletion.choices().get(0)
//                        .message().content().get();

                // Antwort anzeigen
                System.out.println("\n*****Impulsi****:\n" + result.string());

                // Antwort von OpenAI zu den Messages hinzufügen
//                chatMessages.add(createAssistantMessageParam(choice0));

            } catch (IOException e) {
                System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
                break; // Beenden der Schleife im Fehlerfall
            }
        }
    }

    private static JSONObject createMessage(String role, String content) {
//        JSONObject contentObj = new JSONObject()
//                .put("type", "text")
//                .put("text", content);
//        JSONArray jsonArray = new JSONArray();
//        jsonArray.put(contentObj);
        return new JSONObject()
                .put("role", role)
//                .put("content", jsonArray);
                .put("content", content);
    }

    private static JSONObject createSystemMessage(String systemPrompt) {
        return createMessage("system", systemPrompt);
    }

    private static JSONObject createUserMessage(String userPrompt) {
        return createMessage("user", userPrompt);
    }

    private static JSONObject createAssistantMessage(String userPrompt) {
        return createMessage("assistant", userPrompt);
    }


//
//    private static ChatCompletionMessageParam createUserMessageParam(String userPrompt) {
//        return ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
//                ChatCompletionUserMessageParam.builder()
//                        .role(ChatCompletionUserMessageParam.Role.USER)
//                        .content(ChatCompletionUserMessageParam.Content.ofTextContent(userPrompt))
//                        .build()
//        );
//    }
//
//    private static ChatCompletionMessageParam createSystemMessageParam(String systemPrompt) {
//        return ChatCompletionMessageParam.ofChatCompletionSystemMessageParam(
//                ChatCompletionSystemMessageParam.builder()
//                        .role(ChatCompletionSystemMessageParam.Role.SYSTEM)
//                        .content(ChatCompletionSystemMessageParam.Content.ofTextContent(systemPrompt))
//                        .build()
//        );
//    }

    private static ChatCompletionMessageParam createAssistantMessageParam(String assistantPrompt) {
        return ChatCompletionMessageParam.ofChatCompletionAssistantMessageParam(
                ChatCompletionAssistantMessageParam.builder()
                        .role(ChatCompletionAssistantMessageParam.Role.ASSISTANT)
                        .content(ChatCompletionAssistantMessageParam.Content.ofTextContent(assistantPrompt))
                        .build()
        );
    }


    private static ResponseBody sendChatCompletionRequest(JSONArray messages) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        // Build JSON request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4o-mini"); // Replace with "gpt-3.5-turbo" if required
        requestBody.put("messages", messages);
//        requestBody.put("prompt", "write a haiku about coding");
        requestBody.put("n", 1);
        JSONObject responseFormat = new JSONObject()
                .put("type", "text");
        requestBody.put("response_format", responseFormat);


        // Build HTTP request
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        // Execute the request and get the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Unexpected code " + response);
                throw new IOException("Unexpected code " + response);
            }
            return response.body();
        }
    }


}