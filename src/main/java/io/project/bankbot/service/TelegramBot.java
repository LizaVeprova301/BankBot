package io.project.bankbot.service;

import io.project.bankbot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
    };

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken(){
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText){
                case "/start":
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName(),"/start");
                    break;
                case "/info":
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName(),"/info");
                    break;
                case "/readme":
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName(),"/readme");
                    break;
                case "/my_memories":
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName(),"/my_memories");
                    break;
                case "/to_take":
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName(),"/to_take");
                    break;
                case "/return":
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName(),"/return");
                    break;
                default:
                    sendMessage(chatId,"Sorry, я так еще не умею");


            }
        }
    }

    private void startCommandReceived(long chatId, String firstName, String command) {
        String answer = switch (command) {
            case "/start" -> "Hiiiiiiii, " + firstName;
            case "/info" -> "Сделано с любовью";
            case "/readme" -> "Тут будет подробная инструкция";
            case "/my_memories" -> "список долгов тут будет";
            case "/to_take" -> "Сколько тебе нужно монет?";
            case "/return" -> "Сколько монет ты хочешь вернуть?";
            default -> "куда ты жмал?";
        };
        log.info("Replied to user ", firstName);
        sendMessage(chatId,answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try{
            execute(message);
        }
        catch (TelegramApiException e){
            log.error("ERROR",e.getMessage());
        }
    }
}
