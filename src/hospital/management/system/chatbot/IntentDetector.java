package hospital.management.system.chatbot;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class IntentDetector {

    public Intent detectIntent(
            String userMessage
    ) {

        if (
                userMessage == null
                        || userMessage.isBlank()
        ) {
            return Intent.UNKNOWN;
        }

        String message =
                normalize(userMessage);

        // --------------------------------------------------------
        // MEDICAL / PRESCRIPTION
        // --------------------------------------------------------

        if (containsAny(
                message,
                "medicine",
                "medicines",
                "dawai",
                "dawa",
                "dava",
                "treatment",
                "diagnosis",
                "symptom",
                "symptoms",
                "prescription",
                "tablet",
                "tablets",
                "drug",
                "drugs",
                "doctor se kya lena",
                "kaunsi dawa",
                "konsi dawa",
                "दवाई",
                "दवा",
                "इलाज",
                "लक्षण",
                "मेडिसिन",
                "நோய்",
                "மருந்து",
                "மருத்துவம்",
                "మందు",
                "చికిత్స",
                "ಔಷಧಿ",
                "ಚಿಕಿತ್ಸೆ",
                "ওষুধ",
                "চিকিৎসা",
                "औषध",
                "દવા",
                "સારવાર",
                "ਦਵਾਈ",
                "ਇਲਾਜ",
                "മരുന്ന്",
                "ചികിത്സ",
                "ଔଷଧ",
                "ଚିକିତ୍ସା",
                "دوا",
                "علاج"
        )) {
            return Intent.MEDICAL_ADVICE;
        }

        // --------------------------------------------------------
        // HELP
        // --------------------------------------------------------

        if (containsAny(
                message,
                "help",
                "commands",
                "what can you do",
                "what can i ask",
                "what can i ask you",
                "what can you help",
                "what do you do",
                "how can you help",
                "show help",
                "kya kar sakte",
                "kya kya kar sakte",
                "kya pooch sakte",
                "kya pooch sakti",
                "kya puch sakte",
                "kya puch sakti",
                "mujhe help",
                "madad",
                "sahayata",
                "सहायता",
                "मदद",
                "உதவி",
                "సహాయం",
                "ಸಹಾಯ",
                "সাহায্য",
                "મદદ",
                "ਮਦਦ",
                "സഹായം",
                "ସାହାଯ୍ୟ",
                "مدد",
                "aide"
        )) {
            return Intent.HELP;
        }

        // --------------------------------------------------------
        // ROOM OCCUPANCY
        // --------------------------------------------------------

        if (containsAny(
                message,
                "occupancy",
                "occupancy rate",
                "occupancy percentage",
                "percentage occupied",
                "percentage of rooms",
                "room occupancy",
                "room utilization",
                "room utilisation",
                "room usage",
                "rooms utilization",
                "rooms utilisation",
                "rooms usage",
                "room utilization rate",
                "how full are the rooms",
                "how occupied are the rooms",
                "kitne percent room",
                "kitna percent room",
                "kitne percentage room",
                "room kitna bhara",
                "room kitne percent bhare",
                "rooms kitne percent bhare",
                "rooms kitna bhare",
                "kamre kitne percent bhare",
                "कमरे कितने प्रतिशत",
                "कितने प्रतिशत कमरे",
                "कमरे कितने प्रतिशत भरे",
                "कमरों का उपयोग",
                "room ka occupancy",
                "rooms ka occupancy",
                "room ki occupancy",
                "कमरा कितना भरा",
                "அறை பயன்பாடு",
                "గది ఆక్యుపెన్సీ",
                "ಕೊಠಡಿ ಬಳಕೆ",
                "রুম ব্যবহার",
                "खोली वापर",
                "રૂમ ઓક્યુપન્સી",
                "ਕਮਰਾ ਵਰਤੋਂ",
                "മുറി ഉപയോഗം",
                "କୋଠରୀ ବ୍ୟବହାର",
                "کمرہ استعمال"
        )) {
            return Intent.ROOM_OCCUPANCY;
        }

        // --------------------------------------------------------
        // OCCUPIED ROOMS
        // --------------------------------------------------------

        if (containsAny(
                message,
                "occupied room",
                "occupied rooms",
                "room occupied",
                "rooms occupied",
                "booked room",
                "booked rooms",
                "room booked",
                "rooms booked",
                "filled room",
                "filled rooms",
                "full room",
                "full rooms",
                "room is full",
                "rooms are full",
                "used room",
                "used rooms",
                "rooms in use",
                "room in use",
                "occupied",
                "bhara room",
                "bhare room",
                "bhare hue room",
                "bhare hue rooms",
                "bharay hue room",
                "bharay hue rooms",
                "room bhara",
                "rooms bhare",
                "rooms bharay",
                "kitne room bhare",
                "kitne rooms bhare",
                "kitne kamre bhare",
                "kamre bhare",
                "kamre bhare hue",
                "भरे हुए कमरे",
                "भरे कमरे",
                "कमरे भरे हुए",
                "பயன்பாட்டில் உள்ள அறைகள்",
                "நிரம்பிய அறைகள்",
                "ఉపయోగంలో ఉన్న గదులు",
                "ನಿರಂಬಿದ ಕೊಠಡಿಗಳು",
                "ব্যবহৃত রুম",
                "भरलेल्या खोल्या",
                "ભરેલા રૂમ",
                "ਵਰਤੋਂ ਵਿੱਚ ਕਮਰੇ",
                "ഉപയോഗത്തിലുള്ള മുറികൾ",
                "ବ୍ୟବହୃତ କୋଠରୀ",
                "بھرے ہوئے کمرے"
        )) {
            return Intent.OCCUPIED_ROOMS;
        }

        // --------------------------------------------------------
        // AVAILABLE ROOMS
        // --------------------------------------------------------

        if (containsAny(
                message,
                "available room",
                "available rooms",
                "room available",
                "rooms available",
                "free room",
                "free rooms",
                "empty room",
                "empty rooms",
                "vacant room",
                "vacant rooms",
                "room vacant",
                "rooms vacant",
                "any room available",
                "any rooms available",
                "is any room free",
                "are any rooms free",
                "room is available",
                "rooms are available",
                "koi room available",
                "koi room khali",
                "koi rooms khali",
                "room khali",
                "rooms khali",
                "khali room",
                "khali rooms",
                "kitne room khali",
                "kitne rooms khali",
                "kitne kamre khali",
                "kamre khali",
                "kamra khali",
                "koi kamra khali",
                "कोई कमरा खाली",
                "कितने कमरे खाली",
                "खाली कमरे",
                "कमरा खाली",
                "कमरे उपलब्ध",
                "उपलब्ध कमरे",
                "क्या कोई कमरा खाली",
                "காலி அறை",
                "காலி அறைகள்",
                "அறைகள் கிடைக்குமா",
                "ఖాళీ గది",
                "ఖాళీ గదులు",
                "అందుబాటులో గదులు",
                "ಖಾಲಿ ಕೊಠಡಿ",
                "ಖಾಲಿ ಕೊಠಡಿಗಳು",
                "ಲಭ್ಯ ಕೊಠಡಿಗಳು",
                "খালি রুম",
                "উপলব্ধ রুম",
                "रिकाम्या खोल्या",
                "ઉપલબ્ધ રૂમ",
                "ਖਾਲੀ ਕਮਰੇ",
                "ലഭ്യമായ മുറികൾ",
                "ଖାଲି କୋଠରୀ",
                "دستیاب کمرے",
                "خالی کمرے"
        )) {
            return Intent.AVAILABLE_ROOMS;
        }

        // --------------------------------------------------------
        // TOTAL PATIENTS
        // --------------------------------------------------------

        if (isPatientQuestion(message)) {
            return Intent.TOTAL_PATIENTS;
        }

        // --------------------------------------------------------
        // GREETING
        // --------------------------------------------------------

        if (containsGreeting(message)) {
            return Intent.GREETING;
        }

        return Intent.UNKNOWN;
    }

    private boolean isPatientQuestion(
            String message
    ) {

        boolean patientMentioned =
                containsAny(
                        message,
                        "patient",
                        "patients",
                        "patient count",
                        "number of patient",
                        "number of patients",
                        "total patient",
                        "total patients",
                        "admitted patient",
                        "admitted patients",
                        "admitted",
                        "mari",
                        "mareez",
                        "mariz",
                        "mareej",
                        "mariz kitne",
                        "mareez kitne",
                        "mari kitne",
                        "मरीज",
                        "मरीज़",
                        "रोगी",
                        "मरीजों",
                        "रोगियों",
                        "कुल मरीज",
                        "कितने मरीज",
                        "मरीज कितने",
                        "मरीजों की संख्या",
                        "नॉरोगी",
                        "நோயாளி",
                        "நோயாளிகள்",
                        "రోగి",
                        "రోగులు",
                        "ರೋಗಿ",
                        "ರೋಗಿಗಳು",
                        "রোগী",
                        "রোগীরা",
                        "रुग्ण",
                        "रुग्णालयातील रुग्ण",
                        "દર્દી",
                        "દર્દીઓ",
                        "ਮਰੀਜ਼",
                        "ਮਰੀਜ਼ਾਂ",
                        "രോഗി",
                        "രോഗികൾ",
                        "ରୋଗୀ",
                        "ৰোগী",
                        "مریض",
                        "مریضوں"
                );

        if (!patientMentioned) {
            return false;
        }

        return containsAny(
                message,
                "how many",
                "how much",
                "count",
                "number",
                "total",
                "how many are there",
                "how many admitted",
                "how many patients",
                "kitne",
                "kitna",
                "kitni",
                "kitney",
                "kitne hain",
                "kitne hai",
                "kitne h",
                "kitne hai hospital me",
                "kitne hain hospital mein",
                "kitne patient hain",
                "kitne patients hain",
                "patient kitne",
                "patients kitne",
                "total patient",
                "total patients",
                "patient count",
                "patient ki sankhya",
                "mareez kitne",
                "mareez kitne hain",
                "mareezon ki sankhya",
                "mari kitne",
                "mari kitne hain",
                "कितने",
                "कितना",
                "कितनी",
                "कुल",
                "संख्या",
                "कितने हैं",
                "कितने है",
                "कितने मरीज",
                "मरीज कितने",
                "मरीजों की संख्या",
                "कुल मरीज",
                "किती रुग्ण",
                "एकूण रुग्ण",
                "કેટલા દર્દી",
                "કુલ દર્દી",
                "ਕਿੰਨੇ ਮਰੀਜ਼",
                "ਕੁੱਲ ਮਰੀਜ਼",
                "എത്ര രോഗികൾ",
                "ആകെ രോഗികൾ",
                "કેટલા દર્દી",
                "کيتنے مریض",
                "کل مریض"
        );
    }

    private boolean containsGreeting(
            String message
    ) {

        String[] words =
                message.split("\\s+");

        for (String word : words) {

            String cleaned =
                    word.replaceAll(
                            "^[\\p{Punct}]+|[\\p{Punct}]+$",
                            ""
                    );

            if (
                    cleaned.equals("hi")
                            || cleaned.equals("hello")
                            || cleaned.equals("hey")
                            || cleaned.equals("hii")
                            || cleaned.equals("hiii")
                            || cleaned.equals("namaste")
                            || cleaned.equals("namaskar")
                            || cleaned.equals("bonjour")
                            || cleaned.equals("नमस्ते")
                            || cleaned.equals("नमस्कार")
                            || cleaned.equals("வணக்கம்")
                            || cleaned.equals("నమస్కారం")
                            || cleaned.equals("ನಮಸ್ಕಾರ")
                            || cleaned.equals("নমস্কার")
                            || cleaned.equals("ਸਤ")
                            || cleaned.equals("ਨਮਸਕਾਰ")
                            || cleaned.equals("നമസ്കാരം")
                            || cleaned.equals("ନମସ୍କାର")
            ) {
                return true;
            }
        }

        return message.equals("good morning")
                || message.equals("good afternoon")
                || message.equals("good evening")
                || message.equals("good night")
                || message.equals("good morning assistant")
                || message.equals("hello assistant")
                || message.equals("hi assistant")
                || message.equals("hey assistant")
                || message.equals("sat sri akal")
                || message.equals("السلام علیکم");
    }

    private String normalize(
            String value
    ) {

        String normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFKC
                );

        return normalized
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[!?.,;:]+", " ")
                .replaceAll("\\s+", " ");
    }

    private boolean containsAny(
            String message,
            String... keywords
    ) {

        for (String keyword : keywords) {

            if (
                    keyword == null
                            || keyword.isBlank()
            ) {
                continue;
            }

            String normalizedKeyword =
                    keyword
                            .toLowerCase(Locale.ROOT)
                            .trim();

            if (
                    normalizedKeyword.contains(" ")
            ) {

                if (
                        message.contains(
                                normalizedKeyword
                        )
                ) {
                    return true;
                }

            } else {

                String regex =
                        "(^|[^\\p{L}\\p{N}])"
                                + Pattern.quote(
                                normalizedKeyword
                        )
                                + "([^\\p{L}\\p{N}]|$)";

                if (
                        Pattern
                                .compile(regex)
                                .matcher(message)
                                .find()
                ) {
                    return true;
                }
            }
        }

        return false;
    }
}