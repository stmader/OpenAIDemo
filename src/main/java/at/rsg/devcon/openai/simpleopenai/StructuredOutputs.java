package at.rsg.devcon.openai.simpleopenai;

import at.rsg.devcon.openai.InputHelper;
import at.rsg.devcon.openai.KeySafe;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.ResponseFormat;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;

import java.io.IOException;
import java.util.List;

/**
 * @see <a href="https://github.com/sashirestela/simple-openai?tab=readme-ov-file#chat-completion-with-structured-outputs">https://github.com/sashirestela/simple-openai?tab=readme-ov-file#chat-completion-with-structured-outputs</a>
 */
public class StructuredOutputs {
    // OpenAI API-Key .
    private static final String API_KEY = KeySafe.API_KEY;




    public static void main(String[] args) throws IOException {
        var openAI = SimpleOpenAI.builder()
                .apiKey(API_KEY)
                .build();
        while (true) {
            demoCallChatWithStructuredOutputs(openAI);
        }

    }

    public static void demoCallChatWithStructuredOutputs(SimpleOpenAI openAI) throws IOException {

        String userInput = InputHelper.readLine("\n*****Bitte ein Auto eingeben****: \n");

        var chatRequest = ChatRequest.builder()
                .model("gpt-4o-mini")
                .message(ChatMessage.SystemMessage
                        .of("Du gibst Informationen zu Autos, insbesondere ihre Höchstgeschwindigkeit, Beschleunigung und Leistung. Bei der Leistung gib auch Details an."))
                .message(ChatMessage.UserMessage.of(userInput))
                .responseFormat(ResponseFormat.jsonSchema(ResponseFormat.JsonSchema.builder()
                        .name("Car")
                        .schemaClass(Car.class)
                        .description("information about a car's maxSpeed in km per hour, power in kiloWatt and acceleration")
                        .build()))
                .temperature(0.0)
                .build();
        var chatResponse = openAI.chatCompletions().createStream(chatRequest).join();
        StringBuffer allResults = new StringBuffer();
        chatResponse.filter(chatResp -> chatResp.getChoices().size() > 0 && chatResp.firstContent() != null)
                .map(Chat::firstContent)
                .forEach(result -> allResults.append(result));
        org.json.JSONObject json = new org.json.JSONObject(allResults.toString());
        System.out.println("Car Assistent:" + json.toString(4));
    }


    /**
     * Represents a car with its basic details.
     * This class provides information about a car's brand and model,
     * and contains a nested class representing the car's features.
     */
    public static class Car {

        public String carBrand;
        public String carModel;
        public Features features;

        public static class Features {
            public int maxSpeed;
            public int powerInKilowatt;
            public String detailedPowerInformation;
            public String acceleration;

        }

    }
}
