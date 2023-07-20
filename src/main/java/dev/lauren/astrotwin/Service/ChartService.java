package dev.lauren.astrotwin.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.IIOException;

import org.springframework.stereotype.Service;

import dev.lauren.astrotwin.Model.AtlasModel;
import dev.lauren.astrotwin.Model.CelebModel;
import dev.lauren.astrotwin.Model.UserForm;
import dev.lauren.astrotwin.Model.AstrologModel;
import dev.lauren.astrotwin.Model.UserModel;
import lombok.AllArgsConstructor;

// Does not access chart database
// This service is meant for calculating charts where
// caller can then use or insert chart model
@Service
public class ChartService {
    // for windows =
    // public static final String ASTROLOG_FPATH = "\\src\\main\\java\\astrotwin\\Astrolog\\astrolog.exe";
    // for mac = 
    private static final String ASTROLOG_FPATH = "/src/main/java/dev/lauren/astrotwin/astrolog/astrolog/astrolog";

    // parsing variables
    private static final int CHART_NODE_IDX = 13;
    private static final int NORTH_IDX = 10;
    private static final int ASC_IDX = 11;
    private static final int PLANET_IDX = 26;
    private static final int ZODIAC_IDX = 42;
    private static final int ELEM_IDX = 42;
    private static final int MODE_IDX = 48;

    private static final int NUM_SIGN = 12;


    public static AstrologModel calculateChart(CelebModel celeb) throws InterruptedException, IOException {
        AstrologModel resChart = new AstrologModel();
        System.out.println(celeb);
        String inputStr = getChartData(celeb.getBlocation(), celeb.getBday());
        String[] astrologOutput = inputStr.split(System.lineSeparator());
        if (astrologOutput.length < 2) throw new IIOException("Unable to retrieve chart from astrolog");
        
        // parse astrolog output
        // planet mappings
        extractPlanetMap(astrologOutput, resChart);
        // analysis mappings
        extractAnalysis(astrologOutput, resChart);

        return resChart;
    }

    public static AstrologModel calculateChart(UserModel user) throws InterruptedException, IOException {
        AstrologModel resChart = new AstrologModel();

        //String locationStr = user.getBstate().length() == 0 ? user.getBtown() + ", " + user.getBcountry() :
        //                                                      user.getBtown() + ", " + user.getBstate() + ", " + user.getBcountry();
        //
        
        
        //resChart.setBlocation(locationStr);      
        //AtlasModel location = AtlasService.getLocation(ASTROLOG_FPATH, ASTROLOG_FPATH, ASTROLOG_FPATH, ASC_IDX)  
        UserForm userData = user.getUserData();
        
        AtlasModel location = AtlasService.getLocation(userData.getLocation().getTown(), 
                                                       userData.getLocation().getCountry(), 
                                                       userData.getLocation().getCode(), 
                                                       userData.getDate().getYear());
        
        String inputStr = getChartData(location, userData.getDate()); // gets data from astrolog program
        String[] astrologOutput = inputStr.split(System.lineSeparator());
        if (astrologOutput.length < 2) throw new IIOException("Unable to retrieve chart from astrolog");
        
        // parse astrolog output
        // planet mappings
        extractPlanetMap(astrologOutput, resChart);
        // analysis mappings
        extractAnalysis(astrologOutput, resChart);

        return resChart;
    }

    // run astrolog program to get chart information for parsing
    // return output string 
    private static String getChartData(AtlasModel location, LocalDateTime birthDateTime) throws InterruptedException, IOException  {     
  
        System.out.println(location.getTown() + location.getLatitude() + " " + location.getLongitude() + " " + birthDateTime.toString() + location.getZone());
        System.out.println("hour" + String.valueOf(birthDateTime.getHour()));
        String[] args = {System.getProperty("user.dir").concat(ASTROLOG_FPATH),  
                            "-v", "-j", "-qa", 
                            String.valueOf(birthDateTime.getMonthValue()), 
                            String.valueOf(birthDateTime.getDayOfMonth()), 
                            String.valueOf(birthDateTime.getYear()), 
                            String.valueOf(birthDateTime.getHour()) + ":" + String.valueOf(birthDateTime.getMinute()), 
                            location.getZone(), location.getLongitude(), location.getLatitude()};
        
        StringBuilder sb = new StringBuilder("");
        ProcessBuilder pb = new ProcessBuilder(args);
        Process process = pb.start(); 
        int waitFlag = process.waitFor();
        System.out.print(process.getErrorStream().read());
        System.out.println(waitFlag);
        InputStream error = process.getErrorStream();

    // Read from InputStream
        for (int k = 0; k < error.available(); ++k) {
            System.out.println("Error stream = " + error.read());
        }
        //if (waitFlag == 0) {
            if (process.exitValue() == 0) {
                BufferedInputStream in = (BufferedInputStream) process.getInputStream();
                System.out.println(in.read());
                byte[] contents = new byte[1024];
                int bytesRead = 0;

                while ((bytesRead = in.read(contents)) != -1) {
                    sb.append(new String(contents, 0, bytesRead));
                }
            }
        //}
        System.out.println(sb.toString());
        return sb.toString();
    }

