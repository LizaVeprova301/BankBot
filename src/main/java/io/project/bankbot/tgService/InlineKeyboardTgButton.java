package io.project.bankbot.tgService;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyboardTgButton {


    public static SendMessage doneOrRejectApplicationKeyboardMessage (long chat_id) {

        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText("Выбери одобрить или отклонить заявку");

        InlineKeyboardMarkup doneOrRejectApplicationInlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();

        inlineKeyboardButton1.setText("Одобрить");

        inlineKeyboardButton1.setCallbackData("doneApplicationButton");

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Отклонить");
        inlineKeyboardButton2.setCallbackData("rejectApplicationButton");


        rowInline1.add(inlineKeyboardButton1);
        rowInline1.add(inlineKeyboardButton2);

        rowsInline.add(rowInline1);


        doneOrRejectApplicationInlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(doneOrRejectApplicationInlineKeyboardMarkup);
        return message;
    }

    public static InlineKeyboardMarkup responseApplicationKeyboard () {

        InlineKeyboardMarkup responseApplicationInlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();


        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();

        inlineKeyboardButton1.setText("Ответить на заявку");

        inlineKeyboardButton1.setCallbackData("responseApplicationButton");

        rowInline1.add(inlineKeyboardButton1);

// настраиваем разметку всей клавиатуры
        rowsInline.add(rowInline1);


// добавляем встроенную клавиатуру в сообщение
        responseApplicationInlineKeyboard.setKeyboard(rowsInline);
        return responseApplicationInlineKeyboard;
    }
    public static InlineKeyboardMarkup backButtonKeyboard () {

        InlineKeyboardMarkup backButtonInlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();

        inlineKeyboardButton1.setText("Назад");

        inlineKeyboardButton1.setCallbackData("backButton");

        rowInline1.add(inlineKeyboardButton1);

        rowsInline.add(rowInline1);
        backButtonInlineKeyboard.setKeyboard(rowsInline);
        return backButtonInlineKeyboard;
    }
}
