package com.example.bot_diary.utilities;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminButtons {

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    @SafeVarargs
    private static InlineKeyboardMarkup createInlineKeyboardMarkup(List<InlineKeyboardButton>... rows) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(Arrays.asList(rows));
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public static InlineKeyboardMarkup getConfirmUserMarkup(Long chatId) {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createButton("Підтвердити", "CONFIRM_" + chatId));
        return createInlineKeyboardMarkup(rowInline);
    }

    public static InlineKeyboardMarkup getUserListButtons(Long chatId) {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createButton("Блокувати", "BLOCK_" + chatId));
        rowInline.add(createButton("Видалити", "DELETE_USER_" + chatId));
        return createInlineKeyboardMarkup(rowInline);
    }

    public static InlineKeyboardMarkup getBlockedUserButtons(Long chatId) {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createButton("Розблокувати", "UNBLOCK_" + chatId));
        rowInline.add(createButton("Видалити", "DELETE_USER_" + chatId));
        return createInlineKeyboardMarkup(rowInline);
    }
}