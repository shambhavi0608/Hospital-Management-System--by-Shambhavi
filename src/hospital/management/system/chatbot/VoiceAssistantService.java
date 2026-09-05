package hospital.management.system.chatbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public final class VoiceAssistantService {

    private static final int LISTEN_SECONDS = 12;

    private static final int LISTEN_TIMEOUT_SECONDS = 20;

    private static final int SPEAK_TIMEOUT_SECONDS = 40;

    public boolean isSupported() {

        String os =
                System.getProperty(
                        "os.name",
                        ""
                );

        return os
                .toLowerCase()
                .contains("win");
    }

    // ============================================================
    // LISTEN
    // ============================================================

    public String listen(
            AssistantLanguage language
    ) throws Exception {

        ensureWindows();

        AssistantLanguage selected =
                language == null
                        ? AssistantLanguage.HINGLISH
                        : language;

        String requestedCulture =
                selected.getCultureCode();

        String script =
                "$ErrorActionPreference = 'Stop'; "

                        + "[Console]::OutputEncoding = "
                        + "[System.Text.Encoding]::UTF8; "

                        + "Add-Type -AssemblyName System.Speech; "

                        + "$installed = "
                        + "[System.Speech.Recognition."
                        + "SpeechRecognitionEngine]"
                        + "::InstalledRecognizers(); "

                        + "$requested = '"
                        + escapePowerShell(
                        requestedCulture
                )
                        + "'; "

                        + "$recognizerInfo = "
                        + "$installed | "
                        + "Where-Object { "
                        + "$_.Culture.Name -eq $requested "
                        + "} | Select-Object -First 1; "

                        // First fallback: en-IN
                        + "if ($null -eq $recognizerInfo) { "
                        + "$recognizerInfo = "
                        + "$installed | "
                        + "Where-Object { "
                        + "$_.Culture.Name -eq 'en-IN' "
                        + "} | Select-Object -First 1; "
                        + "} "

                        // Second fallback: en-US
                        + "if ($null -eq $recognizerInfo) { "
                        + "$recognizerInfo = "
                        + "$installed | "
                        + "Where-Object { "
                        + "$_.Culture.Name -eq 'en-US' "
                        + "} | Select-Object -First 1; "
                        + "} "

                        // Third fallback: any English recognizer
                        + "if ($null -eq $recognizerInfo) { "
                        + "$recognizerInfo = "
                        + "$installed | "
                        + "Where-Object { "
                        + "$_.Culture.Name -like 'en-*' "
                        + "} | Select-Object -First 1; "
                        + "} "

                        // Last fallback: any recognizer
                        + "if ($null -eq $recognizerInfo) { "
                        + "$recognizerInfo = "
                        + "$installed | "
                        + "Select-Object -First 1; "
                        + "} "

                        + "if ($null -eq $recognizerInfo) { "
                        + "throw 'No Windows speech recognizer is installed.'; "
                        + "} "

                        + "$recognizer = New-Object "
                        + "System.Speech.Recognition."
                        + "SpeechRecognitionEngine"
                        + "($recognizerInfo.Culture); "

                        + "$recognizer.SetInputToDefaultAudioDevice(); "

                        + "$grammar = New-Object "
                        + "System.Speech.Recognition.DictationGrammar; "

                        + "$recognizer.LoadGrammar($grammar); "

                        + "$result = $recognizer.Recognize("
                        + "[TimeSpan]::FromSeconds("
                        + LISTEN_SECONDS
                        + ")); "

                        + "if ($null -ne $result) { "
                        + "$result.Text "
                        + "} "

                        + "$recognizer.Dispose();";

        Process process =
                startPowerShell(script);

        boolean completed =
                process.waitFor(
                        LISTEN_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                );

        if (!completed) {

            process.destroyForcibly();

            throw new IllegalStateException(
                    "Listening timed out. Please try again."
            );
        }

        String output =
                readProcessOutput(process);

        String error =
                readProcessError(process);

        if (
                process.exitValue() != 0
        ) {

            if (
                    error == null
                            || error.isBlank()
            ) {

                throw new IllegalStateException(
                        "Windows speech recognition failed."
                                + " Check microphone permission."
                );
            }

            throw new IllegalStateException(
                    cleanPowerShellError(error)
            );
        }

        if (
                output == null
                        || output.isBlank()
        ) {

            throw new IllegalStateException(
                    "No speech was detected. "
                            + "Please speak clearly and try again."
            );
        }

        return output.trim();
    }

    // ============================================================
    // SPEAK
    // ============================================================

    public void speak(
            String text
    ) throws Exception {

        speak(
                text,
                AssistantLanguage.ENGLISH
        );
    }

    public void speak(
            String text,
            AssistantLanguage language
    ) throws Exception {

        ensureWindows();

        if (
                text == null
                        || text.isBlank()
        ) {
            return;
        }

        AssistantLanguage selected =
                language == null
                        ? AssistantLanguage.ENGLISH
                        : language;

        String encodedText =
                Base64.getEncoder()
                        .encodeToString(
                                text.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        String requestedCulture =
                selected.getCultureCode();

        String script =
                "$ErrorActionPreference = 'Stop'; "

                        + "$bytes = "
                        + "[Convert]::FromBase64String('"
                        + encodedText
                        + "'); "

                        + "$text = "
                        + "[Text.Encoding]::UTF8.GetString($bytes); "

                        + "Add-Type -AssemblyName System.Speech; "

                        + "$speaker = New-Object "
                        + "System.Speech.Synthesis."
                        + "SpeechSynthesizer; "

                        + "$requested = '"
                        + escapePowerShell(
                        requestedCulture
                )
                        + "'; "

                        // Try selected language
                        + "$voice = "
                        + "$speaker.GetInstalledVoices() | "
                        + "Where-Object { "
                        + "$_.VoiceInfo.Culture.Name -eq $requested "
                        + "} | Select-Object -First 1; "

                        // Fallback en-IN
                        + "if ($null -eq $voice) { "
                        + "$voice = "
                        + "$speaker.GetInstalledVoices() | "
                        + "Where-Object { "
                        + "$_.VoiceInfo.Culture.Name -eq 'en-IN' "
                        + "} | Select-Object -First 1; "
                        + "} "

                        // Fallback en-US
                        + "if ($null -eq $voice) { "
                        + "$voice = "
                        + "$speaker.GetInstalledVoices() | "
                        + "Where-Object { "
                        + "$_.VoiceInfo.Culture.Name -eq 'en-US' "
                        + "} | Select-Object -First 1; "
                        + "} "

                        // Any voice
                        + "if ($null -eq $voice) { "
                        + "$voice = "
                        + "$speaker.GetInstalledVoices() | "
                        + "Select-Object -First 1; "
                        + "} "

                        + "if ($null -eq $voice) { "
                        + "throw 'No Windows speech voice is installed.'; "
                        + "} "

                        + "$speaker.SelectVoice("
                        + "$voice.VoiceInfo.Name"
                        + "); "

                        + "$speaker.Rate = 0; "

                        + "$speaker.Volume = 100; "

                        + "$speaker.Speak($text); "

                        + "$speaker.Dispose();";

        Process process =
                startPowerShell(script);

        boolean completed =
                process.waitFor(
                        SPEAK_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                );

        if (!completed) {

            process.destroyForcibly();

            throw new IllegalStateException(
                    "Voice response timed out."
            );
        }

        String error =
                readProcessError(process);

        if (
                process.exitValue() != 0
        ) {

            throw new IllegalStateException(
                    cleanPowerShellError(error)
            );
        }
    }

    // ============================================================
    // CHECK RECOGNIZER
    // ============================================================

    public boolean isRecognitionLanguageAvailable(
            AssistantLanguage language
    ) {

        if (!isSupported()) {
            return false;
        }

        AssistantLanguage selected =
                language == null
                        ? AssistantLanguage.HINGLISH
                        : language;

        String culture =
                selected.getCultureCode();

        String script =
                "$ErrorActionPreference = 'Stop'; "

                        + "Add-Type -AssemblyName System.Speech; "

                        + "$installed = "
                        + "[System.Speech.Recognition."
                        + "SpeechRecognitionEngine]"
                        + "::InstalledRecognizers(); "

                        + "$match = "
                        + "$installed | "
                        + "Where-Object { "
                        + "$_.Culture.Name -eq '"
                        + escapePowerShell(culture)
                        + "' "
                        + "} | Select-Object -First 1; "

                        + "if ($null -ne $match) { "
                        + "Write-Output 'true' "
                        + "} else { "
                        + "Write-Output 'false' "
                        + "}";

        try {

            Process process =
                    startPowerShell(script);

            boolean completed =
                    process.waitFor(
                            10,
                            TimeUnit.SECONDS
                    );

            if (!completed) {

                process.destroyForcibly();

                return false;
            }

            String output =
                    readProcessOutput(process);

            return process.exitValue() == 0
                    && "true".equalsIgnoreCase(
                    output.trim()
            );

        } catch (Exception e) {

            return false;
        }
    }

    // ============================================================
    // SPEECH VOICE CHECK
    // ============================================================

    public boolean isSpeechVoiceAvailable(
            AssistantLanguage language
    ) {

        if (!isSupported()) {
            return false;
        }

        AssistantLanguage selected =
                language == null
                        ? AssistantLanguage.ENGLISH
                        : language;

        String culture =
                selected.getCultureCode();

        String script =
                "$ErrorActionPreference = 'Stop'; "

                        + "Add-Type -AssemblyName System.Speech; "

                        + "$speaker = New-Object "
                        + "System.Speech.Synthesis."
                        + "SpeechSynthesizer; "

                        + "$voice = "
                        + "$speaker.GetInstalledVoices() | "
                        + "Where-Object { "
                        + "$_.VoiceInfo.Culture.Name -eq '"
                        + escapePowerShell(culture)
                        + "' "
                        + "} | Select-Object -First 1; "

                        + "if ($null -ne $voice) { "
                        + "Write-Output 'true' "
                        + "} else { "
                        + "Write-Output 'false' "
                        + "} "

                        + "$speaker.Dispose();";

        try {

            Process process =
                    startPowerShell(script);

            boolean completed =
                    process.waitFor(
                            10,
                            TimeUnit.SECONDS
                    );

            if (!completed) {

                process.destroyForcibly();

                return false;
            }

            String output =
                    readProcessOutput(process);

            return process.exitValue() == 0
                    && "true".equalsIgnoreCase(
                    output.trim()
            );

        } catch (Exception e) {

            return false;
        }
    }

    // ============================================================
    // POWERSHELL
    // ============================================================

    private Process startPowerShell(
            String script
    ) throws IOException {

        String encoded =
                Base64.getEncoder()
                        .encodeToString(
                                script.getBytes(
                                        StandardCharsets.UTF_16LE
                                )
                        );

        ProcessBuilder builder =
                new ProcessBuilder(
                        "powershell.exe",
                        "-NoLogo",
                        "-NoProfile",
                        "-NonInteractive",
                        "-ExecutionPolicy",
                        "Bypass",
                        "-EncodedCommand",
                        encoded
                );

        return builder.start();
    }

    private String readProcessOutput(
            Process process
    ) throws IOException {

        return new String(
                process.getInputStream()
                        .readAllBytes(),
                StandardCharsets.UTF_8
        ).trim();
    }

    private String readProcessError(
            Process process
    ) throws IOException {

        return new String(
                process.getErrorStream()
                        .readAllBytes(),
                StandardCharsets.UTF_8
        ).trim();
    }

    private String cleanPowerShellError(
            String error
    ) {

        if (
                error == null
                        || error.isBlank()
        ) {

            return "Windows speech service failed.";
        }

        String cleaned =
                error.replaceAll(
                        "(?s)#< CLIXML.*",
                        ""
                ).trim();

        if (cleaned.isBlank()) {

            return "Windows speech service failed.";
        }

        if (cleaned.length() > 500) {

            return cleaned.substring(
                    0,
                    500
            );
        }

        return cleaned;
    }

    private String escapePowerShell(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.replace(
                "'",
                "''"
        );
    }

    private void ensureWindows() {

        if (!isSupported()) {

            throw new UnsupportedOperationException(
                    "Voice assistant is available "
                            + "on Windows only."
            );
        }
    }
}