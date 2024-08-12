package dev.lauren.astrotwin.Service;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import dev.lauren.astrotwin.Model.AtlasModel;
import dev.lauren.astrotwin.Model.CelebChartModel;
import dev.lauren.astrotwin.Model.CelebModel;
import dev.lauren.astrotwin.Model.ChartNode;
import dev.lauren.astrotwin.Model.AstrologModel;
import dev.lauren.astrotwin.Model.ZodiacModel;
import dev.lauren.astrotwin.Repositories.CelebChartsRepository;
import dev.lauren.astrotwin.Repositories.CelebsRepository;

@Service
public class CelebService {
    private final String CELEB_CHART = "https://www.astro.com/astro-databank/%s";
    @Autowired
    private CelebsRepository celebsRepository;
    @Autowired
    private CelebChartsRepository celebChartRepository;
    boolean listInitialized = false;
    //Set<CelebModel> celebCache = new HashSet<>();

    Map<String, CelebModel> celebCache = new HashMap<>();
    
    public CelebModel insertCeleb(Map<String, String> celebData, String AstrologFilePath) throws Exception {
        
        System.out.println("Insert celeb");
        for (Map.Entry<String, String> e : celebData.entrySet()) {
            System.out.println("key: " + e.getKey());
            System.out.println("value: " + e.getValue());
        }
        String name = celebData.get("name");
        
        if (listInitialized && celebCache.containsKey(name)) {
            return celebCache.get(name);
        }
        CelebModel celeb = new CelebModel();
        String method = celebData.get("type");
        if (method.equals("name")) {
            // look up celebs birthday information
            String nameFormatted = formatName(name);
            try {

                Document response;
                response = Jsoup.connect(String.format(CELEB_CHART, nameFormatted)).get();
                Elements elements = response.select("td");
                celeb = getCelebModel(name, elements);

                Element imageElement = response.select(".thumbimage").select("[srcset]").first();
                System.out.println("image element " + imageElement);
                if (imageElement != null) {
                    String imageSrc = imageElement.attr("src");
                    System.out.println("image src " + imageSrc);
                    celeb.setImageUrl(imageSrc);
                }
                
            }  catch (Exception e1) {
                System.out.println(e1.toString());
                System.out.println("Unable to search astro-databank for " + nameFormatted);
                throw new Exception("Unable to search astro-databank for " + nameFormatted);
            }
        } else {
            celeb.setBday(LocalDateTime.of(Integer.parseInt(celebData.get("year")), 
                                           Integer.parseInt(celebData.get("month")), 
                                           Integer.parseInt(celebData.get("day")),
                                           Integer.parseInt(celebData.get("hour")), 
                                           Integer.parseInt(celebData.get("minute"))));

            if (method.equals("full")) {
                System.out.println("set manual");
                celeb.setName(celebData.get("name"));
                celeb.setBlocation(new AtlasModel(celebData.get("longitude"), 
                                                  celebData.get("latitude"), 
                                                  celebData.get("zone"), 
                                                  celebData.get("town"), 
                                                  celebData.get("country")));

            } else if (method.equals("calculate")) {
                System.out.println("calculate");
                try {
                    AtlasModel blocation = AtlasService.getLocation(celebData.get("town"), 
                                                                    celebData.get("country"), 
                                                                    celebData.get("state"), 
                                                                    Integer.parseInt(celebData.get("year")),
                                                                    AstrologFilePath);
                    celeb.setBlocation(blocation);
            
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception("Unable to create AtlasModel for celeb");
                }
            }
        }

        // calculate celeb chart
        try {
            AstrologModel celebChart = ChartService.calculateChart(celeb, AstrologFilePath);           
            List<ChartNode> chart = new ArrayList<>();

            for (Map.Entry<String,String> placement : celebChart.getSignMap().entrySet()) {
                String planet = placement.getKey();
                String zodiac = placement.getValue();

                ChartNode chartNode = new ChartNode(planet,
                                                    zodiac,
                                                    ZodiacModel.getElement(zodiac),
                                                    ZodiacModel.getMode(zodiac),
                                                    celebChart.getHouseMap().get(planet));

                chart.add(chartNode);
            }
            CelebChartModel celebChartModel = celebChartRepository.insert(new CelebChartModel(chart));
            celeb.setCelebChart(celebChartModel);
            CelebModel celebDB = celebsRepository.insert(celeb);
            celebCache.put(celebDB.getName(), celebDB);
            return celebDB;

        } catch (Exception e1) {
            System.out.println("Unable to add celeb encountered error while creating celeb");
            e1.printStackTrace();
            throw new Exception("Unable to add celeb encountered error while creating celeb");
        }
    }


