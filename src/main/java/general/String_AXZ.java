package general;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class String_AXZ {

    // Custom key for encryption/decryption
    private static final String CUSTOM_KEY = "C5A-50FT"; // Change this to something unique

    // Encrypt a string
    /*public static String encrypt(String input) {
        StringBuilder encrypted = new StringBuilder();
        int keyLength = CUSTOM_KEY.length();

        for (int i = 0; i < input.length(); i++) {
            // XOR the character with a key character (repeats key if necessary)
            char keyChar = CUSTOM_KEY.charAt(i % keyLength);
            char encryptedChar = (char) (input.charAt(i) ^ keyChar);
            encrypted.append(encryptedChar);
        }

        // Convert to Base64 to make it unreadable
        return Base64.getEncoder().encodeToString(encrypted.toString().getBytes());
    }*/
    public static String encrypt(String input) {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = new byte[inputBytes.length];
        int keyLength = CUSTOM_KEY.length();

        for (int i = 0; i < inputBytes.length; i++) {
            byte keyByte = (byte) CUSTOM_KEY.charAt(i % keyLength);
            encryptedBytes[i] = (byte) (inputBytes[i] ^ keyByte); // XOR on bytes
        }

        return Base64.getEncoder().encodeToString(encryptedBytes); // Directly encode bytes
    }

    // Decrypt a string
    public static String decrypt(String encrypted) {
        // Decode from Base64
        byte[] decodedBytes = Base64.getDecoder().decode(encrypted == null ? "NULL" : encrypted);
        String decodedString = new String(decodedBytes);

        StringBuilder decrypted = new StringBuilder();
        int keyLength = CUSTOM_KEY.length();

        for (int i = 0; i < decodedString.length(); i++) {
            // XOR the character with the same key character
            char keyChar = CUSTOM_KEY.charAt(i % keyLength);
            char decryptedChar = (char) (decodedString.charAt(i) ^ keyChar);
            decrypted.append(decryptedChar);
        }

        return decrypted.toString();
    }

    /*public static void main(String[] args) {
        String originalText = "Hello World, this is CSA-Soft !";
        System.out.println("Original Text: " + originalText);

        // Encrypt
        String encryptedText = encrypt(originalText);
        System.out.println("Encrypted Text: " + encryptedText);

        // Decrypt
        String decryptedText = decrypt(encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);
    }*/
}

