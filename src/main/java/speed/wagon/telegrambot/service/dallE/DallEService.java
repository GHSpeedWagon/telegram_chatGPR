package speed.wagon.telegrambot.service.dallE;

import com.squareup.okhttp.Request;

import java.io.File;

public interface DallEService {
    Request postRequestToDallE(String requestMessage);
    File savePNGResponse(Request request);
}
