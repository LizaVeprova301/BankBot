package io.project.bankbot.service;

import io.project.bankbot.config.BotConfig;

import io.project.bankbot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static io.project.bankbot.tgService.InlineKeyboardTgButton.backButtonKeyboard;

@Slf4j
@Component("telegramBot")
@Primary
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    @Lazy
    ToTakeMoney toTakeMoney;

    @Autowired
    @Lazy
    ReturnMoney returnMoney;

    @Autowired
    @Lazy
    ConsiderApplication considerApplication;

    @Autowired
    private ApplicationRepository applicationRepository;


    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }



    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();
            Session userSession = sessionRepository.findBySessionChatId(chatId);
            if (userRepository.findById(chatId).isEmpty()){
                sendMessage(chatId, "Вы не зарегистрированы");
                return;
            }
            switch (callbackData){
                case "responseApplicationButton":
                    considerApplication.responseApplicationButton(chatId,userSession);
                    break;
                case "doneApplicationButton", "rejectApplicationButton":
                    considerApplication.doneOrRejectApplication(callbackQuery,userSession);
                    break;
                case "backButton":
                    switch (sessionRepository.findBySessionChatId(chatId).getSessionName()){
                        case "return_money", "to_take_money":
                            back(chatId,userSession);
                            break;
                        default:
                            sendMessage(chatId, "Вы нажали кнопку назад, но вы находитесь в какой-то другой сессии");
                            break;

                    }
                    break;
                default:
                    sendMessage(chatId, "Sorry, я так еще не умею");
                    break;
            }
        }


        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Session userSession = sessionRepository.findBySessionChatId(chatId);
            if (userRepository.findById(message.getChatId()).isEmpty()){
                sendMessage(chatId, "Вы не зарегистрированы");
                return;
            }

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, message.getChat().getFirstName(), "/start");
                    break;
                case "/info":
                    startCommandReceived(chatId, message.getChat().getFirstName(), "/info");
                    break;
                case "/my_applications":
                    //startCommandReceived(chatId, message.getChat().getFirstName(), "/my_applications");
                    try {
                        considerApplication.viewConsiderApplication(chatId,userSession);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "/my_memories":
                    //startCommandReceived(chatId, message.getChat().getFirstName(), "/my_memories");
                    viewMemories(update.getMessage().getChatId());
                    break;
                case "/to_take":
                    toTakeMoney(update.getMessage());
                    startCommandReceived(chatId, message.getChat().getFirstName(), "/to_take");
                    break;
                case "/return":
                    try {
                        returnMoney.viewListWaitingReturnMoney(chatId,userSession);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    //startCommandReceived(chatId, message.getChat().getFirstName(), "/return");
                    break;
                default:
                    switch (sessionRepository.findBySessionChatId(chatId).getSessionName()) {
                        case "to_take_money": toTakeMoney.registerToTakeMoney(message,userSession);
                            break;
                        case "consider_application":
                            try {
                                considerApplication.choiseApplication(message,userSession);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "return_money":
                            returnMoney.returnMoneyProcess(message,userSession);
                            break;

                        default:
                            sendMessage(chatId, "Sorry, я так еще не умею");
                            break;
                    }
                    break;
            }
        }
    }

    private void viewMemories(Long chatId) {
        List<Application> userApplications = applicationRepository.findAllByApplicationOwnerOrApplicationInvestorAndStatusNot(chatId,chatId);

        if (userApplications.isEmpty()) {
            sendMessage(chatId, "У вас нечего вспоминать");
        } else {
            StringBuilder messageText = new StringBuilder("Ваши воспоминания:\n\n");

            int counter = 1;
            for (Application application : userApplications) {
                messageText.append(counter).append(". Дата создания заявки: ").append(application.getApplicationDate()).append("\n")
                        .append("Кто занимал: ").append(userRepository.findByChatId(application.getApplicationOwner()).getFirstname()).append("\n")
                        .append("У кого занимал: ").append(userRepository.findByChatId(application.getApplicationInvestor()).getFirstname()).append("\n");
                if (Objects.equals(application.getApplicationStatus(), "return")||Objects.equals(application.getApplicationStatus(), "reject")) {
                    messageText.append("Сумма: ").append(application.getApplicationAmount()).append("\n");
                } else{
                    messageText.append("Первоначальная сумма: ").append(application.getApplicationAmount()).append("\n")
                                .append("Нынешняя сумма: ").append(application.getApplicationAmountProcess()).append("\n");
                }
                messageText.append("Статус воспоминания: ").append(textApplicationStatus(application.getApplicationStatus())).append("\n\n");
                counter++;
            }
            // Отправляем сообщение пользователю
            sendMessage(chatId, messageText.toString());
        }

    }

    public String textApplicationStatus(String applicationStatus) {
        return switch (applicationStatus) {
            case "reject" -> "Отклонено";
            case "waiting_on_refunds" -> "Ожидает возврата монет";
            case "consideration" -> "Заявка на рассмотрении";
            case "return" -> "Заявка закрыта, вам отдали эти монеты";
            default -> "Непонятный статус заявки";
        };

    }

    private void toTakeMoney(Message msg) {
        var chatId = msg.getChatId();
        this.toTakeMoney.initialize(chatId);

    }


    private void registerUser(Message msg) {
        Session session = new Session();
        session.setSessionChatId(msg.getChatId());
        sessionRepository.save(session);

        if (userRepository.findById(msg.getChatId()).isEmpty()) {
           var chatId = msg.getChatId();
           var chat = msg.getChat();
           User user = new User();
           user.setChatId(chatId);
           user.setFirstname(chat.getFirstName());
           user.setLastname(chat.getLastName());
           user.setUsername(chat.getUserName());
           user.setRegistered(new Timestamp(System.currentTimeMillis()));

           userRepository.save(user);
           log.info("User saved {}", user);

        }
    }

    private void startCommandReceived(long chatId, String firstName, String command) {
        String answer = switch (command) {
            case "/start" -> "Hiiiiiiii, " + firstName;
            case "/info" -> "Сделано с любовью";
            case "/my_applications" -> "Я не знал.. что ты такая стерва!";
            case "/my_memories" -> "Давай расскажем, ему, как мы ебались, прошлой ночью..";
            case "/to_take" -> "Введи сколько тебе нужно монет? Целым числом без символов ./, только циферки, пожалуйста. Или вернитесь, если передумали"; //done
            default -> "куда ты жмал?";
        };
        log.info("Replied to user {}", firstName);
        if (command.equals("/to_take") ){
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(answer);
            message.setReplyMarkup(backButtonKeyboard());
            sendMessageWithKeyboard(message);
        } else {
            sendMessage(chatId, answer);
        }
    }

    private void back(Long chatId, Session session) {
        session.setSessionName("default");
        session.setSessionStage("default");
        sessionRepository.save(session);
        sendMessage(chatId,"Я готов слушать команду");
    }

    void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("ERROR {}", e.getMessage());
        }
    }
    void sendMessageWithKeyboard(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("ERROR {}", e.getMessage());
        }
    }
}
