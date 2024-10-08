package dev.lauren.astrotwin.Controller;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.jsoup.Jsoup;
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

import dev.lauren.astrotwin.Model.ContactForm;
import dev.lauren.astrotwin.Model.MatchForm;
import dev.lauren.astrotwin.Model.UserForm;
import dev.lauren.astrotwin.Model.UserModel;
import dev.lauren.astrotwin.Service.EmailService;
import dev.lauren.astrotwin.Service.MatchService;
import dev.lauren.astrotwin.Service.UserService;


@RestController
@RequestMapping("/api/v1/user")
//@CrossOrigin(origins = "http://3.94.188.92:3000")
@CrossOrigin(origins = "http://localhost:3000")
//@CrossOrigin(origins = "http://astrotwin.net:3000")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private MatchService matchService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private Environment env;




    @PostMapping
    public ResponseEntity<UserModel> insertUser(@RequestBody UserForm payload) {
        System.out.println("insertUser");
        System.out.println(payload.toString());
        String astrologFilePath = env.getProperty("ASTROLOG_FPATH");
        return new ResponseEntity<>(userService.createUser(payload, astrologFilePath), HttpStatus.CREATED);
    }

    @GetMapping("/getUser")
    public ResponseEntity<Optional<UserModel>> getUser(@RequestParam String query) {
        String userId = query;
        return new ResponseEntity<>(userService.searchUserById(userId), HttpStatus.OK);
    }

    @PostMapping("/getMatchList")
    public ResponseEntity<List<Document>> getMatchList(@RequestBody MatchForm payload) {

        return new ResponseEntity<>(matchService.getMatchList(payload), HttpStatus.OK);
    }

    @GetMapping("/getCities/{searchStr}")
    public ResponseEntity<String> getCities(@PathVariable String searchStr) {
        System.out.println(searchStr);
        try {
            StringBuilder url = new StringBuilder();
            //http://api.geonames.org/searchJSON?name_startsWith=
            url.append("http://api.geonames.org/searchJSON?name_startsWith=");
            url.append(URLEncoder.encode(searchStr, "UTF-8"));
            url.append("&maxRows=10&username=");
            url.append(env.getProperty("CITIES_USERNAME"));
            System.out.println("URL: " + url.toString());
            String response = Jsoup.connect(url.toString()).ignoreContentType(true).execute().body();
            System.out.println(response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/contactMe")
    public ResponseEntity<String> sendFeedback(@RequestBody ContactForm payload) {
        String response = emailService.sendFeedback(payload);
        return new ResponseEntity<>(response, response.equals("Success") ? HttpStatus.OK : HttpStatus.BAD_GATEWAY);
    }
  
}
