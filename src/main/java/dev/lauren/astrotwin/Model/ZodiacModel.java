package dev.lauren.astrotwin.Model;



public class ZodiacModel { 
    final public static String[] planets = new String[]{"Ascendant", "Sun", "Moon", "Mercury", "Venus", "Mars",
                                                 "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"};
    
                        
    public static String getMode(String zodiac) {
        if (zodiac.equalsIgnoreCase("Aries") ||
            zodiac.equalsIgnoreCase("Cancer") ||
            zodiac.equalsIgnoreCase("Libra") ||
            zodiac.equalsIgnoreCase("Capricorn")) {
                return "Cardinal";
        } else if (zodiac.equalsIgnoreCase("Taurus") ||
                   zodiac.equalsIgnoreCase("Leo") ||
                   zodiac.equalsIgnoreCase("Scorpio") ||
                   zodiac.equalsIgnoreCase("Aquarius")) {
                    return "Fixed";
        } else {
            return "Mutable";
        }
    }

    public static String getElement(String zodiac) {
        if (zodiac.equalsIgnoreCase("Sagittarius") || 
            zodiac.equalsIgnoreCase("Leo") ||
            zodiac.equalsIgnoreCase("Aries")) {
                return "Fire";
        } else if (zodiac.equalsIgnoreCase("Taurus") ||
                   zodiac.equalsIgnoreCase("Virgo") || 
                   zodiac.equalsIgnoreCase("Capricorn")) {
                    return "Earth";
        } else if (zodiac.equalsIgnoreCase("Gemini") ||
                   zodiac.equalsIgnoreCase("Libra") ||
                   zodiac.equalsIgnoreCase("Aquarius")) {
                    return "Air";
        } else {
            return "Water";
        }
    }
}
