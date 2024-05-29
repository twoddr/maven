package general;


import java.awt.*;

public class RGBConverter {

    public static Integer convert_color_2_int(Color color) {

        int rouge = (color.getRed() * 255) << 24;
        int vert = (color.getGreen() * 255) << 16;
        int bleu = (color.getBlue() * 255) << 8;
        int alpha = color.getAlpha() * 255;

        return rouge + vert + bleu + alpha;
        //return couleurInt;
    }

    public static Color convert_int_2_color(int couleur) {
        int red = (couleur >> 24) & 0xFF;
        int green = (couleur >> 16) & 0xFF;
        int blue = (couleur >> 8) & 0xFF;
        int alpha = couleur & 0xFF;

        return new Color(red / 255f,
                green / 255f,
                blue / 255f,
                alpha / 255f);
    }

}