    private static void extractPlanetMap(String[] astrologOutput, AstrologModel chart) {
        // set chart planets -> zodiac
        if (!astrologOutput[1].substring(0, 4).equalsIgnoreCase("body")) {
                throw new IllegalStateException("Chart string format: 'body' is ".concat(astrologOutput[2].substring(0, 4)));
        }
        for (int i = 3; i < 15; i ++) {
            //String cuspAbv = astrologOutput[i].substring(astrologOutput[i].length() - 5, astrologOutput[i].length() - 2);
            //Zodiac cusp = getAbvEnum(Zodiac.values(), cuspAbv);
            //cuspMap.put(i - 2, cusp);
            

            if (i != CHART_NODE_IDX) {
                String planetAbv = astrologOutput[i].substring(0, 3);
                String zodiacAbv = astrologOutput[i].substring(8, 11);
                String house = astrologOutput[i].substring(29, 31);
    
                Planet planet = getAbvEnum(Planet.values(), planetAbv);
                Zodiac zodiac = getAbvEnum(Zodiac.values(), zodiacAbv);
    
                chart.getSignMap().put(planet.toString(), zodiac.toString());
                chart.getHouseMap().put(planet.toString(), Integer.parseInt(house.trim()));

            } 
        }
    }

    private static void extractAnalysis(String[] astrologOutput, AstrologModel chart) {
        extractPlanet(astrologOutput, chart);
        extractZodiac(astrologOutput, chart);
        extractModes(astrologOutput, chart);
        extractElement(astrologOutput, chart);
    }

    private static void extractPlanet(String[] astrologInput, AstrologModel chart) {
        if (!astrologInput[PLANET_IDX - 1].substring(2, 8).equalsIgnoreCase("planet")) {
            throw new IllegalStateException("Chart string format: 'planet' is ".concat(astrologInput[25].substring(2, 8)));
        }
        
        List<ChartNode> temp = new ArrayList<>();
        for (int i = PLANET_IDX; i < PLANET_IDX + NUM_SIGN; i ++) {
            // North node we dont use
            if (i - PLANET_IDX == NORTH_IDX) continue;
            
            String[] fragments = astrologInput[i].split(":");
            String planet = fragments[0].trim();
            String[] equation = fragments[1].split("/");
            String percent = equation[1].replaceAll("\\s+|%", "");
      
            //Ascendant last letter gets cut off
            if (i - PLANET_IDX == ASC_IDX) planet = planet.concat("t");

            temp.add(new ChartNode(planet, Double.valueOf(percent)));
        }
        Collections.sort(temp, (n2, n1) -> n1.value.compareTo(n2.value));

        for (ChartNode node : temp) {     
            chart.getPlanetPercent().add(new ArrayList<>(Arrays.asList(node.name, String.valueOf(node.value))));
        }
    }

    private static void extractZodiac(String[] astrologInput, AstrologModel chart) {
        if (!astrologInput[ZODIAC_IDX - 1].substring(7, 11).equalsIgnoreCase("sign")) {
            throw new IllegalStateException("Chart string format: 'sign' is ".concat(astrologInput[41]).substring(7, 11));
        }
        
        List<ChartNode> temp = new ArrayList<>();
        for (int i = ZODIAC_IDX; i < ZODIAC_IDX + NUM_SIGN; i ++) {
            // Zodiacs
            String[] fragments = astrologInput[i].split("%");
            String[] zodiacsStr = fragments[0].split("[:|/]");
            String zodiac = zodiacsStr[0].trim();
            String percent = zodiacsStr[2].trim();

            temp.add(new ChartNode(zodiac, Double.valueOf(percent)));
        }
        Collections.sort(temp, (n2, n1) -> n1.value.compareTo(n2.value));

        for (ChartNode node : temp) {     
            chart.getZodiacPercent().add(new ArrayList<>(Arrays.asList(node.name, String.valueOf(node.value))));
        }
    }
    
