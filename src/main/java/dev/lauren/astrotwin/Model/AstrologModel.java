package dev.lauren.astrotwin.Model;
import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Data
@Component
public class AstrologModel {
    private UserModel user;
    private UserChartModel chart;
    //String name;
    //LocalDateTime bday;
    //String blocation;
    private Map<String, String> signMap;
    private Map<String, Integer> houseMap;
    private List<List<String>> modePercent;
    private List<List<String>> elementPercent;
    private List<List<String>> zodiacPercent;
    private List<List<String>> planetPercent;

    public AstrologModel() {
        signMap = new HashMap<>();
        houseMap = new HashMap<>();
        modePercent = new ArrayList<>();
        elementPercent = new ArrayList<>();
        zodiacPercent = new ArrayList<>();
        planetPercent = new ArrayList<>();
    }
}

