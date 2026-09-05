package hospital.management.system.chatbot;

/**
 * Smart HMS Assistant mein supported languages.
 *
 * cultureCode Windows Speech Recognition aur
 * Text-to-Speech ke liye use hota hai.
 */
public enum AssistantLanguage {

    ENGLISH(
            "English",
            "en-IN"
    ),

    HINDI(
            "हिन्दी",
            "hi-IN"
    ),

    HINGLISH(
            "Hinglish",
            "en-IN"
    ),

    TAMIL(
            "தமிழ்",
            "ta-IN"
    ),

    TELUGU(
            "తెలుగు",
            "te-IN"
    ),

    KANNADA(
            "ಕನ್ನಡ",
            "kn-IN"
    ),

    BENGALI(
            "বাংলা",
            "bn-IN"
    ),

    MARATHI(
            "मराठी",
            "mr-IN"
    ),

    GUJARATI(
            "ગુજરાતી",
            "gu-IN"
    ),

    PUNJABI(
            "ਪੰਜਾਬੀ",
            "pa-IN"
    ),

    MALAYALAM(
            "മലയാളം",
            "ml-IN"
    ),

    ODIA(
            "ଓଡ଼ିଆ",
            "or-IN"
    ),

    ASSAMESE(
            "অসমীয়া",
            "as-IN"
    ),

    URDU(
            "اردو",
            "ur-IN"
    ),

    FRENCH(
            "Français",
            "fr-FR"
    );

    private final String displayName;
    private final String cultureCode;

    AssistantLanguage(
            String displayName,
            String cultureCode
    ) {
        this.displayName = displayName;
        this.cultureCode = cultureCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCultureCode() {
        return cultureCode;
    }

    /*
     * Backward compatibility ke liye.
     */
    public String getCulture() {
        return cultureCode;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * ComboBox ya saved text se language find karta hai.
     */
    public static AssistantLanguage fromDisplayName(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return HINGLISH;
        }

        String normalizedValue = value.trim();

        for (AssistantLanguage language : values()) {

            if (language.displayName.equalsIgnoreCase(
                    normalizedValue
            )
                    || language.name().equalsIgnoreCase(
                    normalizedValue
            )
                    || language.cultureCode.equalsIgnoreCase(
                    normalizedValue
            )) {

                return language;
            }
        }

        return HINGLISH;
    }
}