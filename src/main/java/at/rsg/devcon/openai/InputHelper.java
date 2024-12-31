package at.rsg.devcon.openai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputHelper {

    /**
     * Liest eine Benutzereingabe mit einer Eingabeaufforderung.
     *
     * @param prompt Die Eingabeaufforderung, die dem Benutzer angezeigt wird.
     * @return Die Benutzereingabe als String.
     * @throws IOException Bei einem I/O-Fehler.
     */
    public static String readLine(String prompt) throws IOException {
        System.out.print(prompt);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return reader.readLine();
    }

    /**
     * Wartet auf die Eingabe eines beliebigen Zeichens durch den Benutzer.
     *
     * @throws IOException Bei einem I/O-Fehler.
     */
    public static void readKey() throws IOException {
        System.out.println("Drücken Sie eine beliebige Taste, um fortzufahren...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        reader.read(); // Nur ein Zeichen lesen
    }

    public static void main(String[] args) {
        try {
            // Beispiel für readLine
            String input = readLine("Geben Sie etwas ein: ");
            System.out.println("Sie haben eingegeben: " + input);

            // Beispiel für readKey
            readKey();
            System.out.println("Taste gedrückt. Beenden...");
        } catch (IOException e) {
            System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
        }
    }
}