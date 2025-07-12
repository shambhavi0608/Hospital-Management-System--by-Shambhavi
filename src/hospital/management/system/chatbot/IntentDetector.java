package hospital.management.system.chatbot;

/**
 * English, Hinglish aur Indian-language commands ka
 * lightweight rule-based intent detector.
 */
public final class IntentDetector {

    public Intent detectIntent(
            String userMessage
    ) {

        if (
                userMessage == null
                        ||
                        userMessage.isBlank()
        ) {
            return Intent.UNKNOWN;
        }

        String message =
                userMessage
                        .toLowerCase()
                        .trim();

        if (
                containsAny(
                        message,
                        "medicine",
                        "dawai",
                        "दवाई",
                        "दवा",
                        "treatment",
                        "diagnosis",
                        "symptom",
                        "prescription",
                        "tablet",
                        "மருந்து",
                        "మందు",
                        "ಔಷಧಿ",
                        "ওষুধ",
                        "औषध",
                        "દવા",
                        "ਦਵਾਈ",
                        "മരുന്ന്",
                        "ଔଷଧ",
                        "ঔষধ",
                        "دوا"
                )
        ) {
            return Intent.MEDICAL_ADVICE;
        }

        if (
                isPatientQuestion(message)
        ) {
            return Intent.TOTAL_PATIENTS;
        }

        if (
                isAvailableRoomQuestion(message)
        ) {
            return Intent.AVAILABLE_ROOMS;
        }

        if (
                isOccupiedRoomQuestion(message)
        ) {
            return Intent.OCCUPIED_ROOMS;
        }

        if (
                containsAny(
                        message,
                        "occupancy",
                        "room utilization",
                        "room usage",
                        "percentage occupied",
                        "कितने प्रतिशत",
                        "room kitna bhara",
                        "அறை பயன்பாடு",
                        "గది ఆక్యుపెన్సీ",
                        "ಕೊಠಡಿ ಬಳಕೆ",
                        "রুম ব্যবহার",
                        "खोली वापर",
                        "રૂમ ઓક્યુપન્સી",
                        "ਕਮਰਾ ਵਰਤੋਂ",
                        "മുറി ഉപയോഗം",
                        "کمرہ استعمال"
                )
        ) {
            return Intent.ROOM_OCCUPANCY;
        }

        if (
                containsAny(
                        message,
                        "help",
                        "commands",
                        "what can you do",
                        "kya kar sakte",
                        "kya pooch sakte",
                        "मदद",
                        "सहायता",
                        "உதவி",
                        "సహాయం",
                        "ಸಹಾಯ",
                        "সাহায্য",
                        "मदत",
                        "મદદ",
                        "ਮਦਦ",
                        "സഹായം",
                        "ସାହାଯ୍ୟ",
                        "مدد",
                        "aide"
                )
        ) {
            return Intent.HELP;
        }

        if (
                containsAny(
                        message,
                        "hello",
                        "hi",
                        "hey",
                        "namaste",
                        "नमस्ते",
                        "नमस्कार",
                        "வணக்கம்",
                        "నమస్కారం",
                        "ನಮಸ್ಕಾರ",
                        "নমস্কার",
                        "ਸਤ ਸ੍ਰੀ ਅਕਾਲ",
                        "നമസ്കാരം",
                        "ନମସ୍କାର",
                        "السلام علیکم",
                        "bonjour"
                )
        ) {
            return Intent.GREETING;
        }

        return Intent.UNKNOWN;
    }

    private boolean isPatientQuestion(
            String message
    ) {

        return containsAny(
                message,
                "total patient",
                "patient count",
                "number of patient",
                "how many patient",
                "kitne patient",
                "patients kitne",
                "कितने मरीज",
                "कुल मरीज",
                "मरीजों की संख्या",
                "நோயாளிகள் எத்தனை",
                "மொத்த நோயாளி",
                "ఎంత మంది రోగులు",
                "మొత్తం రోగులు",
                "ಎಷ್ಟು ರೋಗಿಗಳು",
                "ಒಟ್ಟು ರೋಗಿಗಳು",
                "কত রোগী",
                "মোট রোগী",
                "किती रुग्ण",
                "एकूण रुग्ण",
                "કેટલા દર્દી",
                "કુલ દર્દી",
                "ਕਿੰਨੇ ਮਰੀਜ਼",
                "ਕੁੱਲ ਮਰੀਜ਼",
                "എത്ര രോഗികൾ",
                "ആകെ രോഗികൾ",
                "କେତେ ରୋଗୀ",
                "মুঠ ৰোগী",
                "کتنے مریض",
                "کل مریض",
                "combien de patients"
        );
    }

    private boolean isAvailableRoomQuestion(
            String message
    ) {

        return containsAny(
                message,
                "available room",
                "free room",
                "empty room",
                "room available",
                "khali room",
                "rooms khali",
                "खाली कमरे",
                "कमरे उपलब्ध",
                "कितने कमरे खाली",
                "காலி அறை",
                "அறைகள் கிடைக்குமா",
                "ఖాళీ గది",
                "అందుబాటులో గదులు",
                "ಖಾಲಿ ಕೊಠಡಿ",
                "ಲಭ್ಯ ಕೊಠಡಿ",
                "খালি রুম",
                "উপলব্ধ রুম",
                "रिकाम्या खोल्या",
                "उपलब्ध खोल्या",
                "ખાલી રૂમ",
                "ઉપલબ્ધ રૂમ",
                "ਖਾਲੀ ਕਮਰੇ",
                "ਉਪਲਬਧ ਕਮਰੇ",
                "ഒഴിഞ്ഞ മുറി",
                "ലഭ്യമായ മുറി",
                "ଖାଲି କୋଠରୀ",
                "উপলব্ধ কোঠা",
                "خالی کمرے",
                "دستیاب کمرے",
                "chambres disponibles"
        );
    }

    private boolean isOccupiedRoomQuestion(
            String message
    ) {

        return containsAny(
                message,
                "occupied room",
                "booked room",
                "filled room",
                "room occupied",
                "bhare room",
                "bharay hue room",
                "भरे हुए कमरे",
                "बुक कमरे",
                "பயன்பாட்டில் உள்ள அறை",
                "நிரம்பிய அறை",
                "ఉపయోగంలో గదులు",
                "నిండిన గదులు",
                "ಬಳಕೆಯಲ್ಲಿರುವ ಕೊಠಡಿ",
                "ತುಂಬಿದ ಕೊಠಡಿ",
                "ব্যবহৃত রুম",
                "ভর্তি রুম",
                "भरलेल्या खोल्या",
                "भरलेले रूम",
                "ભરેલા રૂમ",
                "ਵਰਤੋਂ ਵਿੱਚ ਕਮਰੇ",
                "ਭਰੇ ਹੋਏ ਕਮਰੇ",
                "ഉപയോഗത്തിലുള്ള മുറി",
                "ବ୍ୟବହୃତ କୋଠରୀ",
                "ব্যৱহৃত কোঠা",
                "بھرے ہوئے کمرے",
                "chambres occupées"
        );
    }

    private boolean containsAny(
            String message,
            String... keywords
    ) {

        for (
                String keyword
                : keywords
        ) {

            if (
                    message.contains(
                            keyword.toLowerCase()
                    )
            ) {
                return true;
            }
        }

        return false;
    }
}