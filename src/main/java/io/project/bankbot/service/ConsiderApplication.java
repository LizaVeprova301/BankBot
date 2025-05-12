package io.project.bankbot.service;

import io.project.bankbot.config.BotConfig;
import io.project.bankbot.model.*;
import io.project.bankbot.tgService.InlineKeyboardTgButton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Objects;

import static io.project.bankbot.tgService.InlineKeyboardTgButton.responseApplicationKeyboard;

@Slf4j
@Component("considerApplication")
public class ConsiderApplication extends TelegramBot {
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserRepository userRepository;

    private int numberApplicationChoise = 0;

    List<Application> userApplications;
    InlineKeyboardTgButton inlineKeyboardMarkup;

    @Autowired
    public ConsiderApplication(BotConfig config) {
        super(config);
    }

    public void viewConsiderApplication(Long chatId, Session session) throws TelegramApiException {
        StringBuilder messageText = new StringBuilder();

        this.userApplications = applicationRepository.findAllByApplicationOwnerAndStatus(chatId);
        if (userApplications.isEmpty()) {
            sendMessage(chatId, "У вас нет заявок, что ждут ответа инвестора");
        } else {
            messageText.append("Заявки, что ждут решения инвестора:\n\n");
            int counter = 1;
            for (Application application : userApplications) {
                if (Objects.equals(application.getApplicationAction(), "take_money")){
                    messageText.append(counter).append(". Дата создания заявки: ").append(application.getApplicationDate()).append("\n")
                            .append("Кто хочет занять: ").append(userRepository.findByChatId(application.getApplicationOwner()).getFirstname()).append("\n")
                            .append("У кого хочет занять: ").append(userRepository.findByChatId(application.getApplicationInvestor()).getFirstname()).append("\n")
                            .append("Сумма: ").append(application.getApplicationAmount()).append("\n")
                            .append("Статус воспоминания: ").append(textApplicationStatus(application.getApplicationStatus())).append("\n")
                            .append("\n\n");
                    counter++;
                } else {
                    messageText.append(counter).append(". Дата создания заявки: ").append(application.getApplicationDate()).append("\n")
                            .append("Кто хочет отдать: ").append(userRepository.findByChatId(application.getApplicationOwner()).getFirstname()).append("\n")
                            .append("Кому хочет отдать: ").append(userRepository.findByChatId(application.getApplicationInvestor()).getFirstname()).append("\n")
                            .append("Сумма: ").append(application.getApplicationAmountTemp()).append("\n")
                            .append("Статус воспоминания: ").append(textApplicationStatus(application.getApplicationStatus())).append("\n")
                            .append("\n\n");
                    counter++;
                }


            }
            // Отправляем сообщение пользователю
            sendMessage(chatId, messageText.toString());
            messageText.delete(0, messageText.length());
        }
        this.userApplications = applicationRepository.findAllByApplicationInvestorAndStatus(chatId);
        if (userApplications.isEmpty()) {
            sendMessage(chatId, "У вас нет заявок, что ждут вашего ответа");
        } else {
            messageText.append("Заявки, что ждут твоего решения:\n\n");
            int counter = 1;
            for (Application application : userApplications) {
                if (Objects.equals(application.getApplicationAction(), "return_money_noEnd")|| Objects.equals(application.getApplicationAction(), "return_money_End")) {
                    messageText.append(counter).append(". Дата создания заявки: ").append(application.getApplicationDate()).append("\n")
                            .append("Кто хочет отдать: ").append(userRepository.findByChatId(application.getApplicationOwner()).getFirstname()).append("\n")
                            .append("Кому хочет отдать: ").append(userRepository.findByChatId(application.getApplicationInvestor()).getFirstname()).append("\n")
                            .append("Сумма: ").append(application.getApplicationAmountTemp()).append("\n")
                            .append("Статус воспоминания: ").append(textApplicationStatus(application.getApplicationStatus())).append("\n")
                            .append("\n\n");
                    counter++;
                } else{
                    messageText.append(counter).append(". Дата создания заявки: ").append(application.getApplicationDate()).append("\n")
                            .append("Кто хочет занять: ").append(userRepository.findByChatId(application.getApplicationOwner()).getFirstname()).append("\n")
                            .append("У кого хочет занять: ").append(userRepository.findByChatId(application.getApplicationInvestor()).getFirstname()).append("\n")
                            .append("Сумма: ").append(application.getApplicationAmount()).append("\n")
                            .append("Статус воспоминания: ").append(textApplicationStatus(application.getApplicationStatus())).append("\n")
                            .append("\n\n");
                    counter++;
                }
            }

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageText.toString());
            message.setReplyMarkup(responseApplicationKeyboard());
            execute(message);
        }

    }

    public void responseApplicationButton(Long chatId,Session session){
        session.setSessionName("consider_application");
        session.setSessionStage("choice_application");
        sessionRepository.save(session);
        sendMessage(chatId, "Выбери номер заявки для рассмотрения, напиши число в чатик, без лишних символов! Cессия: " + sessionRepository.findBySessionChatId(chatId).getSessionStage());
    }

    public void choiseApplication(Message message, Session session) throws TelegramApiException {
        String textMessage = message.getText();
        Long chatId = message.getChatId();
        switch (session.getSessionStage()) {
            case "choice_application":
                try {
                    this.numberApplicationChoise = Integer.parseInt(textMessage) - 1;
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Вы неправильно ввели число, попробуйте еще раз. Сессия: " + sessionRepository.findBySessionChatId(chatId).getSessionStage());
                    return;
                }
                execute(InlineKeyboardTgButton.doneOrRejectApplicationKeyboardMessage(chatId));
                session.setSessionStage("done_or_reject_application");
                sessionRepository.save(session);
                break;
            default:
                sendMessage(chatId, "Что-то не так с твоим выбором заявки");
        }
    }

    public void doneOrRejectApplication(CallbackQuery callbackQuery, Session session) {
        String callBackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Application selectedApplication;
        try {
            selectedApplication = this.userApplications.get(this.numberApplicationChoise);
        } catch (Exception e) {
            sendMessage(chatId, "Неверный номер заявки");
            return;
        }
        Long ownerApplicationChatId = selectedApplication.getApplicationOwner();
        String investorUserName = userRepository.findByChatId(chatId).getFirstname();
        if (sessionRepository.findBySessionChatId(chatId).getSessionStage().equals("done_or_reject_application")) {
            switch (callBackData) {
                case "doneApplicationButton":
                    switch (selectedApplication.getApplicationAction()) {
                        case "take_money":
                            selectedApplication.setApplicationStatus("waiting_on_refunds");
                            applicationRepository.save(selectedApplication);
                            sendMessage(chatId, "Заявка одобрена, ждем возвращения монет");
                            sendMessage(ownerApplicationChatId, investorUserName + " одобрил твою заявку, ждем возвращения монет)");
                            break;
                        case "return_money_End":
                            selectedApplication.setApplicationStatus("return");
                            selectedApplication.setApplicationAction("close");
                            selectedApplication.setApplicationAmountTemp(0L);
                            selectedApplication.setApplicationAmountProcess(0L);
                            applicationRepository.save(selectedApplication);
                            sendMessage(chatId, "Заявка одобрена, долг покрыт");
                            sendMessage(ownerApplicationChatId, investorUserName + " одобрил твою заявку. Поздравляю! Долг закрыт!");
                            break;
                        case "return_money_noEnd":
                            Long newAmount = selectedApplication.getApplicationAmountProcess() - selectedApplication.getApplicationAmountTemp();
                            selectedApplication.setApplicationAction("take_money");
                            selectedApplication.setApplicationStatus("waiting_on_refunds");
                            selectedApplication.setApplicationAmountTemp(0L);
                            selectedApplication.setApplicationAmountProcess(newAmount);
                            applicationRepository.save(selectedApplication);
                            sendMessage(chatId, "Заявка одобрена, ждем возвращения монет");
                            sendMessage(ownerApplicationChatId, investorUserName + " одобрил твою заявку, ждем возвращения еще монет)");
                            break;
                        default:
                            sendMessage(chatId, "Что-то пошло не так при одобрении заявки");
                            break;
                    }
                    break;

                case "rejectApplicationButton":
                    switch (selectedApplication.getApplicationAction()) {
                        case "take_money":
                            selectedApplication.setApplicationStatus("reject");
                            applicationRepository.save(selectedApplication);
                            sendMessage(chatId, "Заявка отклонена");
                            sendMessage(ownerApplicationChatId, investorUserName + " отклонил(а) твою заявку(");
                            break;
                        case "return_money_End", "return_money_noEnd":
                            selectedApplication.setApplicationStatus("waiting_on_refunds");
                            selectedApplication.setApplicationAction("take_money");
                            selectedApplication.setApplicationAmountTemp(0L);
                            applicationRepository.save(selectedApplication);
                            sendMessage(chatId, "Заявка отклонена");
                            sendMessage(ownerApplicationChatId, investorUserName + " отклонил(а) твою заявку(");
                            break;
                        default:
                            sendMessage(chatId, "Не смогли отклонить заявку");
                            break;
                    }
                    break;

                default:
                    sendMessage(chatId, "Не смогли принять или отклонить заявку");
                    break;

            }
            session.setSessionStage("default");
            session.setSessionName("default");
            sessionRepository.save(session);
        } else{
            sendMessage(chatId, "Нет возможности");
        }
    }
}
