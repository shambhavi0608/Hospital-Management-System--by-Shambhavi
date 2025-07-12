package hospital.management.system.chatbot;

import hospital.management.system.dao.DashboardDAO;

/**
 * Smart HMS multilingual operational assistant.
 *
 * Live MySQL database se patient aur room statistics leta hai.
 * Medical diagnosis ya medicine prescribe nahi karta.
 */
public final class ChatbotService {

    private final IntentDetector intentDetector =
            new IntentDetector();

    private final DashboardDAO dashboardDAO =
            new DashboardDAO();

    public String generateResponse(
            String message
    ) {

        return generateResponse(
                message,
                AssistantLanguage.HINGLISH
        );
    }

    public String generateResponse(
            String message,
            AssistantLanguage language
    ) {

        AssistantLanguage selectedLanguage =
                language == null
                        ? AssistantLanguage.HINGLISH
                        : language;

        Intent intent =
                intentDetector.detectIntent(
                        message
                );

        try {

            return switch (intent) {

                case GREETING ->
                        getGreeting(
                                selectedLanguage
                        );

                case TOTAL_PATIENTS ->
                        getPatientResponse(
                                selectedLanguage,
                                dashboardDAO
                                        .getTotalPatients()
                        );

                case AVAILABLE_ROOMS ->
                        getAvailableRoomResponse(
                                selectedLanguage,
                                dashboardDAO
                                        .getAvailableRooms()
                        );

                case OCCUPIED_ROOMS ->
                        getOccupiedRoomResponse(
                                selectedLanguage,
                                dashboardDAO
                                        .getOccupiedRooms()
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
                                selectedLanguage
                        );
            };

        } catch (Exception exception) {

            return getDatabaseErrorResponse(
                    selectedLanguage,
                    rootMessage(exception)
            );
        }
    }

