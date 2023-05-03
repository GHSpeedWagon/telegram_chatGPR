package speed.wagon.telegrambot.service.dallE.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import speed.wagon.telegrambot.dto.ApiDallEResponseDto;
import speed.wagon.telegrambot.service.dallE.DallEService;
import speed.wagon.telegrambot.service.dallE.PngParser;

import java.io.File;
import java.io.IOException;

@Service
public class DallEServiceImpl implements DallEService {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String OPEN_AI_API_TOKEN = "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";
    private String API_OPENAI_URL = "https://api.openai.com/v1/images/generations";
    // |_ example: https://api.openai.com/v1/completions
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // |_ we ignore usless values here and get only response from AI
    }
    @Override
    public Request postRequestToDallE(String requestMessage) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("prompt", requestMessage);
        jsonObject.put("n", 1);
        jsonObject.put("size", "1024x1024");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        return new Request.Builder()
                .url(API_OPENAI_URL)
                .header("Authorization", "Bearer " + OPEN_AI_API_TOKEN)
                .post(body)
                .build();
    }

    @Override
    public File savePNGResponse(Request request) {
        try {
            Response response = client.newCall(request).execute();
            ApiDallEResponseDto apiDallEResponseDto = objectMapper
                    .readValue(response.body().string(), ApiDallEResponseDto.class);
            StringBuilder stringBuilder = new StringBuilder();
            apiDallEResponseDto.getData()
                    .stream()
                    .map(ApiDallEResponseDto.ApiDallEResponseUrl::getUrl)
                    .forEach(stringBuilder::append);
            PngParser pngParser = new PngParser();
            File parsedImage = pngParser.parse(stringBuilder.toString());
            return parsedImage;
        } catch (IOException e) {
            throw new RuntimeException("We have exception", e);
        }
    }
}