    private static void extractElement(String[] astrologInput, AstrologModel chart) {
        List<ChartNode> temp = new ArrayList<>();
        for (int i = ELEM_IDX; i < ELEM_IDX + Element.values().length; i ++) {

            String[] fragments = astrologInput[i].split("%");
            if (fragments.length < 2) {
                throw new IllegalStateException("Chart String format issue Elements");
            }
            String[] elementStr = fragments[1].split("[:|/]");
            String element = elementStr[0].replaceAll("\\s+|-", "");
            String percent = elementStr[elementStr.length - 1].trim();

            temp.add(new ChartNode(element, Double.valueOf(percent)));
        }
            
        Collections.sort(temp, (n2, n1) -> n1.value.compareTo(n2.value));

        for (ChartNode node : temp) {     
            chart.getElementPercent().add(new ArrayList<>(Arrays.asList(node.name, String.valueOf(node.value))));
        }
    }

    private static void extractModes(String[] astrologInput, AstrologModel chart) {
        List<ChartNode> temp = new ArrayList<>();
        for (int i = MODE_IDX; i < MODE_IDX + Mode.values().length; i ++) {

            String[] fragments = astrologInput[i].split("%");
            if (fragments.length < 2) {
                throw new IllegalStateException("Chart String format issue Mode");
            }
            String[] modeStr = fragments[1].split("[:|/]");
            String mode = modeStr[0].replaceAll("\\s+|-", "");
            String percent = modeStr[modeStr.length - 1].trim();

            temp.add(new ChartNode(mode, Double.valueOf(percent)));
        }
        Collections.sort(temp, (n2, n1) -> n1.value.compareTo(n2.value));

        for (ChartNode node : temp) {     
            chart.getModePercent().add(new ArrayList<>(Arrays.asList(node.name, String.valueOf(node.value))));
        }
    }

    // returns the enum for the abreviated string
    private static <T extends Enum<T> & SignComponent> T getAbvEnum(T[] enumVals, String abv) {
        T retVal = null;
        for (T x : enumVals) {
            if (x.abv().equals(abv)) {
                if (retVal == null) {
                        retVal = x;
                } else {
                        throw new IllegalArgumentException("More than one SignComponent matches abv");
                }
            }
        }
        if (retVal == null) {
            throw new IllegalStateException("SignComponent is null for chart abv");
        }
        return retVal;
    }

    @AllArgsConstructor
    private static class ChartNode {
        String name;
        Double value;
    }

    
}


interface ChartComponent {
    public String toString();
}

interface SignComponent extends ChartComponent{
public String abv();
}

enum Element implements ChartComponent{
FIRE("Fire"), EARTH("Earth"), AIR("Air"), WATER("Water");
private String elementName;

private Element(String element) {
    this.elementName = element;

}

@Override
public String toString() {
    return elementName;
}

}

enum Mode implements ChartComponent{
CARDINAL("Cardinal"), FIXED("Fixed"), MUTABLE("Mutable");
private String modeName;

private Mode(String mode) {
    this.modeName = mode;
}

@Override 
public String toString() {
    return this.modeName;
}

} 

enum Zodiac implements SignComponent{
ARIES ("Aries", Mode.CARDINAL, Element.FIRE), 
TAURUS("Taurus", Mode.FIXED, Element.EARTH), 
GEMINI("Gemini", Mode.MUTABLE, Element.AIR), 
CANCER("Cancer", Mode.CARDINAL, Element.WATER), 
LEO("Leo", Mode.FIXED, Element.FIRE), 
VIRGO("Virgo", Mode.MUTABLE, Element.EARTH), 
LIBRA("Libra", Mode.CARDINAL, Element.AIR), 
SCORPIO("Scorpio", Mode.FIXED, Element.WATER), 
SAGITTARIUS("Sagittarius", Mode.MUTABLE, Element.FIRE), 
CAPRICORN("Capricorn", Mode.CARDINAL, Element.EARTH), 
AQUARIUS("Aquarius", Mode.FIXED, Element.AIR), 
PISCES("Pisces", Mode.MUTABLE, Element.WATER);
private String zodiacName;
private Mode mode;
private Element element;

private Zodiac(String zodiac, Mode mode, Element element) {
    this.zodiacName = zodiac;
    this.mode = mode;
    this.element = element;
}

@Override
public String toString() {
    return zodiacName;
}

public String abv() {
    return zodiacName.substring(0, 3);
}

public String getMode() {
    return mode.toString();
}

public String getElement() {
    return element.toString();
}
}

enum Planet implements SignComponent{
ASCENDANT("Ascendant"), SUN("Sun"), MOON("Moon"), 
MERCURY("Mercury"), VENUS("Venus"), MARS("Mars"),
JUPITER("Jupiter"), SATURN("Saturn"), URANUS("Uranus"), 
NEPTUNE("Neptune"), PLUTO("Pluto");
private String planetName;


private Planet(String planet) {
    this.planetName = planet;
}

@Override
public String toString() {
    return planetName;
}

public String abv() {
    return planetName.substring(0, 3);
}
}
