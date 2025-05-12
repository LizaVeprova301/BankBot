package io.project.bankbot.service;

import io.project.bankbot.config.BotConfig;
import io.project.bankbot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Objects;

@Slf4j
@Component("toTakeMoneyBot")
public class ToTakeMoney extends TelegramBot {

    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public ToTakeMoney(BotConfig config) {
        super(config);
    }

    public void initialize(Long chatId) {
        Session session = new Session();
        session.setSessionChatId(chatId);
        session.setSessionName("to_take_money");
        session.setSessionStage("amount");
        sessionRepository.save(session);
        log.info("Session saved {}", session);
    }

    public void registerToTakeMoney(Message message,Session session) {
        String textMessage = message.getText();
        Long chatId = message.getChatId();
        if (Objects.equals(session.getSessionName(), "to_take_money")) {
            switch (session.getSessionStage()) {
                case "amount":
                    long amount;
                    try {
                        amount = Long.parseLong(textMessage);
                    } catch (NumberFormatException e) {
                        sendMessage(chatId, "Вы неправильно ввели число, попробуйте еще раз. сессия:" + session.getSessionStage());
                        break;
                    }
                    Application application = new Application();
                    application.setApplicationOwner(chatId);
                    application.setApplicationAction("take_money");
                    application.setApplicationStatus("consideration");
                    application.setApplicationAmount(amount);
                    application.setApplicationAmountProcess(amount);
                    applicationRepository.save(application);
                    log.info("Application saved {} and Amount saved {}", application, application.getApplicationAmount());
                    session.setSessionStage("who");
                    sessionRepository.save(session);
                    sendMessage(chatId, "Введите у кого хотите взять в долг (ник без @). Этап сессии: " + session.getSessionStage());
                    break;
                case "who":
                    Long investorChatId;
                    try {
                        investorChatId = userRepository.findByUsername(textMessage).getChatId();
                    } catch (Exception e) {
                        sendMessage(chatId, "Вы неправильно ввели ник или Человек не зарегистрирован в боте.\n" +
                                " Введите у кого хотите взять в долг(ник без @) еще раз. Этап сессии: " + session.getSessionStage());
                        break;
                    }

                    Application applicationToUpdate = applicationRepository.findByApplicationOwnerAndApplicationInvestor(session.getSessionChatId(), 0L);
                    applicationToUpdate.setApplicationInvestor(investorChatId);
                    applicationRepository.save(applicationToUpdate);
                    log.info("Application saved {} and Investor saved {}", applicationToUpdate, applicationToUpdate.getApplicationInvestor());
                    session.setSessionStage("default");
                    session.setSessionName("default");
                    sessionRepository.save(session);
                    sendMessage(chatId, "Заявка подана, уведомление отправлено инвестору) Этап сессии: " + session.getSessionStage());
                    String investorName = userRepository.findByChatId(investorChatId).getFirstname();
                    String ownerName = userRepository.findByChatId(chatId).getFirstname();
                    sendMessage(investorChatId, "Привет " + investorName + "! " + ownerName + " просит у тебя немного монет. Проверь свои заявки!" + session.getSessionStage());

                    break;
                default:
                    sendMessage(chatId, "Я ожидал другой этап");

            }
        } else {
            sendMessage(chatId, "Я умею толлько to_take_money, а сейчас " + session.getSessionName());
        }
    }
}
