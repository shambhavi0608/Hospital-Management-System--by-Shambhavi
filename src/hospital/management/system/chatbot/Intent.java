package hospital.management.system.chatbot;

/**
 * Chatbot ke supported user intentions.
 *
 * Fixed intent categories chatbot ko predictable
 * aur maintainable banati hain.
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