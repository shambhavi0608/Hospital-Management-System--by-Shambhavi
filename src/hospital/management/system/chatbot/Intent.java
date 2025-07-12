package hospital.management.system.chatbot;

/*
 * Chatbot ke supported user intentions.
 *
 * INTERVIEW:
 * Enum fixed intent categories provide karta hai,
 * jisse chatbot predictable aur maintainable rehta hai.
 */
public enum Intent {

    GREETING,
    TOTAL_PATIENTS,
    AVAILABLE_ROOMS,
    OCCUPIED_ROOMS,
    ROOM_OCCUPANCY,
    HELP,
    MEDICAL_ADVICE,
    UNKNOWN
}