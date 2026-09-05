package hospital.management.system.chatbot;

import hospital.management.system.dao.DashboardDAO;

/**
 * Smart HMS multilingual operational assistant.
 *
 * Flow:
 *
 * User Input
 *      ↓
 * IntentDetector
 *      ↓
 * Intent
 *      ↓
 * DashboardDAO
 *      ↓
 * MySQL
 *      ↓
 * Response
 *
 * Medical diagnosis ya medicine prescribe nahi karta.
 */
public final class ChatbotService {

    private final IntentDetector intentDetector =
            new IntentDetector();

    private final DashboardDAO dashboardDAO =
            new DashboardDAO();

    /**
     * Default language = Hinglish.
     */
    public String generateResponse(String message) {

        return generateResponse(
                message,
                AssistantLanguage.HINGLISH
        );
    }

    /**
     * Main chatbot processing method.
     */
    public String generateResponse(
            String message,
            AssistantLanguage language
    ) {

        AssistantLanguage selectedLanguage =
                language == null
                        ? AssistantLanguage.HINGLISH
                        : language;

        /*
         * Empty input.
         */
        if (message == null || message.isBlank()) {
            return getEmptyMessageResponse(selectedLanguage);
        }

        /*
         * Step 1:
         * User ke message ka intent detect karo.
         */
        Intent intent =
                intentDetector.detectIntent(message);

        try {

            /*
             * Step 2:
             * Intent ke according response generate karo.
             */
            return switch (intent) {

                case GREETING ->
                        getGreeting(selectedLanguage);

                case TOTAL_PATIENTS ->
                        getPatientResponse(
                                selectedLanguage,
                                dashboardDAO.getTotalPatients()
                        );

                case AVAILABLE_ROOMS ->
                        getAvailableRoomResponse(
                                selectedLanguage,
                                dashboardDAO.getAvailableRooms()
                        );

                case OCCUPIED_ROOMS ->
                        getOccupiedRoomResponse(
                                selectedLanguage,
                                dashboardDAO.getOccupiedRooms()
                        );

                case ROOM_OCCUPANCY ->
                        getOccupancyResponse(
                                selectedLanguage
                        );

                case HELP ->
                        getHelpResponse(
                                selectedLanguage
                        );

                case MEDICAL_ADVICE ->
                        getMedicalSafetyResponse(
                                selectedLanguage
                        );

                case UNKNOWN ->
                        getUnknownResponse(
                                selectedLanguage,
                                message
                        );

                default ->
                        getUnknownResponse(
                                selectedLanguage,
                                message
                        );
            };

        } catch (Exception exception) {

            return getDatabaseErrorResponse(
                    selectedLanguage
            );
        }
    }

    // ============================================================
    // ROOM OCCUPANCY
    // ============================================================

