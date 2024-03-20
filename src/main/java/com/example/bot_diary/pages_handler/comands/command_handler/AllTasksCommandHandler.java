package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class AllTasksCommandHandler {

    @Autowired
    private BotService botService;

    @Autowired
    private TaskService taskService;

    public void handle(Update update) throws TelegramApiException {

        // Використання chatId для отримання задач конкретного користувача
        long chatId = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
       /* List<Task> tasks = taskService.findAllTasksByUserChatId(chatId);*/
        List<Task> tasks = taskService.findTasksByStatusAndUserChatId(TaskStatus.NOT_COMPLETED,chatId);

        if (tasks.isEmpty()) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Задач немає.");
            botService.sendMessage(message);
        } else {
            for (Task task : tasks) {

                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();

                InlineKeyboardButton deleteButton = new InlineKeyboardButton();
                deleteButton.setText("Видалити");
                deleteButton.setCallbackData("DELETE_TASK_" + task.getId());
                rowInline.add(deleteButton);


                InlineKeyboardButton doneButton = new InlineKeyboardButton();
                doneButton.setText("Виконано");
                doneButton.setCallbackData("DONE_TASK_" + task.getId());
                rowInline.add(doneButton); // Додайте цю кнопку до рядка інлайн-клавіатури


                InlineKeyboardButton postponeButton = new InlineKeyboardButton();
                postponeButton.setText("Відкласти");
                postponeButton.setCallbackData("POSTPONE_TASK_" + task.getId());
                rowInline.add(postponeButton); // Додайте цю кнопку до рядка інлайн-клавіатури


                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);

             /*   response.append("ID: ").append(task.getId())
                        .append("\nОпис: ").append(task.getDescription())
                        .append("\nСтатус: ").append(task.getStatus().getDisplayName())
                        .append("\n\n");*/

                String messageText = "🗓 Дата: " + task.getDueDate().toLocalDate() + "\n" +
                        "⏰ Час: " + task.getDueDate().toLocalTime() + "\n" +
                        "🔖 Статус: " + task.getStatus().getDisplayName() + "\n" +
                        "📝 Опис: " + task.getDescription() + "\n";


                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(messageText);
                message.setReplyMarkup(markupInline);
                botService.sendMessage(message);

            }
        }

    }

}