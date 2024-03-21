package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.service.TaskService;
import com.example.bot_diary.utilities.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AllTasksCommandHandler {

    private final BotService botService;
    private final TaskService taskService;

    public void handle(Update update) throws TelegramApiException {

        long chatId = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
        List<Task> tasks = taskService.findTasksByStatusAndUserChatId(TaskStatus.NOT_COMPLETED, chatId);

        if (tasks.isEmpty()) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Задач немає.");
            botService.sendMessage(message);
        } else {
            MessageUtils.sendTaskMessages(tasks, chatId, botService);
        }
    }

}
