package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class DeleteTasksCommandHandler {

    private final TaskService taskService;
    private final MessageService messageService;

    public void handle(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        Long taskId = Long.parseLong(callbackData.split("_")[2]);

        taskService.deleteTask(taskId);

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Завдання з ID " + taskId + " успішно видалено.");

        messageService.sendMessage(message);
    }
}
