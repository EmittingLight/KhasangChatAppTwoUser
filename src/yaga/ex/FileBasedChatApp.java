package yaga.ex;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileBasedChatApp {
    private static final String chatFileName = "chat.txt";

    public FileBasedChatApp() {
        // Конструктор без аргументов
        ensureFileExists(chatFileName);
    }

    // Метод для проверки существования файла и создания, если его нет
    public void ensureFileExists(String filename) {
        Path path = Paths.get(filename);
        if (Files.notExists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                System.err.println("Ошибка при создании файла: " + e.getMessage());
            }
        }
    }

    //Метод записывает сообщение в файл чата.
    public void writeMessage(String sender, String recipient, String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(new Date());
        String formattedMessage = formattedDate + " "+ sender + ": " + message;

        try (FileWriter fileWriter = new FileWriter(chatFileName, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(formattedMessage);
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }
    //Метод считывает и выводит историю чата в текстовую область
    public void readAndPrintChatHistory(JTextPane chatTextPane) {
        try (FileReader fileReader = new FileReader(chatFileName);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            chatTextPane.setText("");  // Очистить текстовую область перед добавлением новой истории чата
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                appendFormattedLine(chatTextPane, line);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    // Вспомогательный метод для форматированного добавления строки в JTextPane
    private void appendFormattedLine(JTextPane chatTextPane, String line) {
        // Разбиваем строку на компоненты: дата, логин, сообщение
        String[] components = line.split(" ", 4);

        if (components.length == 4) {
            String formattedDate = components[0] + " " + components[1];
            String sender = components[2].substring(0, components[2].length() - 1); // Удаляем ":" в конце логина
            String message = components[3];

            SimpleAttributeSet dateAttrs = new SimpleAttributeSet();
            StyleConstants.setForeground(dateAttrs, java.awt.Color.BLUE);

            SimpleAttributeSet senderAttrs = new SimpleAttributeSet();
            StyleConstants.setForeground(senderAttrs, java.awt.Color.GREEN);

            SimpleAttributeSet messageAttrs = new SimpleAttributeSet();
            StyleConstants.setForeground(messageAttrs, java.awt.Color.BLACK);

            try {
                chatTextPane.getStyledDocument().insertString(chatTextPane.getDocument().getLength(), formattedDate + " ", dateAttrs);
                chatTextPane.getStyledDocument().insertString(chatTextPane.getDocument().getLength(), sender + ": ", senderAttrs);
                chatTextPane.getStyledDocument().insertString(chatTextPane.getDocument().getLength(), message + "\n", messageAttrs);
            } catch (javax.swing.text.BadLocationException e) {
                e.printStackTrace();
            }
        } else {
            // Если строка не соответствует ожидаемому формату, добавляем как есть
            chatTextPane.setText(chatTextPane.getText() + line + "\n");
        }
    }

    //Метод следит за изменениями в файле чата,
    // обновляя текстовую область чата в реальном времени при каждом изменении.
    public void monitorChatFile(ChatScreen chatScreen) {
        try {
            Path chatFilePath = Paths.get(chatFileName);
            WatchService watchService = FileSystems.getDefault().newWatchService();
            chatFilePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().toString().equals(chatFileName)) {
                        chatScreen.updateChatArea();
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writePrivateMessage(String sender, String recipient, String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedMessage = dateFormat.format(new Date()) + " - " + sender + " -> " + recipient + ": " + message;

        // Определите путь к файлу для личных сообщений
        String privateChatFileName = "private_chat_" + sender + "_" + recipient + ".txt";

        try (FileWriter fileWriter = new FileWriter(privateChatFileName, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(formattedMessage);
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }
}