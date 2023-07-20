package dev.lauren.astrotwin.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import com.mongodb.client.AggregateIterable;
import dev.lauren.astrotwin.Model.MatchForm;
import dev.lauren.astrotwin.Model.ZodiacModel;
import dev.lauren.astrotwin.Repositories.CelebChartsRepository;
import dev.lauren.astrotwin.Repositories.CelebsRepository;
import dev.lauren.astrotwin.Repositories.UsersRepository;
import lombok.Data;

@Data
@Service
public class MatchService {
    
    // cache to store list of matches for id
    // gets emptied when variables change since that
    // will effect the computed matches
    // list is in sorted order
    private Map<String, List<Document>> matchListCache;
    
    @Autowired 
    private MongoTemplate mongoTemplate;
    @Autowired 
    private UsersRepository usersRepository;
    @Autowired 
    private CelebsRepository celebsRepository;
    @Autowired 
    private CelebChartsRepository celebChartsRepository;

    public MatchService() {
        matchListCache = new HashMap<>();
    }

    // caclulate match list  
    public List<Document> getMatchList(MatchForm payload) {
        String userId = payload.getUserId();
        

        //if (matchListCache.containsKey(userId)) {
        //    if (!payload.getIsVarsModified()) {
        //        System.out.println("from cache");
        //        return matchListCache.get(userId);
        //    } else {
        //        System.out.println("Calc new");
        //        matchListCache.remove(userId);
        //    }
        //}
        

        Double maxPoints = getMaxPoints(payload.getVars());                                  
        
        List<Document> request = Arrays.asList(new Document("$match", 
                                            new Document("_id", 
                                            new ObjectId(userId))), 
                                            new Document("$unwind", 
                                            new Document("path", "$chart")), 
                                            new Document("$addFields", 
                                            new Document("planetVal", 
                                            new Document("$first", 
                                            new Document("$filter", 
                                            new Document("input", Arrays.asList(new Document("_id", "Sun")
                                                                        .append("value", payload.getVars().get("Sun")), 
                                                                    new Document("_id", "Moon")
                                                                        .append("value", payload.getVars().get("Moon")), 
                                                                    new Document("_id", "Ascendant")
                                                                        .append("value", payload.getVars().get("Ascendant")), 
                                                                    new Document("_id", "Mars")
                                                                        .append("value", payload.getVars().get("Mars")), 
                                                                    new Document("_id", "Venus")
                                                                        .append("value", payload.getVars().get("Venus")), 
                                                                    new Document("_id", "Mercury")
                                                                        .append("value", payload.getVars().get("Mercury")), 
                                                                    new Document("_id", "Jupiter")
                                                                        .append("value", payload.getVars().get("Jupiter")), 
                                                                    new Document("_id", "Saturn")
                                                                        .append("value", payload.getVars().get("Saturn")), 
                                                                    new Document("_id", "Uranus")
                                                                        .append("value", payload.getVars().get("Uranus")), 
                                                                    new Document("_id", "Neptune")
                                                                        .append("value", payload.getVars().get("Neptune")), 
                                                                    new Document("_id", "Pluto")
                                                                        .append("value", payload.getVars().get("Pluto"))))
                                                                .append("as", "planetVals")
                                                                .append("cond", 
                                            new Document("$eq", Arrays.asList("$chart.planet", "$$planetVals._id")))
                                                                .append("limit", 1L))))), 
                                            new Document("$lookup", 
                                            new Document("from", "celeb_charts")
                                                    .append("let", 
                                            new Document("userPlanet", "$chart.planet"))
                                                    .append("pipeline", Arrays.asList(new Document("$unwind", "$chart"), 
                                                        new Document("$match", 
                                                        new Document("$expr", 
                                                        new Document("$eq", Arrays.asList("$$userPlanet", "$chart.planet"))))))
                                                    .append("as", "result")), 
                                            new Document("$unwind", 
                                            new Document("path", "$result")), 
                                            new Document("$project", 
                                            new Document("planetVal", "$planetVal.value")
                                                    .append("celebID", "$result._id")
                                                    .append("zodiacScore", 
                                            new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$chart.zodiac", "$result.chart.zodiac")), payload.getVars().get("Zodiac"), 0L)))
                                                    .append("elementScore", 
                                            new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$chart.element", "$result.chart.element")), payload.getVars().get("Element"), 0L)))
                                                    .append("modeScore", 
                                            new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$chart.mode", "$result.chart.mode")), payload.getVars().get("Mode"), 0L)))
                                                    .append("houseScore", 
                                            new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$chart.house", "$result.chart.house")), payload.getVars().get("House"), 1L)))), 
                                            new Document("$group", 
                                            new Document("_id", "$celebID")
                                                    .append("score", 
                                            new Document("$sum", 
                                            new Document("$multiply", Arrays.asList("$planetVal", 
                                                                new Document("$max", Arrays.asList("$zodiacScore", "$elementScore", "$modeScore")), "$houseScore"))))), 
                                                                
                                            new Document("$lookup", 
                                                    new Document("from", "celebs")
                                                        .append("localField", "_id")
                                                        .append("foreignField", "celebChart")
                                                        .append("as", "celeb")), 
                                            new Document("$project", 
                                                    new Document("celeb", 
                                                    new Document("$first", "$celeb"))
                                                            .append("chartId", "$_id")
                                                            .append("percent", 
                                                        new Document("$multiply", Arrays.asList(new Document("$divide", Arrays.asList("$score", maxPoints)), 100L)))),
                                            new Document("$sort", new Document("percent", -1L)));




        AggregateIterable<Document> result = mongoTemplate.getCollection("user_charts").aggregate(request);

        List<Document> resultList = new ArrayList<>();
        for (Document d : result) { 
            resultList.add(d);
        }  
        matchListCache.put(userId, resultList);
        
        return resultList;
    }

    private double getMaxPoints(Map<String, Double> vars) {
        
        double maxMatch = Math.max(Math.max(vars.get("Element"), vars.get("Mode")), vars.get("Zodiac"));
        double res = 0;
        
        for (String planet : ZodiacModel.planets) {
            res += vars.get(planet);
        }
        res *= (maxMatch * vars.get("House"));
        return res;
    }
      
}

