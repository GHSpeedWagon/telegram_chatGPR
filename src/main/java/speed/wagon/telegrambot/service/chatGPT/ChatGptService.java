package speed.wagon.telegrambot.service.chatGPT;

import com.squareup.okhttp.Request;

public interface ChatGptService {
    Request postRequestToChatGPT(String requestMessage);
    String getResponseFromChatGPT(Request request);
}
