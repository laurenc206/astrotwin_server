package dev.lauren.astrotwin.Model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserForm {
    String name;
    LocalDateTime date;
    Location location;

    @Data
    public class Location {
        String town;
        String region;
        String country;
        String code;
    }

}
