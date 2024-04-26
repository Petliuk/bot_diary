package com.example.bot_diary.utilities;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class AdminButtons {

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private static InlineKeyboardMarkup createInlineKeyboardMarkup(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rows);
        return markupInline;
    }

    public static InlineKeyboardMarkup getConfirmUserMarkup(Long chatId) {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createButton("Підтвердити", "CONFIRM_" + chatId));
        return createInlineKeyboardMarkup(List.of(rowInline));
    }

    public static InlineKeyboardMarkup getUserListButtons(Long chatId) {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createButton("Блокувати", "BLOCK_" + chatId));
        rowInline.add(createButton("Видалити", "DELETE_USER_" + chatId));
        return createInlineKeyboardMarkup(List.of(rowInline));
    }

    public static InlineKeyboardMarkup getBlockedUserButtons(Long chatId) {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createButton("Розблокувати", "UNBLOCK_" + chatId));
        rowInline.add(createButton("Видалити", "DELETE_USER_" + chatId));
        return createInlineKeyboardMarkup(List.of(rowInline));
    }

    public static InlineKeyboardMarkup getRegistrationRequestMarkup() {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createButton("Так", "APPLY_YES"));
        rowInline.add(createButton("Ні", "APPLY_NO"));
        return createInlineKeyboardMarkup(List.of(rowInline));
    }

    public static InlineKeyboardMarkup getMainMenuMarkup() {
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(createButton("Створити задачу", "CREATE_TASK"));

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(createButton("Мої завдання", "MY_TASKS"));

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);

        return createInlineKeyboardMarkup(rowsInline);
    }
}