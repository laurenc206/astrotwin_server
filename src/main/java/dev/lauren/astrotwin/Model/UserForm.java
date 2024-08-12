package dev.lauren.astrotwin.Model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserForm {
    String name;
   
    LocalDateTime birthday;
    LocalDateTime birthday_UTC;
   // LocalDateTime date;
    Location location;
    
    @Data
    public class Location {
        String text;
        String lat;
        String lng;
    }
}
