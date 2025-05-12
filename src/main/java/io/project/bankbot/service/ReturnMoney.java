package io.project.bankbot.service;

import io.project.bankbot.config.BotConfig;
import io.project.bankbot.model.*;
import io.project.bankbot.tgService.InlineKeyboardTgButton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static io.project.bankbot.tgService.InlineKeyboardTgButton.backButtonKeyboard;
import static io.project.bankbot.tgService.InlineKeyboardTgButton.responseApplicationKeyboard;

@Slf4j
@Component("returnMoney")
public class ReturnMoney extends TelegramBot{
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserRepository userRepository;
    private List<Application> userApplicationsWaitMoney;
    private int numberApplicationChoise = 0;

    public ReturnMoney(BotConfig config) {
        super(config);
    }

    void viewListWaitingReturnMoney(Long chatId, Session session) throws TelegramApiException {
        StringBuilder messageText = new StringBuilder();
        this.userApplicationsWaitMoney = applicationRepository.findAllByApplicationOwnerAndStatusWaiting(chatId);
        if (this.userApplicationsWaitMoney.isEmpty()) {
            sendMessage(chatId, "У вас нет долгов. Приятная фраза, не правда ли)");
        } else {
            messageText.append("Они ждут твоих монет:\n\n");
            int counter = 1;
            for (Application application : this.userApplicationsWaitMoney) {
                messageText.append(counter).append(". Дата создания заявки: ").append(application.getApplicationDate()).append("\n")
                        .append("У кого занял: ").append(userRepository.findByChatId(application.getApplicationInvestor()).getFirstname()).append("\n")
                        .append("Первоначальная сумма: ").append(application.getApplicationAmount()).append("\n")
                        .append("Нынешняя сумма: ").append(application.getApplicationAmountProcess()).append("\n")
                        .append("Статус воспоминания: ").append(textApplicationStatus(application.getApplicationStatus())).append("\n")
                        .append("\n\n");
                counter++;
            }

            session.setSessionName("return_money");
            session.setSessionStage("choise_application");

            messageText.append("Выбери номер заявки, в которую хочешь внести монеты, напиши число в чатик, без лишних символов! Cессия: ").append(sessionRepository.findBySessionChatId(chatId).getSessionStage());

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageText.toString());
            message.setReplyMarkup(backButtonKeyboard());

            sessionRepository.save(session);
            execute(message);

        }
    }

    public void returnMoneyProcess(Message message, Session session) {
        Long chatId = message.getChatId();
        String messageText = message.getText();
        switch (session.getSessionStage()) {
            case "choise_application":
                try {
                    this.numberApplicationChoise = Integer.parseInt(messageText) - 1;
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Вы неправильно ввели число, попробуйте еще раз. сессия: " + sessionRepository.findBySessionChatId(chatId).getSessionStage());
                    break;
                }
                try {
                    Application application = this.userApplicationsWaitMoney.get(this.numberApplicationChoise);
                } catch (Exception e){
                    sendMessage(chatId, "Такой заявки нет в списке, попробуйте еще раз. сессия: " + sessionRepository.findBySessionChatId(chatId).getSessionStage());
                    break;
                }
                session.setSessionStage("amount");
                sessionRepository.save(session);
                super.sendMessage(chatId,"Введите сумму, которую хотите внести. Также, только циферки");
                break;
            case "amount":
                long amount;
                try {
                    amount = Long.parseLong(messageText);
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Вы неправильно ввели число, попробуйте еще раз. сессия:" + session.getSessionStage());
                    break;
                }
                Application application = this.userApplicationsWaitMoney.get(this.numberApplicationChoise);
                Long newAmount = application.getApplicationAmountProcess() - amount;
                Long investorChatId = application.getApplicationInvestor();
                String ownerUserName = userRepository.findByChatId(chatId).getFirstname();

                if (newAmount < 0) {
                    sendMessage(chatId,"Многовато будет, побереги чаевые. Попробуй ввести еще раз");
                    break;
                } else if (newAmount > 0) {
                    sendMessage(investorChatId, ownerUserName + " вроде отправил(а) тебе немного грошей, подтверждаешь? Проверь свою казну и глянь заявки" );
                    sendMessage(chatId, "Отлично, ты уже ближе к цели" );
                    application.setApplicationAction("return_money_noEnd");
                }else {
                    sendMessage(investorChatId, ownerUserName + " вроде отправил(а) тебе и хочет закрыть долг, подтверждаешь? Проверь свою казну и глянь заявки" );
                    sendMessage(chatId, "Отлично, этого должно хватить, чтобы закрыть долг" );
                    application.setApplicationAction("return_money_End");
                }
                application.setApplicationAmountTemp(amount);
                application.setApplicationStatus("consideration");
                applicationRepository.save(application);
                log.info("Application saved {} and AmountTemp saved {}", application, application.getApplicationAmountTemp());
                session.setSessionName("default");
                session.setSessionStage("default");
                sessionRepository.save(session);
                break;
            default:
                sendMessage(chatId, "Неверная сессия в return_money");

        }

    }


}