    private String formatName(String name) {
        String temp = name.replaceAll("\\p{Punct}", "");
        String[] surnames = temp.split(" ");
        StringBuilder nameFormatted = new StringBuilder("");
        if (surnames.length > 1) {
            nameFormatted.append(surnames[surnames.length -1]); // get last name
            nameFormatted.append(",");
            for (int i = 0; i < surnames.length -1; i++) {
                nameFormatted.append("_" + surnames[i]);
            }
        } else {
            nameFormatted.append(surnames[0]);
        }
        return nameFormatted.toString();
    }

    private CelebModel getCelebModel(String name, Elements elements) throws Exception {
        // Save strings from response with information we need to extract data to create Birthday for Person
        boolean savePlace = false;
        boolean saveDate = false;
        boolean saveTimezone = false;
        StringBuilder dateStr = new StringBuilder("");
        StringBuilder placeStr = new StringBuilder("");
        StringBuilder timezoneStr = new StringBuilder("");
        for (Element e : elements) {
            if (e.hasText()) {
                if (e.text().equals("Place")) {
                    savePlace = true; // Following line contains place information
                } else if (e.text().equals("born on")) {
                    saveDate = true; // Following line contains birthdate and time information
                } else if (e.text().equals("Timezone")) {
                    saveTimezone = true;
                } else if (saveDate) {
                    dateStr.append(e.text());
                    //System.out.println("datestr: " + dateStr.toString());
                    saveDate = false;
                } else if (savePlace) {
                    placeStr.append(e.text());
                    //System.out.println("placestr: " + placeStr.toString());
                    savePlace = false;
                } else if (saveTimezone) {
                    timezoneStr.append(e.text());
                    //System.out.println("timezonestr: " + timezoneStr.toString());
                    break;
                }     
            }
        }
                

        LocalDateTime birthDateTime = extraDateTime(dateStr.toString());
        String[] placeTokens = placeStr.toString().split("\\,");
        String[] timeTokens = timezoneStr.toString().split(" ");

        if (placeTokens.length >= 2 && birthDateTime != null) {
            String town = placeTokens[0];
            String country = placeTokens[1];

            if (placeTokens.length == 4 && timeTokens.length >= 2) {
                // can create Atlas model using coordinates for better accuracy - ie dont have to do location search for chart
                String latitude = placeTokens[2].trim();
                String longitude = placeTokens[3].trim();
                String timezone = timeTokens[1];
                AtlasModel birthLocation = new AtlasModel(longitude, latitude, timezone, town, country);
                System.out.println(name + " " + birthDateTime.toString() + " " + birthLocation.getTown());
                return new CelebModel(name, birthDateTime, birthLocation);
            } else {
                return new CelebModel(name, birthDateTime, town, country);
            }  
        
        } else {
            return null;
        }
    }

    private LocalDateTime extraDateTime(String s) {
        if (s.isEmpty()) return null;
        String[] tokens = s.split(" ");
        if (tokens.length > 4) {
            int day = Integer.parseInt(tokens[0]);
            Month month = Month.valueOf(tokens[1].toUpperCase().trim());
            int year = Integer.parseInt(tokens[2]);
            String[] timeTokens = tokens[4].split(":");
            if (timeTokens.length == 2) {
                int hour = Integer.parseInt(timeTokens[0]);
                int minute = Integer.parseInt(timeTokens[1]);
                return LocalDateTime.of(year, month, day, hour, minute);
            }      
        }
        return null;
    }
    // get celeb info
    public Optional<CelebModel> searchCelebByName(String name) {
        if (listInitialized) {
            return Optional.ofNullable(celebCache.getOrDefault(name, null));
        } else {
            return celebsRepository.findByName(name);
        }
    }

    public Optional<CelebModel> searchCelebById(ObjectId id) {
        return celebsRepository.findById(id);
    }

    public Set<String> findAll() {
        if (!listInitialized) {
            for (CelebModel celeb : celebsRepository.findAll()) {
                celebCache.putIfAbsent(celeb.getName(), celeb);
            }
            
            listInitialized = true;   
        }
        return celebCache.keySet();
    }

}
