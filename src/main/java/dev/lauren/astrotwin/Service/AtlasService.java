package dev.lauren.astrotwin.Service;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

import dev.lauren.astrotwin.Model.AtlasModel;

@Service
public class AtlasService {

    //private static final String ASTROLOG_FPATH = "/src/main/java/dev/lauren/astrotwin/astrolog/astrolog/astrolog";
    
    private static final String ASTROLOG_FPATH = "/astrotwin_springboot/src/main/java/dev/lauren/astrotwin/astrolog/astrolog/astrolog";
    private static final String defaultName = "User";

    public static AtlasModel getLocation(String town, String country, String code, int birthYear) throws IOException, InterruptedException {
        AtlasModel resultAtlasModel = new AtlasModel();
        
        boolean locationFound = false;
        resultAtlasModel.setTown(town);
        resultAtlasModel.setCountry(country);
        
        String placeIdentifyStr = (country.equals("United States") || country.equals("Canada")) ? code : country;
        

        String output = getLocationData(defaultName, town, birthYear);
        String[] lines = output.split("\n");
            
        for (int i = 1; i < lines.length; i++) { // first line is just a header and not part of results    
            System.out.println("line: " + lines[i]);     
            String line = lines[i].replaceAll("^\\s+[\\d]+:\\s", "");          
            String[] tokens = line.split(",", 2);
            if (tokens.length != 2) {
                System.out.println("Results Format Error: Cant split using \",\" between town and (state), country (lat long, timezone)");
                System.out.println("Line is: " + lines[i]);
                throw new IOException();
            }

            String townStr = tokens[0];
            String remaining = tokens[1];
            String[] remainingTokens = remaining.split("[(|)]", 2);
            if (remainingTokens.length != 2) {
                System.out.println("Results format error: Can't split between country and (lat, long, timezone)");
                System.out.println("Line is: " + lines[i]);
                throw new IOException();
            }

            String countryStr = remainingTokens[0];
            if (townStr.equalsIgnoreCase(town) && (countryStr.contains(placeIdentifyStr))) {
                
                // this line is a match so extract polar coordinates and time zone     
                String polarStr = remainingTokens[1].replace(")", "");               
                String removePunct = polarStr.replace(",", "");
                String[] polarTokens = removePunct.split("\\s+");
                if (polarTokens.length != 3) {
                    System.out.println("Polar Format Error: Should have (lat long, timezone)");
                    System.out.println(polarStr);
                    throw new IOException();
                } else {
                    resultAtlasModel.setLongitude(polarTokens[0]);
                    resultAtlasModel.setLatitude(polarTokens[1]);
                    resultAtlasModel.setZone(polarTokens[2]);
                    //System.out.println("Location selected: " + line);
                    locationFound = true;
                    break;
                }
            }
        }

        if (!locationFound) {
            System.out.println("No cities match " + town + ", " + country + " (" + placeIdentifyStr + ")");
            System.out.print(output);
            throw new IllegalStateException();
        }

        return resultAtlasModel;
    }

    // get astrolog data
    public static String getLocationData(String name, String town, int birthYear) throws InterruptedException, IOException {
        String[] args = {System.getProperty("user.dir").concat(ASTROLOG_FPATH), 
                         "-e", "-e", "-zi", name, town,
                         "-qy", String.valueOf(birthYear) ,"-N"};
        StringBuilder sb = new StringBuilder("");
        
        ProcessBuilder pb = new ProcessBuilder(args);
        Process process = pb.start(); 
        int waitFlag = process.waitFor();
        if (waitFlag == 0) {
            if (process.exitValue() == 0) {
                BufferedInputStream in = (BufferedInputStream) process.getInputStream();
                byte[] contents = new byte[1024];
                 
                int bytesRead = 0;
                 
                while ((bytesRead = in.read(contents)) != -1) {
                    sb.append(new String(contents, 0, bytesRead));
                }
            }
        }
        System.out.println("fpath atlas service " + ASTROLOG_FPATH);
        System.out.println("atlas service ");
        System.out.println(sb.toString());
        return sb.toString();
    }

}
