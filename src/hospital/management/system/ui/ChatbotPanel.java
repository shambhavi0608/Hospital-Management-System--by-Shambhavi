package hospital.management.system.ui;

import hospital.management.system.chatbot.AssistantLanguage;
import hospital.management.system.chatbot.ChatbotService;
import hospital.management.system.chatbot.VoiceAssistantService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Multilingual text and voice HMS Assistant panel.
 */
public final class ChatbotPanel extends JPanel {

    private static final Color NAVY =
            new Color(7, 44, 90);

    private static final Color BLUE =
            new Color(22, 104, 220);

    private static final Color TEAL =
            new Color(13, 177, 177);

    private static final Color PAGE_BACKGROUND =
            new Color(245, 248, 252);

    private static final Color MUTED_TEXT =
            new Color(88, 104, 130);

    private static final Color BORDER_COLOR =
            new Color(218, 227, 238);

    /*
     * Windows ka Unicode font.
     * Hindi, Marathi, Gujarati, Bengali, Tamil,
     * Telugu, Kannada, Malayalam, Punjabi etc.
     * ke liye much better support.
     */
    private static final String UNICODE_FONT =
            findUnicodeFont();

    private final ChatbotService chatbotService =
            new ChatbotService();

    private final VoiceAssistantService voiceService =
            new VoiceAssistantService();

    private final JTextPane conversationPane =
            new JTextPane();

    private final JTextField inputField =
            new JTextField();

    private final JButton sendButton =
            new ModernButton("SEND");

    private final JButton microphoneButton =
            new ModernButton("MIC");

    private final JComboBox<AssistantLanguage>
            languageBox =
            new JComboBox<>(
                    AssistantLanguage.values()
            );

    private final JCheckBox speakRepliesBox =
            new JCheckBox("Speak replies");

    private final JLabel statusLabel =
            new JLabel("Ready");

    public ChatbotPanel() {

        setLayout(
                new BorderLayout(
                        0,
                        18
                )
        );

        setBackground(
                PAGE_BACKGROUND
        );

        setBorder(
                new EmptyBorder(
                        26,
                        28,
                        26,
                        28
                )
        );

        add(
                createHeader(),
                BorderLayout.NORTH
        );

        add(
                createContent(),
                BorderLayout.CENTER
        );

        appendAssistantMessage(
                "Namaste! Main aapka Smart HMS operational "
                        + "assistant hoon. Aap patient count, "
                        + "available rooms, occupied rooms aur "
                        + "room occupancy ke baare mein pooch "
                        + "sakte hain."
        );

        updateVoiceStatus();
    }

    // ============================================================
    // UNICODE FONT
    // ============================================================

    private static String findUnicodeFont() {

        String[] preferredFonts = {
                "Nirmala UI",
                "Noto Sans",
                "Noto Sans Devanagari",
                "Arial Unicode MS",
                "Segoe UI"
        };

        String[] installedFonts =
                GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getAvailableFontFamilyNames();

        for (String preferred : preferredFonts) {

            for (String installed : installedFonts) {

                if (
                        installed.equalsIgnoreCase(
                                preferred
                        )
                ) {
                    return installed;
                }
            }
        }

        return "Dialog";
    }

    // ============================================================
    // HEADER
    // ============================================================

    private JComponent createHeader() {

        JPanel headerPanel =
                new JPanel(
                        new BorderLayout(
                                20,
                                0
                        )
                );

        headerPanel.setOpaque(false);

        JPanel titlePanel =
                new JPanel();

        titlePanel.setOpaque(false);

        titlePanel.setLayout(
                new BoxLayout(
                        titlePanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "Multilingual HMS Assistant"
                );

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        28
                )
        );

        titleLabel.setForeground(NAVY);

        JLabel subtitleLabel =
                new JLabel(
                        "Text and voice help for safe hospital operations"
                );

        subtitleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        subtitleLabel.setForeground(
                MUTED_TEXT
        );

        titlePanel.add(titleLabel);

        titlePanel.add(
                Box.createVerticalStrut(4)
        );

        titlePanel.add(subtitleLabel);

