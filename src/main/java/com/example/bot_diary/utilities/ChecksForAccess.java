package com.example.bot_diary.utilities;

import com.example.bot_diary.models.User;
import com.example.bot_diary.models.UserState;
import com.example.bot_diary.models.UserStatus;
import com.example.bot_diary.pages_handler.comands.command_handler.MessageService;
import com.example.bot_diary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Component
public class ChecksForAccess {

    @Autowired
    private UserService userService;

    @Autowired
    private  MessageService messageService;

    @Value("${admin.chat.id}")
    private Long adminChatId;

    public boolean isUserEligibleToProceed(long chatId, Optional<User> userOptional, String messageText) throws TelegramApiException {
        if (userOptional.isPresent()) {
            UserStatus status = userOptional.get().getStatus();

            if (status == UserStatus.BLOCKED) {
                messageService.sendMessage(chatId, "Ви заблоковані та більше немаєте доступу до бота.");
                return false;
            }

            if (status == UserStatus.UNCONFIRMED) {
                messageService.sendMessage(chatId, "Вашу заявку ще не підтверджено. Будь ласка, зачекайте, поки адміністратор перевірить вашу заявку.");
                return false;
            }
        }

        if (!userOptional.isPresent() && !"/start".equals(messageText)) {
            SendMessage message = new SendMessage(String.valueOf(chatId), "Будь ласка, спочатку зареєструйтесь, використовуючи команду /start.");
            messageService.sendMessage(message);
            return false;
        }
        return true;
    }

    public void blockUser(Long userId, Long adminChatId) {
        if (userId.equals(adminChatId)) {
            messageService.sendMessage(adminChatId, "Ти себе не зможеш заблокувати, ти ж адмін");
        } else {
            userService.findUserByChatId(userId).ifPresent(user -> {
                if (user.getStatus() != UserStatus.BLOCKED) {
                    user.setStatus(UserStatus.BLOCKED);
                    userService.saveUser(user);

                    messageService.sendMessage(adminChatId, String.format("Користувача %s %s (ID: %d) заблоковано.",
                            user.getFirstName(), user.getLastName(), userId));

                    messageService.sendMessage(userId, "Ви були заблоковані та більше не маєте доступу до бота.");
                }
            });
        }
    }

    public void unblockUser(Long userId) {
        userService.findUserByChatId(userId).ifPresent(user -> {
            user.setStatus(UserStatus.CONFIRMED);
            userService.saveUser(user);

            String adminMessage = String.format("Користувача %s %s (ID: %d) розблоковано.",
                    user.getFirstName(), user.getLastName(), user.getChatId());
            messageService.sendMessage(adminChatId, adminMessage);

            String userMessage = "Ваш доступ до боту було відновлено.";
            messageService.sendMessage(user.getChatId(), userMessage);
        });
    }

    public void deleteUser(Long userId, Long adminChatId) {
        if (userId.equals(adminChatId)) {
            messageService.sendMessage(adminChatId, "Ти себе не зможеш видалити, ти ж адмін");
        } else {
            userService.findUserByChatId(userId).ifPresent(user -> {
                userService.deleteUser(user);
                messageService.sendMessage(adminChatId, "Користувача видалено: " + user.getFirstName() + " " + user.getLastName());
            });
        }
    }

    public boolean preChecks(Update update, UserState currentState, Optional<User> userOptional, long chatId) throws TelegramApiException {
        if (userOptional.isPresent() && userOptional.get().getStatus() == UserStatus.BLOCKED) {
            messageService.sendMessage(chatId, "Ви заблоковані та більше немаєте доступу до боту.");
            return false;
        }
        if (!(chatId == adminChatId) && userOptional.isPresent() && userOptional.get().getStatus() == UserStatus.UNCONFIRMED) {
            messageService.sendMessage(chatId, "Вашу заявку ще не підтверджено. Будь ласка, зачекайте, поки адміністратор перевірить вашу заявку.");
            return false;
        }
        String callbackData = update.getCallbackQuery().getData();
        if (callbackData.equals("continue_creation") || callbackData.equals("save_task_now")) {
            if (currentState != UserState.TASK_CREATED) {
                messageService.sendMessage(chatId, "Ці кнопки доступні тільки під час створення задачі.");
                return false;
            }
        }
        return true;
    }

}
