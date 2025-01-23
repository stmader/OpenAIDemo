package at.rsg.devcon.openai.simpleopenai;

import at.rsg.devcon.openai.InputHelper;
import at.rsg.devcon.openai.KeySafe;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.ResponseFormat;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;

import java.io.IOException;
import java.util.ArrayList;
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

        demoCallChatWithStructuredOutputs(openAI);

    }

    public static void demoCallChatWithStructuredOutputs(SimpleOpenAI openAI) {
        var chatRequest = ChatRequest.builder()
                .model("gpt-4o-mini")
                .message(ChatMessage.SystemMessage
                        .of("You are a helpful math tutor. Guide the user through the solution step by step."))
                .message(ChatMessage.UserMessage.of("How can I solve 8x + 7 = -23"))
                .responseFormat(ResponseFormat.jsonSchema(ResponseFormat.JsonSchema.builder()
                        .name("MathReasoning")
                        .schemaClass(MathReasoning.class)
                        .build()))
                .build();
        var chatResponse = openAI.chatCompletions().createStream(chatRequest).join();
        chatResponse.filter(chatResp -> chatResp.getChoices().size() > 0 && chatResp.firstContent() != null)
                .map(Chat::firstContent)
                .forEach(result -> printResult(result));
        System.out.println();
    }

    private static void printResult(String result) {
        System.out.print(result);
        if (result.contains("}")) {
            System.out.println();
        }
    }

    public static class MathReasoning {

        public List<Step> steps;
        public String finalAnswer;

        public static class Step {

            public String explanation;
            public String output;

        }

    }
}