        JPanel toolsPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                5
                        )
                );

        toolsPanel.setOpaque(false);

        JLabel languageLabel =
                new JLabel("Language");

        languageLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        /*
         * IMPORTANT:
         * ComboBox ka default renderer Windows/LAF
         * font use karta tha, jiski wajah se
         * हिन्दी / தமிழ் etc. boxes ban rahe the.
         *
         * Custom renderer Unicode font force karta hai.
         */
        languageBox.setRenderer(
                new LanguageRenderer()
        );

        languageBox.setFont(
                new Font(
                        UNICODE_FONT,
                        Font.PLAIN,
                        14
                )
        );

        languageBox.setPreferredSize(
                new Dimension(
                        175,
                        40
                )
        );

        languageBox.setSelectedItem(
                AssistantLanguage.HINGLISH
        );

        languageBox.addActionListener(
                event -> updateVoiceStatus()
        );

        JLabel onlineLabel =
                new JLabel("● Online");

        onlineLabel.setForeground(
                new Color(
                        18,
                        164,
                        101
                )
        );

        onlineLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        toolsPanel.add(languageLabel);
        toolsPanel.add(languageBox);
        toolsPanel.add(onlineLabel);

        headerPanel.add(
                titlePanel,
                BorderLayout.WEST
        );

        headerPanel.add(
                toolsPanel,
                BorderLayout.EAST
        );

        return headerPanel;
    }

    // ============================================================
    // LANGUAGE RENDERER
    // ============================================================

    private static final class LanguageRenderer
            extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {

            JLabel label =
                    (JLabel)
                            super.getListCellRendererComponent(
                                    list,
                                    value,
                                    index,
                                    isSelected,
                                    cellHasFocus
                            );

            label.setFont(
                    new Font(
                            UNICODE_FONT,
                            Font.PLAIN,
                            14
                    )
            );

            label.setBorder(
                    new EmptyBorder(
                            5,
                            8,
                            5,
                            8
                    )
            );

            if (
                    value
                            instanceof AssistantLanguage
            ) {

                AssistantLanguage language =
                        (AssistantLanguage) value;

                label.setText(
                        language.getDisplayName()
                );
            }

            return label;
        }
    }

    // ============================================================
    // CONTENT
    // ============================================================

    private JComponent createContent() {

        JPanel bodyPanel =
                new JPanel(
                        new BorderLayout(
                                18,
                                0
                        )
                );

        bodyPanel.setOpaque(false);

        bodyPanel.add(
                createChatCard(),
                BorderLayout.CENTER
        );

        bodyPanel.add(
                createInformationCard(),
                BorderLayout.EAST
        );

        return bodyPanel;
    }

    // ============================================================
    // CHAT CARD
    // ============================================================

    private JComponent createChatCard() {

        JPanel cardPanel =
                new JPanel(
                        new BorderLayout(
                                0,
                                12
                        )
                );

        cardPanel.setBackground(
                Color.WHITE
        );

        cardPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(
                                16,
                                16,
                                14,
                                16
                        )
                )
        );

        conversationPane.setEditable(false);

        conversationPane.setBackground(
                Color.WHITE
        );

        conversationPane.setFont(
                new Font(
                        UNICODE_FONT,
                        Font.PLAIN,
                        14
                )
        );

        conversationPane.setBorder(
                new EmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );

        /*
         * JTextPane ke default document font ko bhi
         * Unicode font par set kar rahe hain.
         */
        SimpleAttributeSet defaultStyle =
                new SimpleAttributeSet();

        StyleConstants.setFontFamily(
                defaultStyle,
                UNICODE_FONT
        );

        StyleConstants.setFontSize(
                defaultStyle,
                14
        );

        conversationPane
                .getStyledDocument()
                .setCharacterAttributes(
                        0,
                        0,
                        defaultStyle,
                        false
                );

        JScrollPane conversationScrollPane =
                new JScrollPane(
                        conversationPane
                );

        conversationScrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(
                                231,
                                236,
                                243
                        )
                )
        );

        conversationScrollPane
                .getVerticalScrollBar()
                .setUnitIncrement(16);

        cardPanel.add(
                conversationScrollPane,
                BorderLayout.CENTER
        );

        cardPanel.add(
                createInputSection(),
                BorderLayout.SOUTH
        );

        return cardPanel;
    }

    // ============================================================
    // INPUT SECTION
    // ============================================================

    private JComponent createInputSection() {

        JPanel footerPanel =
                new JPanel(
                        new BorderLayout(
                                0,
                                10
                        )
                );

        footerPanel.setOpaque(false);

        footerPanel.add(
                createQuickActions(),
                BorderLayout.NORTH
        );

        JPanel inputRow =
                new JPanel(
                        new BorderLayout(
                                9,
                                0
                        )
                );

        inputRow.setOpaque(false);

        inputField.setFont(
                new Font(
                        UNICODE_FONT,
                        Font.PLAIN,
                        14
                )
        );

        inputField.setPreferredSize(
                new Dimension(
                        0,
                        50
                )
        );

        inputField.setToolTipText(
                "Type your hospital operational question"
        );

        inputField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        194,
                                        207,
                                        224
                                )
                        ),
                        new EmptyBorder(
                                0,
                                13,
                                0,
                                13
                        )
                )
        );

        inputField.addActionListener(
                event -> sendMessage()
        );

        microphoneButton.setToolTipText(
                "Click to speak"
        );

        sendButton.setToolTipText(
                "Send question"
        );

        styleButton(
                microphoneButton,
                Color.WHITE,
                BLUE
        );

        styleButton(
                sendButton,
                BLUE,
                Color.WHITE
        );

        microphoneButton.addActionListener(
                event -> listenFromMicrophone()
        );

        sendButton.addActionListener(
                event -> sendMessage()
        );

        JPanel buttons =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                8,
                                0
                        )
                );

        buttons.setOpaque(false);

        buttons.add(microphoneButton);
        buttons.add(sendButton);

        inputRow.add(
                inputField,
                BorderLayout.CENTER
        );

        inputRow.add(
                buttons,
                BorderLayout.EAST
        );

        footerPanel.add(
                inputRow,
                BorderLayout.CENTER
        );

        JPanel statusPanel =
                new JPanel(
                        new BorderLayout()
                );

        statusPanel.setOpaque(false);

        statusLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        statusLabel.setForeground(
                MUTED_TEXT
        );

        speakRepliesBox.setOpaque(false);

        speakRepliesBox.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        speakRepliesBox.setForeground(NAVY);

        statusPanel.add(
                statusLabel,
                BorderLayout.WEST
        );

        statusPanel.add(
                speakRepliesBox,
                BorderLayout.EAST
        );

        footerPanel.add(
                statusPanel,
                BorderLayout.SOUTH
        );

        return footerPanel;
    }

    // ============================================================
    // QUICK ACTIONS
    // ============================================================

    private JComponent createQuickActions() {

        JPanel panel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                7,
                                0
                        )
                );

        panel.setOpaque(false);

        panel.add(
                createQuickButton(
                        "Admitted Patients",
                        "How many patients are admitted?"
                )
        );

        panel.add(
                createQuickButton(
                        "Available Rooms",
                        "How many rooms are available?"
                )
        );

        panel.add(
                createQuickButton(
                        "Occupied Rooms",
                        "How many rooms are occupied?"
                )
        );

        panel.add(
                createQuickButton(
                        "Occupancy",
                        "What is the room occupancy?"
                )
        );

        panel.add(
                createQuickButton(
                        "Help",
                        "Help"
                )
        );

        return panel;
    }

    private JButton createQuickButton(
            String text,
            String message
    ) {

        JButton button =
                new ModernButton(text);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        button.setForeground(BLUE);

        button.setBackground(
                new Color(
                        237,
                        245,
                        255
                )
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                new EmptyBorder(
                        8,
                        10,
                        8,
                        10
                )
        );

        button.addActionListener(
                event -> {

                    inputField.setText(
                            message
                    );

                    sendMessage();
                }
        );

        return button;
    }

    // ============================================================
    // INFORMATION CARD
    // ============================================================

    private JComponent createInformationCard() {

        JPanel cardPanel =
                new JPanel();

        cardPanel.setBackground(
                Color.WHITE
        );

        cardPanel.setPreferredSize(
                new Dimension(
                        285,
                        0
                )
        );

        cardPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(
                                20,
                                20,
                                20,
                                20
                        )
                )
        );

        cardPanel.setLayout(
                new BoxLayout(
                        cardPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "Assistant Capabilities"
                );

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        17
                )
        );

        titleLabel.setForeground(NAVY);

        cardPanel.add(titleLabel);

        cardPanel.add(
                Box.createVerticalStrut(18)
        );

        addCapability(
                cardPanel,
                "Live database statistics",
                "Patient and room counts"
        );

        addCapability(
                cardPanel,
                "Multilingual input",
                "15 language options"
        );

        addCapability(
                cardPanel,
                "Voice mode",
                "Windows microphone and speech"
        );

        addCapability(
                cardPanel,
                "Natural language",
                "Common English and Hinglish questions"
        );

        addCapability(
                cardPanel,
                "Privacy aware",
                "No diagnosis or prescription"
        );

        cardPanel.add(
                Box.createVerticalGlue()
        );

        JPanel warningPanel =
                new JPanel(
                        new BorderLayout()
                );

        warningPanel.setBackground(
                new Color(
                        255,
                        248,
                        230
                )
        );

        warningPanel.setBorder(
                new EmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );

        JLabel warningLabel =
                new JLabel(
                        "<html><b>Safety</b><br>"
                                + "This assistant does not diagnose "
                                + "or prescribe medicine.</html>"
                );

        warningLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        warningLabel.setForeground(
                new Color(
                        132,
                        83,
                        11
                )
        );

        warningPanel.add(
                warningLabel,
                BorderLayout.CENTER
        );

        cardPanel.add(warningPanel);

        return cardPanel;
    }

    private void addCapability(
            JPanel parent,
            String title,
            String details
    ) {

        JLabel titleLabel =
                new JLabel(title);

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        titleLabel.setForeground(NAVY);

        JLabel detailsLabel =
                new JLabel(details);

        detailsLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        detailsLabel.setForeground(
                MUTED_TEXT
        );

        parent.add(titleLabel);

        parent.add(
                Box.createVerticalStrut(3)
        );

        parent.add(detailsLabel);

        parent.add(
                Box.createVerticalStrut(18)
        );
    }

    // ============================================================
    // SEND MESSAGE
    // ============================================================

    private void sendMessage() {

        String message =
                inputField
                        .getText()
                        .trim();

        if (message.isBlank()) {
            return;
        }

        appendMessage(
                "You",
                message,
                BLUE
        );

        inputField.setText("");

        AssistantLanguage language =
                getSelectedLanguage();

        setBusy(
                true,
                "Generating response..."
        );

        SwingWorker<String, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected String doInBackground() {

                        return chatbotService
                                .generateResponse(
                                        message,
                                        language
                                );
                    }

                    @Override
                    protected void done() {

                        try {

                            String response =
                                    get();

                            appendAssistantMessage(
                                    response
                            );

                            if (
                                    speakRepliesBox
                                            .isSelected()
                            ) {

                                speakResponse(
                                        response,
                                        language
                                );

                            } else {

                                setBusy(
                                        false,
                                        "Ready"
                                );
                            }

                        } catch (Exception exception) {

                            appendAssistantMessage(
                                    "Sorry, response generate nahi "
                                            + "ho paayi. Please try again."
                            );

                            setBusy(
                                    false,
                                    "Ready"
                            );
                        }
                    }
                };

        worker.execute();
    }

    // ============================================================
    // MICROPHONE
    // ============================================================

    private void listenFromMicrophone() {

        if (!voiceService.isSupported()) {

            setBusy(
                    false,
                    "Voice is available on Windows only."
            );

            return;
        }

        AssistantLanguage language =
                getSelectedLanguage();

        setBusy(
                true,
                "Listening... speak now"
        );

        SwingWorker<String, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected String doInBackground()
                            throws Exception {

                        return voiceService.listen(
                                language
                        );
                    }

                    @Override
                    protected void done() {

                        try {

                            String recognized =
                                    get();

                            inputField.setText(
                                    recognized
                            );

                            setBusy(
                                    false,
                                    "Speech captured"
                            );

                            sendMessage();

                        } catch (Exception exception) {

                            setBusy(
                                    false,
                                    "Voice unavailable"
                            );

                            JOptionPane.showMessageDialog(
                                    ChatbotPanel.this,
                                    "Voice input could not start.\n\n"
                                            + rootMessage(
                                            exception
                                    )
                                            + "\n\n"
                                            + "You can still type your question.",
                                    "Voice Input",
                                    JOptionPane.WARNING_MESSAGE
                            );
                        }
                    }
                };

        worker.execute();
    }

    // ============================================================
    // TEXT TO SPEECH
    // ============================================================

    private void speakResponse(
            String text,
            AssistantLanguage language
    ) {

        statusLabel.setText(
                "Speaking reply..."
        );

        SwingWorker<Void, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Void doInBackground()
                            throws Exception {

                        voiceService.speak(
                                text,
                                language
                        );

                        return null;
                    }

                    @Override
                    protected void done() {

                        try {

                            get();

                            setBusy(
                                    false,
                                    "Ready"
                            );

                        } catch (Exception exception) {

                            setBusy(
                                    false,
                                    "Reply shown as text"
                            );
                        }
                    }
                };

        worker.execute();
    }

    // ============================================================
    // VOICE STATUS
    // ============================================================

    private void updateVoiceStatus() {

        AssistantLanguage language =
                getSelectedLanguage();

        if (
                voiceService
                        .isRecognitionLanguageAvailable(
                                language
                        )
        ) {

            statusLabel.setText(
                    "Voice ready"
            );

        } else {

            statusLabel.setText(
                    "Voice: Windows recognizer fallback available"
            );
        }
    }

    // ============================================================
    // GET SELECTED LANGUAGE
    // ============================================================

    private AssistantLanguage getSelectedLanguage() {

        AssistantLanguage selected =
                (AssistantLanguage)
                        languageBox
                                .getSelectedItem();

        return selected == null
                ? AssistantLanguage.HINGLISH
                : selected;
    }

    // ============================================================
    // CHAT MESSAGE
    // ============================================================

    private void appendAssistantMessage(
            String text
    ) {

        appendMessage(
                "Assistant",
                text,
                TEAL
        );
    }

    private void appendMessage(
            String author,
            String text,
            Color authorColor
    ) {

        StyledDocument document =
                conversationPane
                        .getStyledDocument();

        SimpleAttributeSet authorStyle =
                new SimpleAttributeSet();

        StyleConstants.setBold(
                authorStyle,
                true
        );

        StyleConstants.setForeground(
                authorStyle,
                authorColor
        );

        StyleConstants.setFontFamily(
                authorStyle,
                UNICODE_FONT
        );

        StyleConstants.setFontSize(
                authorStyle,
                14
        );

        SimpleAttributeSet textStyle =
                new SimpleAttributeSet();

        StyleConstants.setForeground(
                textStyle,
                NAVY
        );

        /*
         * MOST IMPORTANT FIX:
         * Text ko Segoe UI ki jagah Unicode font mein render karo.
         */
        StyleConstants.setFontFamily(
                textStyle,
                UNICODE_FONT
        );

        StyleConstants.setFontSize(
                textStyle,
                14
        );

        try {

            String time =
                    LocalTime.now()
                            .format(
                                    DateTimeFormatter
                                            .ofPattern(
                                                    "hh:mm a"
                                            )
                            );

            document.insertString(
                    document.getLength(),
                    author
                            + "  "
                            + time
                            + "\n",
                    authorStyle
            );

            document.insertString(
                    document.getLength(),
                    text.trim()
                            + "\n\n",
                    textStyle
            );

            conversationPane.setCaretPosition(
                    document.getLength()
            );

        } catch (BadLocationException ignored) {
        }
    }

    // ============================================================
    // BUSY STATE
    // ============================================================

    private void setBusy(
            boolean busy,
            String message
    ) {

        sendButton.setEnabled(!busy);

        microphoneButton.setEnabled(!busy);

        inputField.setEnabled(!busy);

        languageBox.setEnabled(!busy);

        statusLabel.setText(message);

        if (!busy) {

            inputField.requestFocusInWindow();
        }
    }

    // ============================================================
    // BUTTON STYLE
    // ============================================================

    private void styleButton(
            JButton button,
            Color background,
            Color foreground
    ) {

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        button.setBackground(background);

        button.setForeground(foreground);

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BLUE
                        ),
                        new EmptyBorder(
                                11,
                                15,
                                11,
                                15
                        )
                )
        );
    }

    // ============================================================
    // ERROR MESSAGE
    // ============================================================

    private String rootMessage(
            Throwable throwable
    ) {

        Throwable current =
                throwable;

        while (
                current.getCause() != null
        ) {

            current =
                    current.getCause();
        }

        return current.getMessage() == null
                ? "Unknown voice error"
                : current.getMessage();
    }
}