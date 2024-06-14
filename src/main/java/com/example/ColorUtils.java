package com.example;

public class ColorUtils {
    
    /**
     * Converts a color string from the database format (0x...) to the CSS format (#...).
     * 
     * @param dbColor the color string from the database
     * @return the color string in CSS format
     */
    public static String convertDbColorToCss(String dbColor) {
        if (dbColor != null && dbColor.startsWith("0x")) {
            return dbColor.replace("0x", "#");
        }
        return dbColor;
    }
}
