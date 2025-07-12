package hospital.management.system.chatbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Windows Speech Recognition aur Text-to-Speech service.
 *
 * Features:
 * - Microphone se voice input
 * - Voice ko text mein convert karna
 * - Assistant response ko bolkar sunana
 * - Selected language culture use karna
 * - Kisi paid cloud API ki requirement nahi
 *
 * Note:
 * Selected language ka Windows language/speech pack
 * computer mein installed hona chahiye.
 */
public final class VoiceAssistantService {

    private static final int LISTEN_SECONDS = 12;
    private static final int LISTEN_TIMEOUT_SECONDS = 20;
    private static final int SPEAK_TIMEOUT_SECONDS = 40;

    /**
     * Voice feature filhaal Windows ke liye configured hai.
     */
    public boolean isSupported() {

        String operatingSystem =
                System.getProperty(
                        "os.name",
                        ""
                );

        return operatingSystem
                .toLowerCase()
                .contains("win");
    }

    /**
     * Microphone se voice listen karke recognized text return karta hai.
     */
    public String listen(
            AssistantLanguage language
    ) throws Exception {

        ensureWindows();

        AssistantLanguage selectedLanguage =
                language == null
                        ? AssistantLanguage.HINGLISH
                        : language;

        String cultureCode =
                selectedLanguage.getCultureCode();

        String script =
                "$ErrorActionPreference = 'Stop'; "
                        + "[Console]::OutputEncoding = "
                        + "[System.Text.Encoding]::UTF8; "
                        + "Add-Type -AssemblyName System.Speech; "
                        + "$installed = [System.Speech.Recognition."
                        + "SpeechRecognitionEngine]::InstalledRecognizers(); "
                        + "$recognizerInfo = $installed | Where-Object { "
                        + "$_.Culture.Name -eq '"
                        + cultureCode
                        + "' } | Select-Object -First 1; "
                        + "if ($null -eq $recognizerInfo) { "
                        + "$recognizerInfo = $installed | Where-Object { "
                        + "$_.Culture.Name -eq 'en-IN' } "
                        + "| Select-Object -First 1; } "
                        + "if ($null -eq $recognizerInfo) { "
                        + "$recognizerInfo = $installed | Where-Object { "
                        + "$_.Culture.Name -eq 'en-US' } "
                        + "| Select-Object -First 1; } "
                        + "if ($null -eq $recognizerInfo) { "
                        + "$recognizerInfo = $installed "
                        + "| Select-Object -First 1; } "
                        + "if ($null -eq $recognizerInfo) { "
                        + "throw 'No Windows speech recognizer is installed.'; } "
                        + "$recognizer = New-Object "
                        + "System.Speech.Recognition."
                        + "SpeechRecognitionEngine($recognizerInfo.Culture); "
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
                        + "}";

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

        if (process.exitValue() != 0) {

            if (
                    error == null
                            ||
                            error.isBlank()
            ) {

                throw new IllegalStateException(
                        "Voice recognition could not start. "
                                + "Check microphone permission and "
                                + "Windows speech language."
                );
            }

            throw new IllegalStateException(
                    cleanPowerShellError(error)
            );
        }

        if (
                output == null
                        ||
                        output.isBlank()
        ) {

            throw new IllegalStateException(
                    "No speech was detected. "
                            + "Please speak clearly and try again."
            );
        }

        return output.trim();
    }

    /**
     * Assistant ke response ko Windows Text-to-Speech se speak karta hai.
     */
    public void speak(
            String text
    ) throws Exception {

        speak(
                text,
                AssistantLanguage.ENGLISH
        );
    }

    /**
     * Selected language ke according voice response speak karta hai.
     */
    public void speak(
            String text,
            AssistantLanguage language
    ) throws Exception {

        ensureWindows();

        if (
                text == null
                        ||
                        text.isBlank()
        ) {
            return;
        }

        AssistantLanguage selectedLanguage =
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

        String cultureCode =
                selectedLanguage.getCultureCode();

        String script =
                "$ErrorActionPreference = 'Stop'; "
                        + "$bytes = [Convert]::FromBase64String('"
                        + encodedText
                        + "'); "
                        + "$text = [Text.Encoding]::UTF8.GetString($bytes); "
                        + "Add-Type -AssemblyName System.Speech; "
                        + "$speaker = New-Object "
                        + "System.Speech.Synthesis.SpeechSynthesizer; "
                        + "$culture = '"
                        + cultureCode
                        + "'; "
                        + "$voice = $speaker.GetInstalledVoices() "
                        + "| Where-Object { "
                        + "$_.VoiceInfo.Culture.Name -eq $culture "
                        + "} | Select-Object -First 1; "
                        + "if ($null -ne $voice) { "
                        + "$speaker.SelectVoice($voice.VoiceInfo.Name); "
                        + "} "
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

        if (process.exitValue() != 0) {

            if (
                    error == null
                            ||
                            error.isBlank()
            ) {

                throw new IllegalStateException(
                        "Text-to-speech could not start."
                );
            }

            throw new IllegalStateException(
                    cleanPowerShellError(error)
            );
        }
    }

    /**
     * Check karta hai ki selected recognition language
     * Windows mein available hai ya nahi.
     */
    public boolean isRecognitionLanguageAvailable(
            AssistantLanguage language
    ) {

        if (!isSupported()) {
            return false;
        }

        AssistantLanguage selectedLanguage =
                language == null
                        ? AssistantLanguage.HINGLISH
                        : language;

        String cultureCode =
                selectedLanguage.getCultureCode();

        String script =
                "$ErrorActionPreference = 'Stop'; "
                        + "Add-Type -AssemblyName System.Speech; "
                        + "$installed = "
                        + "[System.Speech.Recognition."
                        + "SpeechRecognitionEngine]"
                        + "::InstalledRecognizers(); "
                        + "$match = $installed "
                        + "| Where-Object { "
                        + "$_.Culture.Name -eq '"
                        + cultureCode
                        + "' }; "
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
                    &&
                    "true".equalsIgnoreCase(
                            output.trim()
                    );

        } catch (Exception exception) {

            return false;
        }
    }

    private Process startPowerShell(
            String script
    ) throws IOException {

        String encodedScript =
                Base64.getEncoder()
                        .encodeToString(
                                script.getBytes(
                                        StandardCharsets.UTF_16LE
                                )
                        );

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "powershell.exe",
                        "-NoLogo",
                        "-NoProfile",
                        "-NonInteractive",
                        "-ExecutionPolicy",
                        "Bypass",
                        "-EncodedCommand",
                        encodedScript
                );

        return processBuilder.start();
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

        String cleaned =
                error.replaceAll(
                        "(?s)#< CLIXML.*",
                        ""
                ).trim();

        if (cleaned.isBlank()) {

            return "Windows speech service failed. "
                    + "Check microphone permission and "
                    + "installed speech language.";
        }

        if (
                cleaned.length()
                        > 500
        ) {

            return cleaned.substring(
                    0,
                    500
            );
        }

        return cleaned;
    }

    private void ensureWindows() {

        if (!isSupported()) {

            throw new UnsupportedOperationException(
                    "Voice assistant is currently available "
                            + "only on Windows."
            );
        }
    }
}
