package yaga.ex;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

            SwingUtilities.invokeLater(() -> {
                ChatScreen chatScreen1 = new ChatScreen();
                ChatScreen chatScreen2 = new ChatScreen();
                ChatScreen chatScreen3 = new ChatScreen();

                chatScreen1.setOtherChatScreens(chatScreen2, chatScreen3);
                chatScreen2.setOtherChatScreens(chatScreen1, chatScreen3);
                chatScreen3.setOtherChatScreens(chatScreen1, chatScreen2);
            });
        }
}
