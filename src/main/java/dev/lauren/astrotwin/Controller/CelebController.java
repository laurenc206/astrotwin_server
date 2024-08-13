package dev.lauren.astrotwin.Controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.lauren.astrotwin.Model.CelebModel;
import dev.lauren.astrotwin.Model.SearchModel;
import dev.lauren.astrotwin.Service.CelebService;

@RestController
@RequestMapping("/api/v1/celeb")
//@CrossOrigin(origins = "http://3.94.188.92:3000")
@CrossOrigin(origins = "http://localhost:3000")
//@CrossOrigin(origins = "http://astrotwin.net:3000")
public class CelebController {
    @Autowired
    private CelebService celebService;
    @Autowired
    private Environment env;
    

    @PostMapping("/insertCeleb")
    public ResponseEntity<CelebModel> insertCeleb(@RequestBody Map<String, String> payload) {
        System.out.println("insert celeb");
        String astrologFilePath = env.getProperty("ASTROLOG_FPATH");
        
        try {
            CelebModel response = celebService.insertCeleb(payload, astrologFilePath);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/insertCelebByName")
    public ResponseEntity<CelebModel> insertCelebByName(@RequestBody SearchModel payload) {
        String name = payload.getQuery();
        System.out.println("insert celeb by name");
        Map<String,String> insertPayload= new HashMap<>();
        insertPayload.put("name", name);
        insertPayload.put("type", "name");
        String astrologFilePath = env.getProperty("ASTROLOG_FPATH");
        try {
            CelebModel response = celebService.insertCeleb(insertPayload, astrologFilePath);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } 
    }

    @PostMapping("/insertCeleb/datacrawl") 
    public void datacrawl() {
        Random rand = new Random();
        List<String> names = new ArrayList<>();
        final String url = "https://www.telltalesonline.com/26925/popular-celebs/";
        String astrologFilePath = env.getProperty("ASTROLOG_FPATH");
        try {
            // get list of names
            Document response = Jsoup.connect(url).userAgent("Mozilla").get();
            Elements nameElements = response.select("div[data-id=\"19057e6e\"] h2");
            for (Element e : nameElements) {
                String name = e.text().replaceAll("^[\\d]+.\\s", "");
                names.add(name);
            }

            for (String name: names) {
                Map<String,String> payload= new HashMap<>();
                payload.put("name", name);
                payload.put("type", "name");
                try {
                    CelebModel celebResponse = celebService.insertCeleb(payload, astrologFilePath);
                    System.out.println(celebResponse.getName() + " added");
                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println(name + " discarded");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Optional<CelebModel>> searchCelebByName(@RequestParam String query) {
        String name = query;
        Optional<CelebModel> response = celebService.searchCelebByName(name);
        
        return new ResponseEntity<Optional<CelebModel>>(response, response.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/search/findAll")
    public ResponseEntity<Set<String>> findAll(){
           
        return new ResponseEntity<>(celebService.findAll(), HttpStatus.OK);
    }
    
    //@GetMapping("/getChartById/{id}")
    //public ResponseEntity<Optional<CelebChartModel>> getCelebChartById(@PathVariable ObjectId id) {
    //    return new ResponseEntity<>(celebService.getCelebChartById(id), HttpStatus.OK);
    //}

    //@GetMapping("/getChartByName/{name}")
    //public ResponseEntity<Optional<CelebChartModel>> getCelebChartById(@PathVariable String name) {
    //    return new ResponseEntity<>(celebService.getCelebChartByName(name), HttpStatus.OK);
    //}

    
}