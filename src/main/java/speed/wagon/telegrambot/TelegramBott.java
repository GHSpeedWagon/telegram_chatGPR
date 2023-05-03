package speed.wagon.telegrambot;

import com.squareup.okhttp.Request;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import speed.wagon.telegrambot.service.chatGPT.ChatGptService;
import speed.wagon.telegrambot.service.chatGPT.impl.ChatGptServiceImpl;
import speed.wagon.telegrambot.service.dallE.DallEService;
import speed.wagon.telegrambot.service.dallE.impl.DallEServiceImpl;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Data
@Component
public class TelegramBott extends TelegramLongPollingBot {
    private final ChatGptService chatGptService = new ChatGptServiceImpl();
    private final DallEService dallEService = new DallEServiceImpl();
    @Value(value = "${telegram.bot.username}")
    private static String TELEGRAM_BOT_USERNAME = "RickAndMortyUniverseBot";
    // |_ example: BlaBlaBlaBot;
    @Value("${telegram.bot.token}")
    private String TELEGRAM_BOT_TOKEN = "6098984658:AAG4Al1aGemb19dkOsdZsO7dXTTQcBUTe14";
    private boolean markedAsGPT = false;
    private boolean markedAsDallE = false;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            InlineKeyboardMarkup quitButton = getQuitButton();
            if (!markedAsGPT && !markedAsDallE) {
                if (messageText.equals("/start")) {
                    SendMessage startMessage = getStartMessage(message);
                    try {
                        execute(startMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException("Can't execute message while creating buttons", e);
                    }
                }
            } else if (markedAsGPT && !markedAsDallE) {
                try {
                    Request request = chatGptService.postRequestToChatGPT(messageText);
                    String response = chatGptService.getResponseFromChatGPT(request);
                    message.setText(response);
                    message.setReplyMarkup(quitButton);
                    log.info(response);
                    execute(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException("We have exception while working with response", e);
                }
            } else if (markedAsDallE && !markedAsGPT) {
                Request request = dallEService.postRequestToDallE(messageText);
                File image = dallEService.savePNGResponse(request);
                SendPhoto photo = new SendPhoto();
                photo.setChatId(update.getMessage().getChatId());
                photo.setPhoto(new InputFile(image));
                photo.setReplyMarkup(quitButton);
                try {
                    execute(photo);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (update.hasCallbackQuery() && !markedAsGPT || !markedAsDallE) {
            turnOnGPTorDallE(update);
        } else {
            SendMessage reserveMessage = new SendMessage();
            reserveMessage.setChatId(update.getMessage().getChatId());
            reserveMessage.setText("u should send only text");
            try {
                execute(reserveMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException("We have exception while working with reserve message", e);
            }
        }
    }
    private SendMessage getStartMessage(SendMessage message) {
        InlineKeyboardMarkup chatGPTandDallEButtons = getGPTandDallEButtons();
        message.setReplyMarkup(chatGPTandDallEButtons);
        message.setText("Set the IO you need. \nFor speak with AI - ChatGPT \nFor generate images - Dall-E");
        return message;
    }

    private void turnOnGPTorDallE(Update update) {
        String callData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
        InlineKeyboardMarkup quitButton = getQuitButton();
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        if (callData.equals("chat_gpt")) {
            markedAsGPT = true;
            markedAsDallE = false;
            message.setReplyMarkup(quitButton);
            message.setText("Write your text for chatGPT here");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else if (callData.equals("dall_e")) {
            markedAsDallE = true;
            markedAsGPT = false;
            message.setReplyMarkup(quitButton);
            message.setText("Write your prompts");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else if (callData.equals("quit")) {
            markedAsDallE = false;
            markedAsGPT = false;
            SendMessage startMessage = getStartMessage(message);
            try {
                execute(startMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private InlineKeyboardMarkup getGPTandDallEButtons() {
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
            List<InlineKeyboardButton> firstLevelButton = new ArrayList<>();
            InlineKeyboardButton chatGptButton = new InlineKeyboardButton();
            chatGptButton.setText("ChatGPT");
            chatGptButton.setCallbackData("chat_gpt");
            firstLevelButton.add(chatGptButton);
            rowsInLine.add(firstLevelButton);
            List<InlineKeyboardButton> secondLevelButton = new ArrayList<>();
            InlineKeyboardButton dallEButton = new InlineKeyboardButton();
            dallEButton.setText("Dall-E");
            dallEButton.setCallbackData("dall_e");
            secondLevelButton.add(dallEButton);
            rowsInLine.add(secondLevelButton);
            inlineKeyboardMarkup.setKeyboard(rowsInLine);
            return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getQuitButton() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstLevelButton = new ArrayList<>();
        InlineKeyboardButton chatGptButton = new InlineKeyboardButton();
        chatGptButton.setText("QUIT");
        chatGptButton.setCallbackData("quit");
        firstLevelButton.add(chatGptButton);
        rowsInLine.add(firstLevelButton);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return TELEGRAM_BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return TELEGRAM_BOT_TOKEN;
    }
}
