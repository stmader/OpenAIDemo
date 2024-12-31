package at.rsg.devcon.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.SystemColor.text;

public class ImageAssistant {

    // OpenAI API-Key (ersetze den Platzhalter durch deinen Schlüssel).
    private static final String API_KEY = "XXX";

    private static final String vertragImageUrl2 = "https://autoverkaufen.net/wp-content/uploads/2016/11/musterkaufvertragautoformularprivatcheck24.png";
    private static final String vertragImageUrl = "https://www.yumpu.com/de/image/facebook/22399366.jpg";

    private static final String SYSTEM_PROMPT = """
            Du bist ein Assistent der hilft herauszufinden, welche Daten in einem Vertrag vorliegen.
            Gib die Antwort als JSON zurück
            """;

    private static final String ASSISTANT_PROMPT = """
            Ich bin Bildi, deine persönliche Assistentin !""";

    public static void main(String[] args) throws IOException {
        /* Initialisierung des OpenAI-Clients mit API-Key */
        OpenAIClient openAiClient = OpenAIOkHttpClient.builder()
                .apiKey(API_KEY)
                .build();


        List<ChatCompletionMessageParam> chatMessages = new ArrayList<>(List.of(
                createSystemMessageParam(SYSTEM_PROMPT),
                createAssistantMessageParam(ASSISTANT_PROMPT),
                createUserImageMessageParam(null , vertragImageUrl)));

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
                        .model(ChatModel.GPT_4O_MINI)
                        .responseFormat(ResponseFormatJsonObject.builder() //Antwort immer als JSON ausgeben
                                .type(ResponseFormatJsonObject.Type.JSON_OBJECT) //ResponseFormatJsonSchema würde sogar ein JSON Schema erlauben
                                .build())
                        .build();
                ChatCompletion chatCompletion = openAiClient.chat().completions().create(params);
                

                //Antwort von OpenAI auswerten. Falls mehrere Antworten, einfach die erste nehmen.
                String choice0 = chatCompletion.choices().get(0)
                        .message().content().get();

                // Antwort anzeigen
                System.out.println("\n*****Bildi****:\n" + choice0);

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

    private static ChatCompletionMessageParam createUserImageMessageParam(String userPrompt, String imageUrl) {
        List<ChatCompletionContentPart> arrayOfContents = new ArrayList<>();

        ChatCompletionContentPartImage.ImageUrl url = ChatCompletionContentPartImage.ImageUrl.builder()
                .url(imageUrl)
//                .putAdditionalProperty("type", JsonValue.from("image-url"))
                .build();

        ChatCompletionContentPartImage image = ChatCompletionContentPartImage.builder()
                .type(ChatCompletionContentPartImage.Type.IMAGE_URL)
                .imageUrl(url).build();
        arrayOfContents.add(ChatCompletionContentPart.ofChatCompletionContentPartImage(image));

        return ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
                ChatCompletionUserMessageParam.builder()
                        .role(ChatCompletionUserMessageParam.Role.USER)
                        .content(ChatCompletionUserMessageParam.Content.ofArrayOfContentParts((arrayOfContents)))
                        .build()
        );
    }


}