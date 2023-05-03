package speed.wagon.telegrambot.service.chatGPT.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import speed.wagon.telegrambot.dto.ApiChatGPTResponseDto;
import speed.wagon.telegrambot.service.chatGPT.ChatGptService;

import java.io.IOException;

@Service
public class ChatGptServiceImpl implements ChatGptService {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String OPEN_AI_API_TOKEN = "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";
    private String AI_MODEL = "text-davinci-003";
    // |_ example: ext-davinci-003
    private String API_OPENAI_URL = "https://api.openai.com/v1/completions";
    // |_ example: https://api.openai.com/v1/completions
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // |_ we ignore usless values here and get only response from AI
    }

    @Override
    public Request postRequestToChatGPT(String requestMessage) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("model", AI_MODEL);
        jsonObject.put("prompt", requestMessage);
        jsonObject.put("max_tokens", 256);
        jsonObject.put("temperature", 0.7);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        return new Request.Builder()
                .url(API_OPENAI_URL)
                .header("Authorization", "Bearer " + OPEN_AI_API_TOKEN)
                .post(body)
                .build();
    }

    @Override
    public String getResponseFromChatGPT(Request request) {
        try {
            Response response = client.newCall(request).execute();
            ApiChatGPTResponseDto apiChatGptResponseDto = objectMapper
                    .readValue(response.body().string(), ApiChatGPTResponseDto.class);
            StringBuilder stringBuilder = new StringBuilder();
            apiChatGptResponseDto.getChoices()
                    .stream()
                    .map(ApiChatGPTResponseDto.ApiTextResponseDto::getText)
                    .forEach(stringBuilder::append);
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException("We have exception", e);
        }
    }
}
