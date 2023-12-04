package yaga.ex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ChatScreen {
    private final JTextArea chatTextArea;
    private final JScrollPane chatScrollPane;
    private final FileBasedChatApp chatApp;

    private final List<User> users = new ArrayList<>(); // Список пользователей

    private JList<User> userList; // Список пользователей
    private DefaultListModel<User> listModel; // Модель для списка пользователей

    private JFrame frame;

    private ChatScreen otherChatScreen1; // Ссылка на другой экземпляр ChatScreen
    private ChatScreen otherChatScreen2; // Ссылка на еще один экземпляр ChatScreen


    public ChatScreen() {
        // Добавляем подсказку о необходимости регистрации
        JOptionPane.showMessageDialog(null,
                "Для использования чата необходимо зарегистрироваться.",
                "Регистрация",
                JOptionPane.INFORMATION_MESSAGE);

        frame = new JFrame("Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);
        chatScrollPane = new JScrollPane(chatTextArea);
        frame.add(chatScrollPane, BorderLayout.CENTER);


        JTextField messageField = getjTextField();

        frame.add(messageField, BorderLayout.SOUTH);

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Разрешаем выбирать только одного пользователя
        JScrollPane userListScrollPane = new JScrollPane(userList);
        frame.add(userListScrollPane, BorderLayout.EAST);

        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int selectedIndex = userList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        User selectedUser = listModel.getElementAt(selectedIndex);
                        // Добавляем проверку, чтобы предотвратить выбор текущего пользователя
                        User currentUser = getCurrentUser();
                        if (selectedUser != null && !selectedUser.equals(currentUser)) {
                            String message = JOptionPane.showInputDialog("Введите личное сообщение для " + selectedUser.getUsername());
                            if (message != null && !message.isEmpty()) {
                                sendPrivateMessage(selectedUser, message);
                            }
                        } else {
                            // Всплывающее сообщение, если пользователь пытается отправить сообщение самому себе
                            JOptionPane.showMessageDialog(frame, "Самому себе нельзя отправить сообщение", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JButton addUserButton = new JButton("Зарегистрироваться в чате");
        addUserButton.addActionListener(e -> {
            String username = JOptionPane.showInputDialog("Введите имя пользователя:");
            if (username != null && !username.isEmpty()) {
                User newUser = new User(username);
                users.add(newUser);
                listModel.addElement(newUser);
                // Обновляем список пользователей в других окнах
                if (otherChatScreen1 != null) {
                    otherChatScreen1.updateUsersList(newUser);
                }
                if (otherChatScreen2 != null) {
                    otherChatScreen2.updateUsersList(newUser);
                }
                addUserButton.setEnabled(false);
            }
        });

        frame.add(addUserButton, BorderLayout.NORTH);

        frame.setVisible(true);

        chatApp = new FileBasedChatApp();

        new Thread(() -> chatApp.monitorChatFile(this)).start();

        chatApp.readAndPrintChatHistory(chatTextArea);
    }

    private JTextField getjTextField() {
        JTextField messageField = new JTextField("Введите текст"); // Добавляем подсказку
        messageField.setForeground(Color.GRAY); // Задаем цвет для прозрачных букв
        messageField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals("Введите текст")) {
                    messageField.setText("");
                    messageField.setForeground(Color.BLACK); // Меняем цвет обратно на черный
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setForeground(Color.GRAY); // Если поле пустое, устанавливаем цвет серым
                    messageField.setText("Введите текст");
                }
            }
        });
        messageField.addActionListener(e -> {
            if (!messageField.getText().isEmpty() && !messageField.getText().equals("Введите текст")) {
                User currentUser = getCurrentUser();
                if (currentUser != null) {
                    String message = messageField.getText();
                    sendMessage(String.valueOf(currentUser), message);
                    messageField.setText("");
                    messageField.setForeground(Color.GRAY);
                }
            }
        });
        return messageField;
    }

    public User getCurrentUser() {
        if (!users.isEmpty()) {
            return users.get(users.size() - 1);
        }
        return null;
    }

    public void updateChatArea() {
        SwingUtilities.invokeLater(() -> {
            chatApp.readAndPrintChatHistory(chatTextArea);

            // Добавляем отображение личных сообщений текущего пользователя
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                List<String> privateMessages = currentUser.getPrivateMessages();
                for (String privateMessage : privateMessages) {
                    chatTextArea.append(privateMessage + "\n");
                }
            }

            chatScrollPane.getVerticalScrollBar().setValue(chatScrollPane.getVerticalScrollBar().getMaximum());
        });
    }


    public void sendMessage(String recipient, String message) {
        if (!users.isEmpty()) {
            User currentUser = users.get(users.size() - 1);
            chatApp.writeMessage(currentUser.getUsername(), recipient, message);
            updateChatArea();
            // Добавляем обновление чата на других экранах
            if (otherChatScreen1 != null) {
                otherChatScreen1.updateChatArea();
            }
            if (otherChatScreen2 != null) {
                otherChatScreen2.updateChatArea();
            }
        }
    }

    public void sendPrivateMessage(User recipient, String message) {
        User currentUser = getCurrentUser();
        if (currentUser != null && recipient != null) {
            // Отправляем личное сообщение
            chatApp.writePrivateMessage(currentUser.getUsername(), recipient.getUsername(), message);

            // Сохраняем личное сообщение у отправителя и получателя
            currentUser.addPrivateMessage("[Личное сообщение для] " + recipient.getUsername() + ": " + message);
            recipient.addPrivateMessage("[Личное сообщение от] " + currentUser.getUsername() + ": " + message);

            updateChatArea();

            // Обновляем чат на других экранах
            if (otherChatScreen1 != null) {
                otherChatScreen1.updateChatArea();
            }
            if (otherChatScreen2 != null) {
                otherChatScreen2.updateChatArea();
            }
        }
    }


    // Метод для установки ссылки на другие экземпляры ChatScreen
    public void setOtherChatScreens(ChatScreen otherChatScreen1, ChatScreen otherChatScreen2) {
        this.otherChatScreen1 = otherChatScreen1;
        this.otherChatScreen2 = otherChatScreen2;
    }

    // Метод для обновления списка пользователей в JList
    public void updateUsersList(User newUser) {
        SwingUtilities.invokeLater(() -> {
            listModel.addElement(newUser);
        });
    }
}
