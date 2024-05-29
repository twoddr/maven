package general;

import javax.swing.*;

public class OSFinder {
    public static String getOSName() {
        UIManager.LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo installedLookAndFeel : installedLookAndFeels) {
            if ("Windows".equals(installedLookAndFeel.getName())) {
                return "Windows";
            } else if (installedLookAndFeel.getName().contains("QT")) {
                return "Linux QT";
            } else if (installedLookAndFeel.getName().contains("GTK")) {
                return "Linux GTK";
            } else if (installedLookAndFeel.getName().contains("Mac")) {
                return "MacOS";
            }
        }
        return "Inconnu !";
    }
}
