package com.example.bot_diary.pages_handler.comands.command_handler;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class DeleteTasksCommandHandler {
    @Autowired
    private TaskService taskService;

    @Autowired
    private BotService botService;

    public void handle(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        Long taskId = Long.parseLong(callbackData.split("_")[2]);

        // Видалення завдання за ID
        taskService.deleteTask(taskId);

        // Відправлення повідомлення про успішне видалення
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Завдання з ID " + taskId + " успішно видалено.");

        botService.sendMessage(message);
    }
}
