package speed.wagon.telegrambot.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiChatGPTResponseDto {
    ApiTextResponseDto apiResponseTextDto = new ApiTextResponseDto();
    private List<ApiTextResponseDto> choices;
    @Data
    public static class ApiTextResponseDto {
        private String text;
    }
}