    private String getOccupancyResponse(
            AssistantLanguage language
    ) throws Exception {

        int availableRooms =
                dashboardDAO.getAvailableRooms();

        int occupiedRooms =
                dashboardDAO.getOccupiedRooms();

        int totalRooms =
                availableRooms
                        + occupiedRooms;

        double percentage =
                totalRooms == 0
                        ? 0
                        : occupiedRooms
                        * 100.0
                        / totalRooms;

        return switch (language) {

            case HINDI ->
                    String.format(
                            "कमरे %.1f%% भरे हुए हैं। उपलब्ध: %d, भरे हुए: %d।",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case HINGLISH ->
                    String.format(
                            "Current room occupancy %.1f%% hai. Available: %d, Occupied: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case TAMIL ->
                    String.format(
                            "தற்போதைய அறை பயன்பாடு %.1f%%. காலி அறைகள்: %d, பயன்படுத்தப்படும் அறைகள்: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case TELUGU ->
                    String.format(
                            "ప్రస్తుత గది ఆక్యుపెన్సీ %.1f%%. అందుబాటులో: %d, ఉపయోగంలో: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case KANNADA ->
                    String.format(
                            "ಪ್ರಸ್ತುತ ಕೊಠಡಿ ಬಳಕೆ %.1f%%. ಲಭ್ಯ: %d, ಬಳಕೆಯಲ್ಲಿರುವುದು: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case BENGALI ->
                    String.format(
                            "বর্তমান রুম ব্যবহারের হার %.1f%%। খালি: %d, ব্যবহৃত: %d।",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case MARATHI ->
                    String.format(
                            "सध्याची रूम ऑक्युपन्सी %.1f%% आहे. उपलब्ध: %d, भरलेल्या: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case GUJARATI ->
                    String.format(
                            "હાલની રૂમ ઓક્યુપન્સી %.1f%% છે. ઉપલબ્ધ: %d, ભરેલા: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case PUNJABI ->
                    String.format(
                            "ਮੌਜੂਦਾ ਕਮਰਾ ਵਰਤੋਂ %.1f%% ਹੈ। ਉਪਲਬਧ: %d, ਭਰੇ ਹੋਏ: %d।",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case MALAYALAM ->
                    String.format(
                            "നിലവിലെ മുറി ഉപയോഗം %.1f%% ആണ്. ലഭ്യം: %d, ഉപയോഗത്തിലുള്ളത്: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case ODIA ->
                    String.format(
                            "ବର୍ତ୍ତମାନ କୋଠରୀ ବ୍ୟବହାର %.1f%%। ଉପଲବ୍ଧ: %d, ବ୍ୟବହୃତ: %d।",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case ASSAMESE ->
                    String.format(
                            "বৰ্তমান কোঠা ব্যৱহাৰ %.1f%%। উপলব্ধ: %d, ব্যৱহৃত: %d।",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case URDU ->
                    String.format(
                            "موجودہ کمرہ استعمال %.1f%% ہے۔ دستیاب: %d، بھرے ہوئے: %d۔",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            case FRENCH ->
                    String.format(
                            "Le taux d’occupation est de %.1f%%. Disponibles : %d, occupées : %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );

            default ->
                    String.format(
                            "Current room occupancy is %.1f%%. Available: %d, Occupied: %d.",
                            percentage,
                            availableRooms,
                            occupiedRooms
                    );
        };
    }

    private String getPatientResponse(
            AssistantLanguage language,
            int value
    ) {

        return switch (language) {

            case HINDI ->
                    "अस्पताल में कुल "
                            + value
                            + " भर्ती मरीज हैं।";

            case HINGLISH ->
                    "Hospital mein total "
                            + value
                            + " admitted patient(s) hain.";

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

    private String getAvailableRoomResponse(
            AssistantLanguage language,
            int value
    ) {

        return switch (language) {

            case HINDI ->
                    "अभी "
                            + value
                            + " कमरे उपलब्ध हैं।";

            case HINGLISH ->
                    "Abhi "
                            + value
                            + " room(s) available hain.";

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

    private String getOccupiedRoomResponse(
            AssistantLanguage language,
            int value
    ) {

        return switch (language) {

            case HINDI ->
                    "अभी "
                            + value
                            + " कमरे भरे हुए हैं।";

            case HINGLISH ->
                    "Abhi "
                            + value
                            + " room(s) occupied hain.";

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

    private String getGreeting(
            AssistantLanguage language
    ) {

        return switch (language) {

            case HINDI ->
                    "नमस्ते! मैं Smart HMS सहायक हूँ। मरीज और कमरों की जानकारी पूछें।";

            case HINGLISH ->
                    "Namaste! Main Smart HMS Assistant hoon. Patient aur room information poochhiye.";

            case TAMIL ->
                    "வணக்கம்! நான் Smart HMS உதவியாளர். நோயாளிகள் மற்றும் அறைகள் பற்றி கேளுங்கள்.";

            case TELUGU ->
                    "నమస్కారం! నేను Smart HMS సహాయకుడిని. రోగులు మరియు గదుల గురించి అడగండి.";

            case KANNADA ->
                    "ನಮಸ್ಕಾರ! ನಾನು Smart HMS ಸಹಾಯಕ. ರೋಗಿಗಳು ಮತ್ತು ಕೊಠಡಿಗಳ ಬಗ್ಗೆ ಕೇಳಿ.";

            case BENGALI ->
                    "নমস্কার! আমি Smart HMS সহায়ক। রোগী ও রুম সম্পর্কে জিজ্ঞাসা করুন।";

            case MARATHI ->
                    "नमस्कार! मी Smart HMS सहाय्यक आहे. रुग्ण आणि खोल्यांची माहिती विचारा.";

            case GUJARATI ->
                    "નમસ્તે! હું Smart HMS સહાયક છું. દર્દી અને રૂમની માહિતી પૂછો.";

            case PUNJABI ->
                    "ਸਤ ਸ੍ਰੀ ਅਕਾਲ! ਮੈਂ Smart HMS ਸਹਾਇਕ ਹਾਂ। ਮਰੀਜ਼ ਅਤੇ ਕਮਰਿਆਂ ਬਾਰੇ ਪੁੱਛੋ।";

            case MALAYALAM ->
                    "നമസ്കാരം! ഞാൻ Smart HMS സഹായി ആണ്. രോഗികളെയും മുറികളെയും കുറിച്ച് ചോദിക്കൂ.";

            case ODIA ->
                    "ନମସ୍କାର! ମୁଁ Smart HMS ସହାୟକ। ରୋଗୀ ଓ କୋଠରୀ ବିଷୟରେ ପଚାରନ୍ତୁ।";

            case ASSAMESE ->
                    "নমস্কাৰ! মই Smart HMS সহায়ক। ৰোগী আৰু কোঠাৰ তথ্য সোধক।";

            case URDU ->
                    "السلام علیکم! میں Smart HMS اسسٹنٹ ہوں۔ مریضوں اور کمروں کی معلومات پوچھیں۔";

            case FRENCH ->
                    "Bonjour ! Je suis l’assistant Smart HMS. Posez une question sur les patients ou les chambres.";

            default ->
                    "Hello! I am the Smart HMS Assistant. Ask me about patients or rooms.";
        };
    }

    private String getHelpResponse(
            AssistantLanguage language
    ) {

        return switch (language) {

            case HINDI ->
                    """
                    आप पूछ सकते हैं:
                    • कुल मरीज कितने हैं?
                    • कितने कमरे उपलब्ध हैं?
                    • कितने कमरे भरे हुए हैं?
                    • कमरे कितने प्रतिशत भरे हैं?
                    """;

            case HINGLISH ->
                    """
                    Aap pooch sakte hain:
                    • Total patients kitne hain?
                    • Available rooms kitne hain?
                    • Occupied rooms kitne hain?
                    • Room occupancy kya hai?
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
                    """;
        };
    }

    private String getMedicalSafetyResponse(
            AssistantLanguage language
    ) {

        return switch (language) {

            case HINDI ->
                    "मैं चिकित्सा निदान या दवा नहीं बता सकता। कृपया योग्य डॉक्टर से संपर्क करें।";

            case HINGLISH ->
                    "Main diagnosis ya medicine prescribe nahi karta. Please qualified doctor se consult karein.";

            case TAMIL ->
                    "நான் நோயறிதல் அல்லது மருந்து பரிந்துரை செய்ய முடியாது. தகுதியான மருத்துவரை அணுகவும்.";

            case TELUGU ->
                    "నేను వ్యాధి నిర్ధారణ లేదా మందులు సూచించలేను. అర్హత కలిగిన వైద్యుడిని సంప్రదించండి.";

            case KANNADA ->
                    "ನಾನು ರೋಗನಿರ್ಣಯ ಅಥವಾ ಔಷಧಿ ಸೂಚಿಸಲು ಸಾಧ್ಯವಿಲ್ಲ. ಅರ್ಹ ವೈದ್ಯರನ್ನು ಸಂಪರ್ಕಿಸಿ.";

            case BENGALI ->
                    "আমি রোগ নির্ণয় বা ওষুধ দিতে পারি না। যোগ্য চিকিৎসকের সঙ্গে যোগাযোগ করুন।";

            case MARATHI ->
                    "मी निदान किंवा औषध सांगू शकत नाही. कृपया पात्र डॉक्टरांचा सल्ला घ्या.";

            case GUJARATI ->
                    "હું નિદાન અથવા દવા સૂચવી શકતો નથી. કૃપા કરીને યોગ્ય ડૉક્ટરનો સંપર્ક કરો.";

            case PUNJABI ->
                    "ਮੈਂ ਬਿਮਾਰੀ ਦੀ ਜਾਂਚ ਜਾਂ ਦਵਾਈ ਨਹੀਂ ਲਿਖ ਸਕਦਾ। ਯੋਗ ਡਾਕਟਰ ਨਾਲ ਸੰਪਰਕ ਕਰੋ।";

            case MALAYALAM ->
                    "എനിക്ക് രോഗനിർണയമോ മരുന്നോ നിർദ്ദേശിക്കാനാവില്ല. യോഗ്യനായ ഡോക്ടറെ സമീപിക്കുക.";

            case ODIA ->
                    "ମୁଁ ରୋଗ ନିର୍ଣ୍ଣୟ କିମ୍ବା ଔଷଧ ପରାମର୍ଶ ଦେଇପାରିବି ନାହିଁ। ଡାକ୍ତରଙ୍କୁ ଯୋଗାଯୋଗ କରନ୍ତୁ।";

            case ASSAMESE ->
                    "মই ৰোগ নিৰ্ণয় বা ঔষধ পৰামৰ্শ দিব নোৱাৰোঁ। যোগ্য চিকিৎসকৰ সৈতে যোগাযোগ কৰক।";

            case URDU ->
                    "میں بیماری کی تشخیص یا دوا تجویز نہیں کر سکتا۔ کسی مستند ڈاکٹر سے رابطہ کریں۔";

            case FRENCH ->
                    "Je ne peux pas établir de diagnostic ni prescrire de médicament. Consultez un médecin qualifié.";

            default ->
                    "I cannot diagnose conditions or prescribe medicine. Please consult a qualified doctor.";
        };
    }

    private String getUnknownResponse(
            AssistantLanguage language
    ) {

        return switch (language) {

            case HINDI ->
                    "मैं यह प्रश्न नहीं समझ पाया। उपलब्ध सवाल देखने के लिए Help लिखें।";

            case HINGLISH ->
                    "Main ye question samajh nahi saka. Available questions ke liye Help likhein.";

            case FRENCH ->
                    "Je n’ai pas compris cette question. Écrivez Help pour voir les questions disponibles.";

            default ->
                    "I could not understand that question. Type Help to see supported questions.";
        };
    }

    private String getDatabaseErrorResponse(
            AssistantLanguage language,
            String error
    ) {

        if (
                language == AssistantLanguage.HINDI
                        ||
                        language == AssistantLanguage.HINGLISH
        ) {

            return "Database information load nahi hui: "
                    + error;
        }

        return "Database information could not be loaded: "
                + error;
    }

    private String rootMessage(
            Throwable throwable
    ) {

        Throwable current = throwable;

        while (
                current.getCause()
                        != null
        ) {
            current = current.getCause();
        }

        return current.getMessage() == null
                ? "Unknown database error"
                : current.getMessage();
    }
}