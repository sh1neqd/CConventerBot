package ru.dakon.CConventerBot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.dakon.CConventerBot.Model.User;
import ru.dakon.CConventerBot.Model.UserRepository;
import ru.dakon.CConventerBot.config.BotConfig;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConverterService converterService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    static final String USD10_BUTTON = "10USD_BUTTON";
    static final String USD50_BUTTON = "50USD_BUTTON";
    static final String ELSE_BUTTON = "ELSE_BUTTON";
    static final String SQL_FOR_PRICE = "SELECT value FROM price WHERE id=1";
    static final String HELP_MESSAGE = "Доступные на данный момент команды:\n\n /exchange - переводит ваше количество валюты в рубли\n" +
            "/todaysrate - показывает сегодняшний курс доллара\n/help - список команд\n\nКонтакт для предложений - @dakonxd";


    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Перезапуск бота"));
        listOfCommands.add(new BotCommand("/exchange","Перевод долларов в рубли"));
        listOfCommands.add(new BotCommand("/help","Список доступных команд"));
        listOfCommands.add(new BotCommand("/todaysrate","Курс доллара на сегодня"));
        try{
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("error setting bot's command list" + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            User user = createNullUser();
            if(!userRepository.findById(chatId).isEmpty()) {
                user = userRepository.findById(chatId).get();
            }
                switch (messageText) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        updateUserStatus(user, 0);
                        break;
                    case "/exchange":
                        getUserAmount(chatId);
                        break;
                    case "/todaysrate":
                        sendTodayCurrency(chatId);
                        break;
                    case "/help":
                        sendMessage(chatId, HELP_MESSAGE);
                        break;
                    default:
                        if(getUserStatus(chatId)==0){
                        sendMessage(chatId, "Неизвестная команда");
                        }
                        if(getUserStatus(chatId)==1) {
                            if (isRightNumber(messageText)) {
                                sendUserExchange(chatId, Integer.parseInt(messageText));
                                updateUserStatus(user, 0);
                            } else {
                            sendMessage(chatId, "Сумма должна быть больше 0");
                        }
                    }

            }

        } else if (update.hasCallbackQuery()) {
            Integer amount = null;
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            User user = userRepository.findById(chatId).get();

            if(callbackData.equals(USD10_BUTTON)){
                amount = 10;
                sendUserExchange(chatId, amount);
            }
            else if(callbackData.equals(USD50_BUTTON)){
                amount = 50;
                sendUserExchange(chatId, amount);
            } else if(callbackData.equals(ELSE_BUTTON)){
                sendMessage(chatId, "Введите количество:");
                updateUserStatus(user, 1);
            }
            
        }
    }


    private void startCommandReceived(long chatId, String name) {

        String answer = "Привет, " + name + ", добро пожаловать в нашего бота. Здесь ты можешь обменять валюту. Для этого пропиши команду /exchange";

        log.info("Replied to user " + name);

        sendMessage(chatId, answer);


    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try{
            execute(message);
        }catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void registerUser(Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()) {

            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setUserName(chat.getUserName());
            user.setStatus(0);

            userRepository.save(user);
            log.info("user " + user.getUserName()+ " "+ user.getChatId() + " has registered");
        }

    }

    @Scheduled(fixedDelay = 86400000)
    private void getDailyExchange() {
        converterService.getUsdToRub();
    }

    private void sendUserExchange(long chatId, Integer amount) {
        var price = jdbcTemplate.queryForObject(SQL_FOR_PRICE, Double.class);
        sendMessage(chatId, "Cегодня " + amount + "$ = " + amount * price + " рублей");
    }

    private void getUserAmount(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Какое количество $ вы хотите перевести?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var USD10Button = new InlineKeyboardButton();

        USD10Button.setText("10$");
        USD10Button.setCallbackData(USD10_BUTTON);

        var USD50Button = new InlineKeyboardButton();

        USD50Button.setText("50$");
        USD50Button.setCallbackData(USD50_BUTTON);

        var elseButton = new InlineKeyboardButton();

        elseButton.setText("Другое значение");
        elseButton.setCallbackData(ELSE_BUTTON);


        rowInLine.add(USD10Button);
        rowInLine.add(USD50Button);
        rowInLine.add(elseButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);

    }


    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred:" + e.getMessage());
        }
    }

    private void updateUserStatus(User user, Integer status) {
        user.setStatus(status);
        jdbcTemplate.update("UPDATE users_data SET status=? WHERE chat_id=?", user.getStatus(), user.getChatId());
    }

    private boolean isRightNumber(String messageText) {
        if(messageText.matches("^[1-9]\\d*$")) {
            return true;
        } else return false;
    }

    private Integer getUserStatus(Long chatId) {
        return jdbcTemplate.queryForObject("SELECT status FROM users_data WHERE chat_id=?", Integer.class, chatId);
    }

    private User createNullUser() {
        User user = new User();
        user.setChatId(null);
        user.setUserName(null);
        user.setStatus(null);
        return user;
    }

    private void sendTodayCurrency(Long chatId) {
        var price = jdbcTemplate.queryForObject(SQL_FOR_PRICE, Double.class);
        sendMessage(chatId, "Курс доллара на сегодня: " + price + " рублей \n\n" +"Для конвертации вашего значения валюты " +
                "введите /exchange");
    }


}
