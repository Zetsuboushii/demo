package Aufgabe_03_Audio;/*
 * Beispielcode VL Interaktive Systeme, Angewandte Informatik, DHBW Mannheim
 *
 * Prof. Dr. Eckhard Kruse
 */

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Timer-gesteuerte Audioausgabe, unter Berücksichtigung der Mausbewegung
 *
 * @author
 */
class InteractiveAudioOutput {

    double actX, actY, oldX, oldY;    // Alte + aktuelle Mausposition
    double mouse_val1, mouse_val2;    // Mausbewegungsabhängige Werte (z.B. Lautstärke, Pitch)
    double pitchPointer = 0.0;        // Für "Tonhöhe" (Abspielgeschwindigkeit)

    // Zwei verschiedene Sounds (für verschiedene "Materialien")
    byte[] waterBuffer;
    byte[] woodBuffer;
    byte[] windBuffer;
    // Welcher Puffer aktuell abgespielt wird
    byte[] activeBuffer;

    SourceDataLine line;
    byte[] wavOutput;
    int bufLen = 4410;    // TODO: Welche Funktion hat dieser Parameter?

    private final Scene scene;

    /**
     * Konstruktor. Lädt zwei unterschiedliche WAVs ("water" und "wood")
     * und öffnet eine Audio-Output-Line mit dem Format des water-Files.
     */
    InteractiveAudioOutput(Scene scene) {

        this.scene = scene;

        wavOutput = new byte[bufLen];
        oldX = 0;
        oldY = 0;
        mouse_val1 = 0;
        mouse_val2 = 1.0; // Standard-Pitch

        try {
            // TODO: use different audio files for different screen regions

            // water.wav
            InputStream waterStreamRaw = getClass().getResourceAsStream("/water.wav");
            if (waterStreamRaw == null) {
                System.err.println("Error: water.wav not found in resources!");
                return;
            }
            AudioInputStream waterAIS = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(waterStreamRaw));
            AudioFormat waterFormat = waterAIS.getFormat();
            System.out.println("Water.wav Format: " + waterFormat);

            waterBuffer = new byte[(int) (waterFormat.getFrameSize() * waterAIS.getFrameLength())];
            waterAIS.read(waterBuffer);

            // wood.wav
            InputStream woodStreamRaw = getClass().getResourceAsStream("/wood.wav");
            if (woodStreamRaw == null) {
                System.err.println("Error: wood.wav not found in resources!");
                return;
            }
            AudioInputStream woodAIS = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(woodStreamRaw));
            AudioFormat woodFormat = woodAIS.getFormat();
            System.out.println("Wood.wav Format: " + woodFormat);

            woodBuffer = new byte[(int) (woodFormat.getFrameSize() * woodAIS.getFrameLength())];
            woodAIS.read(woodBuffer);

            // wind.wav
            InputStream windStreamRaw = getClass().getResourceAsStream("/wind.wav");
            if (windStreamRaw == null) {
                System.err.println("Error: wind.wav not found in resources!");
                return;
            }
            AudioInputStream windAIS = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(windStreamRaw));
            AudioFormat windFormat = windAIS.getFormat();
            System.out.println("Wind.wav Format: " + windFormat);

            windBuffer = new byte[(int) (windFormat.getFrameSize() * windAIS.getFrameLength())];
            windAIS.read(windBuffer);

            // Für Einfachheit gehen wir davon aus, dass beide WAVs dasselbe Format haben:
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, windFormat);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(windFormat, bufLen * 2);
            line.start();

            // Starten wir erstmal mit "water" als default:
            activeBuffer = waterBuffer;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Event-Handler für Mausbewegung
     * TODO 1: Mausgeschwindigkeit soll Einfluss auf Lautstärke und Tonhöhe/Abspielgeschwindigkeit haben
     * TODO 2: Je nach Mausposition im Bild -> verschiedene Klänge verwenden
     *
     * @param e MouseEvent mit Koordinaten
     */
    void mouseMoved(MouseEvent e) {
        double dx = e.getX() - oldX;
        double dy = e.getY() - oldY;
        double speed = Math.sqrt(dx * dx + dy * dy);

        // Mausgeschwindigkeit -> Lautstärke (z.B. 0..1)
        // Hier etwas skaliert und gekappt bei 1.0
        mouse_val1 = Math.min(speed / 10.0, 1.0);

        // Maus Y-Koordinate -> Tonhöhe/Abspielgeschwindigkeit
        // z.B. 0.5 (oben) .. 2.0 (unten)
        double maxY = scene.getHeight();
        double relativeY = e.getY() / maxY;    // 0.0..1.0
        mouse_val2 = 0.5 + 1.5 * relativeY;    // 0.5..2.0

        // TODO 2: verschiedene Klänge je nach Mausposition (x)
        // Beispiel: obere Hälfte => wood/water, rechte Hälfte => water
        if (e.getY() < scene.getHeight() / 2.0) {
            if (e.getX() < scene.getWidth() / 2.0) {
                activeBuffer = woodBuffer;
            } else {
                activeBuffer = windBuffer;
            }
        } else {
            activeBuffer = waterBuffer;
        }

        // Update old coords
        oldX = e.getX();
        oldY = e.getY();
    }

    /**
     * Timer-gesteuerte Audioausgabe
     */
    void audioUpdate() {
        if (line == null) {
            // Falls kein Audio-Stream geöffnet werden konnte
            return;
        }
        if (line.available() >= bufLen) { // genug Platz für neue Daten
            // TODO: Lineare Interpolation zwischen 2 Samples
            // TODO: Y -> variable Schrittweite -> Tonhöhe
            // TODO: Lautstärke abhängig von Mausgeschwindigkeit

            for (int i = 0; i < bufLen; i++) {
                // pitchPointer wird je Sample um "mouse_val2" erhöht:
                pitchPointer += mouse_val2;

                // Integer-Index in activeBuffer:
                int idx = (int) pitchPointer % activeBuffer.length;
                int nextIdx = (idx + 1) % activeBuffer.length;

                // "fraction" ist der Nachkommateil -> für Interpolation
                double fraction = pitchPointer - Math.floor(pitchPointer);

                // Original-Samples (0..255), Nulllinie ~128
                // => in [-128..127]-Bereich verschieben
                double s1 = (activeBuffer[idx] & 0xff) - 128;
                double s2 = (activeBuffer[nextIdx] & 0xff) - 128;

                // Lineare Interpolation
                double sample = s1 + fraction * (s2 - s1);

                // Lautstärke anpassen (mouse_val1)
                sample *= mouse_val1;

                // Zurück in [0..255] verschieben
                sample += 128;

                // Clampen
                if (sample < 0) sample = 0;
                if (sample > 255) sample = 255;

                // Byte speichern
                wavOutput[i] = (byte) (sample);
            }
            line.write(wavOutput, 0, bufLen);
        }

        // Fade-Out für nächste Runde
        mouse_val1 *= 0.5;
    }
}
