import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AIChatbot extends JFrame {

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    // Use fully-qualified name to avoid ambiguity
    private java.util.List<String> stopwords;

    private Map<String, String> faq;

    public AIChatbot() {

        setTitle("AI Chatbot");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 16));

        JScrollPane scroll = new JScrollPane(chatArea);

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        faq = loadFAQs();
        stopwords = defaultStopwords();

        // ACTION HANDLERS
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processMessage();
            }
        });

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processMessage();
            }
        });

        setVisible(true);
    }

    private void processMessage() {
        String userText = inputField.getText().trim();
        if (userText.length() == 0) return;

        chatArea.append("You: " + userText + "\n");

        String botReply = chatResponse(userText);

        chatArea.append("Bot: " + botReply + "\n\n");

        inputField.setText("");
    }

    private String chatResponse(String question) {

        question = question.toLowerCase();

        // Exact match in FAQs
        java.util.List<String> tokens = tokenize(question);

        for (String key : faq.keySet()) {
            if (question.contains(key)) {
                return faq.get(key);
            }
        }

        // Word similarity based matching
        for (String key : faq.keySet()) {

            java.util.List<String> keyTokens = tokenize(key);

            int matchCount = 0;

            for (int i = 0; i < tokens.size(); i++) {
                if (keyTokens.contains(tokens.get(i))) {
                    matchCount++;
                }
            }

            if (matchCount >= 2) {
                return faq.get(key);
            }
        }

        return "Sorry, I don't understand. Can you rephrase?";
    }

    private java.util.List<String> tokenize(String text) {

        java.util.List<String> list = new ArrayList<String>();

        text = text.replaceAll("[^a-zA-Z ]", " ");
        String[] arr = text.split(" ");

        for (int i = 0; i < arr.length; i++) {
            String w = arr[i].trim();
            if (w.length() > 0 && !stopwords.contains(w)) {
                list.add(w);
            }
        }
        return list;
    }

    private Map<String, String> loadFAQs() {
        Map<String, String> map = new HashMap<String, String>();

        map.put("hello", "Hello! How can I assist you today?");
        map.put("hi", "Hi! How can I help?");
        map.put("your name", "I am a Java AI Chatbot created for your project.");
        map.put("java", "Java is a high-level programming language used for OOP applications.");
        map.put("what is ai", "AI means Artificial Intelligence – machines that can think and learn.");
        map.put("bye", "Goodbye! Have a nice day.");
        map.put("help", "Sure! Tell me what you want help with.");

        // You can train more FAQs easily

        return map;
    }

    private java.util.List<String> defaultStopwords() {
        java.util.List<String> s = new ArrayList<String>();

        String[] arr = {
                "the", "is", "am", "are", "a", "an", "and", "to",
                "of", "what", "why", "who", "how", "when", "where"
        };

        for (int i = 0; i < arr.length; i++) {
            s.add(arr[i]);
        }
        return s;
    }

    public static void main(String[] args) {
        new AIChatbot();
    }
}