    private String getOccupancyResponse(
            AssistantLanguage language
    ) throws Exception {

        int availableRooms =
                dashboardDAO.getAvailableRooms();

        int occupiedRooms =
                dashboardDAO.getOccupiedRooms();

        int totalRooms =
                availableRooms + occupiedRooms;

        double percentage;

        if (totalRooms == 0) {

            percentage = 0;

        } else {

            percentage =
                    occupiedRooms * 100.0 / totalRooms;
        }

        return switch (language) {

            case HINGLISH ->
                    String.format(
                            "Current room occupancy %.1f%% hai. "
                                    + "Available: %d, Occupied: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case HINDI ->
                    String.format(
                            "कमरे %.1f%% भरे हुए हैं। "
                                    + "उपलब्ध: %d, भरे हुए: %d।",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case TAMIL ->
                    String.format(
                            "தற்போதைய அறை பயன்பாடு %.1f%%. "
                                    + "காலி அறைகள்: %d, பயன்படுத்தப்படும் அறைகள்: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case TELUGU ->
                    String.format(
                            "ప్రస్తుత గది ఆక్యుపెన్సీ %.1f%%. "
                                    + "అందుబాటులో: %d, ఉపయోగంలో: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case KANNADA ->
                    String.format(
                            "ಪ್ರಸ್ತುತ ಕೊಠಡಿ ಬಳಕೆ %.1f%%. "
                                    + "ಲಭ್ಯ: %d, ಬಳಕೆಯಲ್ಲಿರುವುದು: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case BENGALI ->
                    String.format(
                            "বর্তমান রুম ব্যবহারের হার %.1f%%। "
                                    + "খালি: %d, ব্যবহৃত: %d।",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case MARATHI ->
                    String.format(
                            "सध्याची रूम ऑक्युपन्सी %.1f%% आहे. "
                                    + "उपलब्ध: %d, भरलेल्या: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case GUJARATI ->
                    String.format(
                            "હાલની રૂમ ઓક્યુપન્સી %.1f%% છે. "
                                    + "ઉપલબ્ધ: %d, ભરેલા: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case PUNJABI ->
                    String.format(
                            "ਮੌਜੂਦਾ ਕਮਰਾ ਵਰਤੋਂ %.1f%% ਹੈ। "
                                    + "ਉਪਲਬਧ: %d, ਭਰੇ ਹੋਏ: %d।",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case MALAYALAM ->
                    String.format(
                            "നിലവിലെ മുറി ഉപയോഗം %.1f%% ആണ്. "
                                    + "ലഭ്യം: %d, ഉപയോഗത്തിലുള്ളത്: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case ODIA ->
                    String.format(
                            "ବର୍ତ୍ତମାନ କୋଠରୀ ବ୍ୟବହାର %.1f%%। "
                                    + "ଉପଲବ୍ଧ: %d, ବ୍ୟବହୃତ: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case ASSAMESE ->
                    String.format(
                            "বৰ্তমান কোঠা ব্যৱহাৰ %.1f%%। "
                                    + "উপলব্ধ: %d, ব্যৱহৃত: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case URDU ->
                    String.format(
                            "موجودہ کمرہ استعمال %.1f%% ہے۔ "
                                    + "دستیاب: %d، بھرے ہوئے: %d۔",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case FRENCH ->
                    String.format(
                            "Le taux d’occupation est de %.1f%%. "
                                    + "Disponibles : %d, occupées : %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            default ->
                    String.format(
                            "Current room occupancy is %.1f%%. "
                                    + "Available: %d, Occupied: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );
        };
    }

    // ============================================================
    // TOTAL PATIENTS
    // ============================================================

    private String getPatientResponse(
            AssistantLanguage language,
            int value
    ) {

        return switch (language) {

            case HINGLISH ->
                    "Hospital mein total "
                            + value
                            + " admitted patient(s) hain.";

            case HINDI ->
                    "अस्पताल में कुल "
                            + value
                            + " भर्ती मरीज हैं।";

            case TAMIL ->
                    "மருத்துவமனையில் மொத்தம் "
                            + value
                            + " நோயாளிகள் அனுமதிக்கப்பட்டுள்ளனர்.";

            case TELUGU ->
                    "ఆసుపత్రిలో మొత్తం "
                            + value
                            + " మంది రోగులు చేరారు.";

            case KANNADA ->
                    "ಆಸ್ಪತ್ರೆಯಲ್ಲಿ ಒಟ್ಟು "
                            + value
                            + " ರೋಗಿಗಳು ದಾಖಲಾಗಿದ್ದಾರೆ.";

            case BENGALI ->
                    "হাসপাতালে মোট "
                            + value
                            + " জন রোগী ভর্তি আছেন।";

            case MARATHI ->
                    "रुग्णालयात एकूण "
                            + value
                            + " रुग्ण दाखल आहेत.";

            case GUJARATI ->
                    "હોસ્પિટલમાં કુલ "
                            + value
                            + " દર્દીઓ દાખલ છે.";

            case PUNJABI ->
                    "ਹਸਪਤਾਲ ਵਿੱਚ ਕੁੱਲ "
                            + value
                            + " ਮਰੀਜ਼ ਦਾਖਲ ਹਨ।";

            case MALAYALAM ->
                    "ആശുപത്രിയിൽ ആകെ "
                            + value
                            + " രോഗികൾ പ്രവേശിപ്പിച്ചിട്ടുണ്ട്.";

            case ODIA ->
                    "ଡାକ୍ତରଖାନାରେ ମୋଟ "
                            + value
                            + " ଜଣ ରୋଗୀ ଭର୍ତ୍ତି ଅଛନ୍ତି।";

            case ASSAMESE ->
                    "হাস্পতালত মুঠ "
                            + value
                            + " জন ৰোগী ভৰ্তি আছে।";

            case URDU ->
                    "ہسپتال میں کل "
                            + value
                            + " مریض داخل ہیں۔";

            case FRENCH ->
                    "Il y a "
                            + value
                            + " patients admis.";

            default ->
                    "There are "
                            + value
                            + " admitted patient(s).";
        };
    }

    // ============================================================
    // AVAILABLE ROOMS
    // ============================================================

    private String getAvailableRoomResponse(
            AssistantLanguage language,
            int value
    ) {

        return switch (language) {

            case HINGLISH ->
                    "Abhi "
                            + value
                            + " room(s) available hain.";

            case HINDI ->
                    "अभी "
                            + value
                            + " कमरे उपलब्ध हैं।";

            case TAMIL ->
                    "தற்போது "
                            + value
                            + " அறைகள் காலியாக உள்ளன.";

            case TELUGU ->
                    "ప్రస్తుతం "
                            + value
                            + " గదులు అందుబాటులో ఉన్నాయి.";

            case KANNADA ->
                    "ಈಗ "
                            + value
                            + " ಕೊಠಡಿಗಳು ಲಭ್ಯವಿವೆ.";

            case BENGALI ->
                    "বর্তমানে "
                            + value
                            + "টি রুম খালি আছে।";

            case MARATHI ->
                    "सध्या "
                            + value
                            + " खोल्या उपलब्ध आहेत.";

            case GUJARATI ->
                    "હાલમાં "
                            + value
                            + " રૂમ ઉપલબ્ધ છે.";

            case PUNJABI ->
                    "ਇਸ ਵੇਲੇ "
                            + value
                            + " ਕਮਰੇ ਉਪਲਬਧ ਹਨ।";

            case MALAYALAM ->
                    "നിലവിൽ "
                            + value
                            + " മുറികൾ ലഭ്യമാണ്.";

            case ODIA ->
                    "ବର୍ତ୍ତମାନ "
                            + value
                            + "ଟି କୋଠରୀ ଉପଲବ୍ଧ ଅଛି।";

            case ASSAMESE ->
                    "বৰ্তমান "
                            + value
                            + "টা কোঠা উপলব্ধ আছে।";

            case URDU ->
                    "اس وقت "
                            + value
                            + " کمرے دستیاب ہیں۔";

            case FRENCH ->
                    value
                            + " chambres sont disponibles.";

            default ->
                    value
                            + " room(s) are currently available.";
        };
    }

    // ============================================================
    // OCCUPIED ROOMS
    // ============================================================

    private String getOccupiedRoomResponse(
            AssistantLanguage language,
            int value
    ) {

        return switch (language) {

            case HINGLISH ->
                    "Abhi "
                            + value
                            + " room(s) occupied hain.";

            case HINDI ->
                    "अभी "
                            + value
                            + " कमरे भरे हुए हैं।";

            case TAMIL ->
                    "தற்போது "
                            + value
                            + " அறைகள் பயன்படுத்தப்படுகின்றன.";

            case TELUGU ->
                    "ప్రస్తుతం "
                            + value
                            + " గదులు ఉపయోగంలో ఉన్నాయి.";

            case KANNADA ->
                    "ಈಗ "
                            + value
                            + " ಕೊಠಡಿಗಳು ಬಳಕೆಯಲ್ಲಿವೆ.";

            case BENGALI ->
                    "বর্তমানে "
                            + value
                            + "টি রুম ব্যবহৃত হচ্ছে।";

            case MARATHI ->
                    "सध्या "
                            + value
                            + " खोल्या भरलेल्या आहेत.";

            case GUJARATI ->
                    "હાલમાં "
                            + value
                            + " રૂમ ભરેલા છે.";

            case PUNJABI ->
                    "ਇਸ ਵੇਲੇ "
                            + value
                            + " ਕਮਰੇ ਭਰੇ ਹੋਏ ਹਨ।";

            case MALAYALAM ->
                    "നിലവിൽ "
                            + value
                            + " മുറികൾ ഉപയോഗത്തിലാണ്.";

            case ODIA ->
                    "ବର୍ତ୍ତମାନ "
                            + value
                            + "ଟି କୋଠରୀ ବ୍ୟବହୃତ ହେଉଛି।";

            case ASSAMESE ->
                    "বৰ্তমান "
                            + value
                            + "টা কোঠা ব্যৱহৃত হৈ আছে।";

            case URDU ->
                    "اس وقت "
                            + value
                            + " کمرے بھرے ہوئے ہیں۔";

            case FRENCH ->
                    value
                            + " chambres sont occupées.";

            default ->
                    value
                            + " room(s) are currently occupied.";
        };
    }

    // ============================================================
    // GREETING
    // ============================================================

    private String getGreeting(
            AssistantLanguage language
    ) {

        return switch (language) {

            case HINGLISH ->
                    "Namaste! Main Smart HMS Assistant hoon. "
                            + "Aap patients, rooms aur occupancy ke "
                            + "baare mein pooch sakte hain. "
                            + "Agar help chahiye to Help likhiye.";

            case HINDI ->
                    "नमस्ते! मैं Smart HMS Assistant हूँ। "
                            + "आप मरीजों और कमरों की जानकारी पूछ सकते हैं।";

            case TAMIL ->
                    "வணக்கம்! நான் Smart HMS உதவியாளர். "
                            + "நோயாளிகள் மற்றும் அறைகள் பற்றி கேட்கலாம்.";

            case TELUGU ->
                    "నమస్కారం! నేను Smart HMS సహాయకుడిని. "
                            + "రోగులు మరియు గదుల గురించి అడగవచ్చు.";

            case KANNADA ->
                    "ನಮಸ್ಕಾರ! ನಾನು Smart HMS ಸಹಾಯಕ. "
                            + "ರೋಗಿಗಳು ಮತ್ತು ಕೊಠಡಿಗಳ ಬಗ್ಗೆ ಕೇಳಬಹುದು.";

            case BENGALI ->
                    "নমস্কার! আমি Smart HMS সহায়ক। "
                            + "রোগী এবং রুম সম্পর্কে জানতে পারেন।";

            case MARATHI ->
                    "नमस्कार! मी Smart HMS सहाय्यक आहे. "
                            + "रुग्ण आणि खोल्यांबद्दल माहिती विचारू शकता.";

            case GUJARATI ->
                    "નમસ્તે! હું Smart HMS સહાયક છું. "
                            + "દર્દીઓ અને રૂમ વિશે પૂછી શકો છો.";

            case PUNJABI ->
                    "ਸਤ ਸ੍ਰੀ ਅਕਾਲ! ਮੈਂ Smart HMS ਸਹਾਇਕ ਹਾਂ। "
                            + "ਮਰੀਜ਼ਾਂ ਅਤੇ ਕਮਰਿਆਂ ਬਾਰੇ ਪੁੱਛ ਸਕਦੇ ਹੋ.";

            case MALAYALAM ->
                    "നമസ്കാരം! ഞാൻ Smart HMS സഹായി ആണ്. "
                            + "രോഗികളെയും മുറികളെയും കുറിച്ച് ചോദിക്കാം.";

            case ODIA ->
                    "ନମସ୍କାର! ମୁଁ Smart HMS ସହାୟକ। "
                            + "ରୋଗୀ ଏବଂ କୋଠରୀ ବିଷୟରେ ପଚାରିପାରିବେ।";

            case ASSAMESE ->
                    "নমস্কাৰ! মই Smart HMS সহায়ক। "
                            + "ৰোগী আৰু কোঠাৰ বিষয়ে সুধিব পাৰে.";

            case URDU ->
                    "السلام علیکم! میں Smart HMS اسسٹنٹ ہوں۔ "
                            + "مریضوں اور کمروں کے بارے میں پوچھ سکتے ہیں.";

            case FRENCH ->
                    "Bonjour ! Je suis l’assistant Smart HMS. "
                            + "Vous pouvez poser des questions sur les patients et les chambres.";

            default ->
                    "Hello! I am the Smart HMS Assistant. "
                            + "You can ask me about patients, rooms and occupancy.";
        };
    }

    // ============================================================
    // HELP
    // ============================================================

    private String getHelpResponse(
            AssistantLanguage language
    ) {

        return switch (language) {

            case HINGLISH ->
                    """
                    Bilkul! Aap mujhse ye questions pooch sakte hain:

                    • Hospital mein kitne patients hain?
                    • Kitne rooms available hain?
                    • Kitne rooms occupied hain?
                    • Room occupancy kitni hai?
                    • What can you do?
                    • Help

                    Examples:

                    "Kitne patients hain?"
                    "Koi room khaali hai?"
                    "How many rooms are occupied?"
                    """;

            case HINDI ->
                    """
                    आप मुझसे पूछ सकते हैं:

                    • कुल मरीज कितने हैं?
                    • कितने कमरे उपलब्ध हैं?
                    • कितने कमरे भरे हुए हैं?
                    • कमरे कितने प्रतिशत भरे हैं?
                    • What can you do?
                    • Help
                    """;

            case TAMIL ->
                    """
                    நீங்கள் கேட்கலாம்:

                    • மொத்த நோயாளிகள் எத்தனை?
                    • எத்தனை அறைகள் கிடைக்கின்றன?
                    • எத்தனை அறைகள் பயன்படுத்தப்படுகின்றன?
                    • அறை பயன்பாடு எவ்வளவு?
                    """;

            case TELUGU ->
                    """
                    మీరు అడగవచ్చు:

                    • మొత్తం రోగులు ఎంత మంది?
                    • ఎన్ని గదులు అందుబాటులో ఉన్నాయి?
                    • ఎన్ని గదులు ఉపయోగంలో ఉన్నాయి?
                    • గది ఆక్యుపెన్సీ ఎంత?
                    """;

            case KANNADA ->
                    """
                    ನೀವು ಕೇಳಬಹುದು:

                    • ಒಟ್ಟು ರೋಗಿಗಳು ಎಷ್ಟು?
                    • ಎಷ್ಟು ಕೊಠಡಿಗಳು ಲಭ್ಯವಿವೆ?
                    • ಎಷ್ಟು ಕೊಠಡಿಗಳು ಬಳಕೆಯಲ್ಲಿವೆ?
                    • ಕೊಠಡಿ ಬಳಕೆ ಎಷ್ಟು?
                    """;

            case BENGALI ->
                    """
                    আপনি জিজ্ঞাসা করতে পারেন:

                    • মোট রোগী কতজন?
                    • কতগুলি রুম খালি আছে?
                    • কতগুলি রুম ব্যবহৃত হচ্ছে?
                    • রুম ব্যবহারের হার কত?
                    """;

            case MARATHI ->
                    """
                    तुम्ही विचारू शकता:

                    • एकूण रुग्ण किती आहेत?
                    • किती खोल्या उपलब्ध आहेत?
                    • किती खोल्या भरलेल्या आहेत?
                    • रूम ऑक्युपन्सी किती आहे?
                    """;

            case GUJARATI ->
                    """
                    તમે પૂછી શકો છો:

                    • કુલ દર્દીઓ કેટલા છે?
                    • કેટલા રૂમ ઉપલબ્ધ છે?
                    • કેટલા રૂમ ભરેલા છે?
                    • રૂમ ઓક્યુપન્સી કેટલી છે?
                    """;

            case PUNJABI ->
                    """
                    ਤੁਸੀਂ ਪੁੱਛ ਸਕਦੇ ਹੋ:

                    • ਕੁੱਲ ਮਰੀਜ਼ ਕਿੰਨੇ ਹਨ?
                    • ਕਿੰਨੇ ਕਮਰੇ ਉਪਲਬਧ ਹਨ?
                    • ਕਿੰਨੇ ਕਮਰੇ ਭਰੇ ਹੋਏ ਹਨ?
                    • ਕਮਰਾ ਵਰਤੋਂ ਕਿੰਨੀ ਹੈ?
                    """;

            case MALAYALAM ->
                    """
                    നിങ്ങൾക്ക് ചോദിക്കാം:

                    • ആകെ രോഗികൾ എത്ര?
                    • എത്ര മുറികൾ ലഭ്യമാണ്?
                    • എത്ര മുറികൾ ഉപയോഗത്തിലാണ്?
                    • മുറി ഉപയോഗം എത്രയാണ്?
                    """;

            case ODIA ->
                    """
                    ଆପଣ ପଚାରିପାରିବେ:

                    • ମୋଟ ରୋଗୀ କେତେ?
                    • କେତେଟି କୋଠରୀ ଉପଲବ୍ଧ?
                    • କେତେଟି କୋଠରୀ ବ୍ୟବହୃତ?
                    • କୋଠରୀ ବ୍ୟବହାର କେତେ?
                    """;

            case ASSAMESE ->
                    """
                    আপুনি সুধিব পাৰে:

                    • মুঠ ৰোগী কিমান?
                    • কিমানটা কোঠা উপলব্ধ?
                    • কিমানটা কোঠা ব্যৱহৃত?
                    • কোঠাৰ ব্যৱহাৰ কিমান?
                    """;

            case URDU ->
                    """
                    آپ پوچھ سکتے ہیں:

                    • کل مریض کتنے ہیں؟
                    • کتنے کمرے دستیاب ہیں؟
                    • کتنے کمرے بھرے ہوئے ہیں؟
                    • کمرے کا استعمال کتنے فیصد ہے؟
                    """;

            case FRENCH ->
                    """
                    Vous pouvez demander :

                    • Combien de patients sont admis ?
                    • Combien de chambres sont disponibles ?
                    • Combien de chambres sont occupées ?
                    • Quel est le taux d’occupation ?
                    """;

            default ->
                    """
                    You can ask:

                    • How many patients are admitted?
                    • How many rooms are available?
                    • How many rooms are occupied?
                    • What is the room occupancy?
                    • Help
                    """;
        };
    }

    // ============================================================
    // MEDICAL SAFETY
    // ============================================================

    private String getMedicalSafetyResponse(
            AssistantLanguage language
    ) {

        return switch (language) {

            case HINGLISH ->
                    "Main diagnosis ya medicine prescribe nahi karta. "
                            + "Medical concern ke liye qualified doctor se consult karein.";

            case HINDI ->
                    "मैं चिकित्सा निदान या दवा prescribe नहीं कर सकता। "
                            + "कृपया योग्य डॉक्टर से संपर्क करें।";

            case TAMIL ->
                    "நான் நோயறிதல் அல்லது மருந்து பரிந்துரை செய்ய முடியாது. "
                            + "தகுதியான மருத்துவரை அணுகவும்.";

            case TELUGU ->
                    "నేను వ్యాధి నిర్ధారణ లేదా మందులు సూచించలేను. "
                            + "అర్హత కలిగిన వైద్యుడిని సంప్రదించండి.";

            case KANNADA ->
                    "ನಾನು ರೋಗನಿರ್ಣಯ ಅಥವಾ ಔಷಧಿ ಸೂಚಿಸಲು ಸಾಧ್ಯವಿಲ್ಲ. "
                            + "ಅರ್ಹ ವೈದ್ಯರನ್ನು ಸಂಪರ್ಕಿಸಿ.";

            case BENGALI ->
                    "আমি রোগ নির্ণয় বা ওষুধ দিতে পারি না। "
                            + "যোগ্য চিকিৎসকের সঙ্গে যোগাযোগ করুন।";

            case MARATHI ->
                    "मी निदान किंवा औषध सांगू शकत नाही. "
                            + "कृपया पात्र डॉक्टरांचा सल्ला घ्या.";

            case GUJARATI ->
                    "હું નિદાન અથવા દવા સૂચવી શકતો નથી. "
                            + "કૃપા કરીને યોગ્ય ડૉક્ટરનો સંપર્ક કરો.";

            case PUNJABI ->
                    "ਮੈਂ ਬਿਮਾਰੀ ਦੀ ਜਾਂਚ ਜਾਂ ਦਵਾਈ ਨਹੀਂ ਲਿਖ ਸਕਦਾ। "
                            + "ਯੋਗ ਡਾਕਟਰ ਨਾਲ ਸੰਪਰਕ ਕਰੋ।";

            case MALAYALAM ->
                    "എനിക്ക് രോഗനിർണയമോ മരുന്നോ നിർദ്ദേശിക്കാനാവില്ല. "
                            + "യോഗ്യനായ ഡോക്ടറെ സമീപിക്കുക.";

            case ODIA ->
                    "ମୁଁ ରୋଗ ନିର୍ଣ୍ଣୟ କିମ୍ବା ଔଷଧ ପରାମର୍ଶ ଦେଇପାରିବି ନାହିଁ। "
                            + "ଡାକ୍ତରଙ୍କୁ ଯୋଗାଯୋଗ କରନ୍ତୁ।";

            case ASSAMESE ->
                    "মই ৰোগ নিৰ্ণয় বা ঔষধ পৰামৰ্শ দিব নোৱাৰোঁ। "
                            + "যোগ্য চিকিৎসকৰ সৈতে যোগাযোগ কৰক।";

            case URDU ->
                    "میں بیماری کی تشخیص یا دوا تجویز نہیں کر سکتا۔ "
                            + "کسی مستند ڈاکٹر سے رابطہ کریں۔";

            case FRENCH ->
                    "Je ne peux pas établir de diagnostic ni prescrire de médicament. "
                            + "Consultez un médecin qualifié.";

            default ->
                    "I cannot diagnose conditions or prescribe medicine. "
                            + "Please consult a qualified doctor.";
        };
    }

    // ============================================================
    // UNKNOWN / CONVERSATIONAL
    // ============================================================

    private String getUnknownResponse(
            AssistantLanguage language,
            String originalMessage
    ) {

        String message =
                originalMessage
                        .toLowerCase()
                        .trim();

        /*
         * How are you?
         */
        if (containsAny(
                message,
                "how are you",
                "how r u",
                "kaise ho",
                "kaisi ho",
                "kaisa hai"
        )) {

            return "Main bilkul ready hoon 😄 "
                    + "Aap hospital patients aur rooms ke "
                    + "baare mein pooch sakte ho.";
        }

        /*
         * Thank you.
         */
        if (containsAny(
                message,
                "thank you",
                "thanks",
                "thank u",
                "shukriya",
                "dhanyawad"
        )) {

            return "You're welcome! 😊 "
                    + "Agar hospital statistics chahiye ho "
                    + "to pooch lena.";
        }

        /*
         * Who are you?
         */
        if (containsAny(
                message,
                "who are you",
                "what are you",
                "tum kaun ho",
                "aap kaun ho",
                "what is your name",
                "tumhara naam kya hai"
        )) {

            return "Main Smart HMS Assistant hoon. "
                    + "Main hospital ke operational information "
                    + "jaise patients, rooms aur occupancy ke "
                    + "live statistics mein help karta hoon.";
        }

        /*
         * Okay / casual confirmation.
         */
        if (containsAny(
                message,
                "okay",
                "ok",
                "acha",
                "achha",
                "theek hai",
                "thik hai",
                "alright"
        )) {

            return "Okay 😊 Main ready hoon. "
                    + "Aap jo hospital information chahte hain, "
                    + "pooch sakte hain.";
        }

        /*
         * Default unknown response.
         */
        return switch (language) {

            case HINGLISH ->
                    "Hmm, main is question ko abhi properly "
                            + "understand nahi kar paaya. "
                            + "Aap thoda simple way mein pooch sakte ho, "
                            + "ya Help likh do.";

            case HINDI ->
                    "Main is question ko abhi support nahi karta. "
                            + "Aap Help likhkar supported questions "
                            + "dekh sakte hain.";

            case TAMIL ->
                    "இந்த கேள்வியை நான் தற்போது புரிந்துகொள்ளவில்லை. "
                            + "ஆதரிக்கப்படும் கேள்விகளுக்கு Help என்று கேளுங்கள்.";

            case TELUGU ->
                    "ఈ ప్రశ్నను నేను ప్రస్తుతం అర్థం చేసుకోలేకపోయాను. "
                            + "సహాయానికి Help అని అడగండి.";

            case KANNADA ->
                    "ಈ ಪ್ರಶ್ನೆಯನ್ನು ನಾನು ಈಗ ಅರ್ಥಮಾಡಿಕೊಳ್ಳಲಿಲ್ಲ. "
                            + "ಸಹಾಯಕ್ಕಾಗಿ Help ಎಂದು ಕೇಳಿ.";

            case BENGALI ->
                    "আমি এই প্রশ্নটি বুঝতে পারিনি। "
                            + "সহায়তার জন্য Help লিখুন।";

            case MARATHI ->
                    "मला हा प्रश्न समजला नाही. "
                            + "मदतीसाठी Help लिहा.";

            case GUJARATI ->
                    "હું આ પ્રશ્ન સમજી શક્યો નથી. "
                            + "મદદ માટે Help લખો.";

            case PUNJABI ->
                    "ਮੈਂ ਇਹ ਸਵਾਲ ਸਮਝ ਨਹੀਂ ਸਕਿਆ। "
                            + "ਮਦਦ ਲਈ Help ਲਿਖੋ.";

            case MALAYALAM ->
                    "ഈ ചോദ്യം എനിക്ക് മനസ്സിലായില്ല. "
                            + "സഹായത്തിനായി Help ചോദിക്കുക.";

            case ODIA ->
                    "ମୁଁ ଏହି ପ୍ରଶ୍ନ ବୁଝିପାରିଲି ନାହିଁ। "
                            + "ସହାୟତା ପାଇଁ Help ଲେଖନ୍ତୁ.";

            case ASSAMESE ->
                    "মই এই প্ৰশ্নটো বুজিব পৰা নাই। "
                            + "সহায়তাৰ বাবে Help লিখক.";

            case URDU ->
                    "میں اس سوال کو سمجھ نہیں سکا۔ "
                            + "مدد کے لیے Help لکھیں.";

            case FRENCH ->
                    "Je n’ai pas compris cette question. "
                            + "Écrivez Help pour voir les questions prises en charge.";

            default ->
                    "I could not understand that question. "
                            + "Type Help to see supported questions.";
        };
    }

    // ============================================================
    // EMPTY MESSAGE
    // ============================================================

    private String getEmptyMessageResponse(
            AssistantLanguage language
    ) {

        return switch (language) {

            case HINGLISH ->
                    "Kuch type ya bolkar poochhiye 😊";

            case HINDI ->
                    "कृपया अपना सवाल लिखें।";

            case TAMIL ->
                    "தயவுசெய்து உங்கள் கேள்வியை கேளுங்கள்.";

            case TELUGU ->
                    "దయచేసి మీ ప్రశ్న అడగండి.";

            case KANNADA ->
                    "ದಯವಿಟ್ಟು ನಿಮ್ಮ ಪ್ರಶ್ನೆಯನ್ನು ಕೇಳಿ.";

            case BENGALI ->
                    "অনুগ্রহ করে আপনার প্রশ্ন করুন।";

            case MARATHI ->
                    "कृपया तुमचा प्रश्न विचारा.";

            case GUJARATI ->
                    "કૃપા કરીને તમારો પ્રશ્ન પૂછો.";

            case PUNJABI ->
                    "ਕਿਰਪਾ ਕਰਕੇ ਆਪਣਾ ਸਵਾਲ ਪੁੱਛੋ.";

            case MALAYALAM ->
                    "ദയവായി നിങ്ങളുടെ ചോദ്യം ചോദിക്കുക.";

            case ODIA ->
                    "ଦୟାକରି ଆପଣଙ୍କ ପ୍ରଶ୍ନ ପଚାରନ୍ତୁ.";

            case ASSAMESE ->
                    "অনুগ্ৰহ কৰি আপোনাৰ প্ৰশ্ন সোধক.";

            case URDU ->
                    "براہ کرم اپنا سوال پوچھیں.";

            case FRENCH ->
                    "Veuillez poser votre question.";

            default ->
                    "Please ask a question.";
        };
    }

    // ============================================================
    // DATABASE ERROR
    // ============================================================

    private String getDatabaseErrorResponse(
            AssistantLanguage language
    ) {

        if (language == AssistantLanguage.HINDI
                || language == AssistantLanguage.HINGLISH) {

            return "Database information load nahi hui. "
                    + "Please database connection check karein.";
        }

        return "Database information could not be loaded.";
    }

    // ============================================================
    // HELPER
    // ============================================================

    private boolean containsAny(
            String message,
            String... keywords
    ) {

        for (String keyword : keywords) {

            if (keyword != null
                    && message.contains(
                    keyword.toLowerCase()
            )) {

                return true;
            }
        }

        return false;
    }
}