package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.pages_handler.comands.BotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class MessageService implements BotService {

    private final AbsSender absSender;

    public MessageService(@Lazy AbsSender absSender) {
        this.absSender = absSender;
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        if (text == null || text.isEmpty()) {
            log.error("Attempted to send empty message to chatId: " + chatId);
            return;
        }
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    @Override
    public void sendMessage(SendMessage message) throws TelegramApiException {
        absSender.execute(message);
    }

    @Override
    public void editMessage(EditMessageText editMessageText) throws TelegramApiException {
        absSender.execute(editMessageText);
    }

    public void sendHelpMessage(Long chatId) {
        String helpText = "🤖 **Вітаємо у вашому персональному організаторі задач!**\n\n" +
                "✨ **Основні команди:**\n" +
                "- **/start** — Розпочніть роботу з ботом та створіть ваші перші задачі.\n" +
                "- **/newtask** — Створіть нову задачу, додавши опис та вибравши час виконання.\n" +
                "- **/calendar** — Перегляньте всі ваші задачі в календарі.\n" +
                "- **/alltasks** — Отримайте список усіх ваших задач.\n" +
                "- **/postponed** — Перегляньте відкладені задачі.\n" +
                "- **/done** — Перегляньте виконані задачі.\n\n" +
                "🕒 **Нагадування:** Якщо ви встановили час для задачі, бот нагадає вам про неї за 10 хвилин до початку.\n\n" +
                "🔧 **Керування задачами:** Ви можете видалити задачу, позначити як відкладену, виконану, або повернути до списку невиконаних.\n\n" +
                "💳 **Про оплату:**\n" +
                "- Бот надається безкоштовно на перші 15 днів.\n" +
                "- Після цього місячне використання коштує 100 грн.\n" +
                "- Оплата виконується через розділ 'Оплата', про що вам надійде сповіщення.";

        sendMessage(chatId, helpText);
    }

}