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

    public ChatScreen() {
        JFrame frame = new JFrame("Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);
        chatScrollPane = new JScrollPane(chatTextArea);
        frame.add(chatScrollPane, BorderLayout.CENTER);

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
                    sendMessage(currentUser, message);
                    messageField.setText("");
                    messageField.setForeground(Color.GRAY);
                }
            }
        });

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
                        String message = JOptionPane.showInputDialog("Введите сообщение для " + selectedUser.getUsername());
                        if (message != null && !message.isEmpty()) {
                            sendMessage(selectedUser, message);
                        }
                    }
                }
            }
        });

        // Создаем кнопку "Добавить пользователя"
        JButton addUserButton = new JButton("Добавить пользователя");
        addUserButton.addActionListener(e -> {
            String username = JOptionPane.showInputDialog("Введите имя пользователя:");
            if (username != null && !username.isEmpty()) {
                User newUser = new User(username);
                users.add(newUser);
                listModel.addElement(newUser); // Добавляем пользователя в список
            }
        });

        frame.add(addUserButton, BorderLayout.NORTH);

        frame.setVisible(true);

        // Создаем экземпляр FileBasedChatApp в конструкторе
        chatApp = new FileBasedChatApp();

        // Запускаем мониторинг чата после создания окна чата
        new Thread(() -> chatApp.monitorChatFile(this)).start();

        // Инициализируем начальную историю чата
        chatApp.readAndPrintChatHistory(chatTextArea);
    }

    //Получает текущего пользователя, который является последним в списке пользователей.
    public User getCurrentUser() {
        if (!users.isEmpty()) {
            return users.get(users.size() - 1);
        }
        return null;
    }

    //Обновляет область чата, загружая и выводя историю чата и прокручивая вниз до последнего сообщения.
    public void updateChatArea() {
        SwingUtilities.invokeLater(() -> {
            chatApp.readAndPrintChatHistory(chatTextArea);
            chatScrollPane.getVerticalScrollBar().setValue(chatScrollPane.getVerticalScrollBar().getMaximum());
        });
    }

    //Отправляет сообщение от текущего пользователя (последнего в списке пользователей) выбранному получателю.
    public void sendMessage(String recipient, String message) {
        if (!users.isEmpty()) {
            User currentUser = users.get(users.size() - 1);
            chatApp.writeMessage(currentUser.getUsername(), recipient, message);
            updateChatArea();
        }
    }

    public void sendMessage(User recipient, String message) {
        sendMessage(recipient.getUsername(), message);
    }
}
