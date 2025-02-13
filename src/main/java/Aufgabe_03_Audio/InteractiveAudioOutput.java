package Aufgabe_03_Audio;/*
 * Beispielcode VL Interaktive Systeme, Angewandte Informatik, DHBW Mannheim
 *
 * Prof. Dr. Eckhard Kruse
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
 * @author Eckhard Kruse
 */
class InteractiveAudioOutput {

    double actX, actY, oldX, oldY;    // Alte + aktuelle Mausposition
    double mouse_val1, mouse_val2;    // Mausbewegungsabhängige Werte

    // Audio data
    SourceDataLine line;
    byte val;
    byte[] wavBuffer;
    byte[] wavOutput;
    int bufPointer;

    final int bufLen = 4410;    // TODO: Welche Funktion hat dieser Parameter? => Maximale "Länge" des abgespielten .wav-Clips?

    InteractiveAudioOutput() {

        wavOutput = new byte[bufLen];

        // Werte mit Defaults initialisieren
        oldX = 0;
        oldY = 0;
        mouse_val1 = mouse_val2 = 0;
        bufPointer = 0;
        try {
            // TODO: use different audio files for different screen regions
            // open audio file and read it into byte buffer
            InputStream audioSrc = getClass().getResourceAsStream("/water.wav");
            if (audioSrc == null) {
                System.err.println("Error: Audio file not found!");
                return;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(audioSrc));
            AudioFormat format = stream.getFormat();
            System.out.println("Wav File: " + format.getSampleRate() + " Hz, " + format.getSampleSizeInBits() + " Bits pro Sample"
                    + " FrameSize: " + format.getFrameSize());
            wavBuffer = new byte[(int) (format.getFrameSize() * stream.getFrameLength())];
            stream.read(wavBuffer);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, bufLen * 2);
            line.start();
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
        // Mausgeschwindigkeit -> Lautstärke
        // Maus Y-Koordinate -> Tonhöhe
        // TODO: Bestimme Mausgeschwindigkeit mit oldX, oldY
        mouse_val1 = 1.;
        actX = e.getX();
        actY = e.getY();
    }

    /**
     * Timer-gesteuerte Audioausgabe
     */
    void audioUpdate() {
        if (line.available() >= bufLen) { // genug Platz für neue Daten
            for (int i = 0; i < bufLen; i++) {
                // TODO: Lineare Interpolation zwischen 2 Samples
                int val = (int) wavBuffer[bufPointer] & 255;
                // val = 128 ist Nulllinie, Wertebereich 0-255

                // TODO: Y -> variable Schrittweite -> Tonhöhe
                // TODO: Lautstärke abhängig von Mausgeschwindigkeit
                bufPointer = (bufPointer + 1) % (wavBuffer.length - 1);
                wavOutput[i] = (byte) val;
            }
            line.write(wavOutput, 0, bufLen);
        }
        mouse_val1 *= 0.5; // Fade out
    }
}

