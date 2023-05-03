package speed.wagon.telegrambot.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiDallEResponseDto {
    ApiDallEResponseUrl apiDallEResponseUrl = new ApiDallEResponseUrl();
    private List<ApiDallEResponseUrl> data;

    @Data
    public static class ApiDallEResponseUrl {
        private String url;
    }
}
