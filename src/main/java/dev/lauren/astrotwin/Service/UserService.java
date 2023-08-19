package dev.lauren.astrotwin.Service;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import dev.lauren.astrotwin.Model.AstrologModel;
import dev.lauren.astrotwin.Model.ChartNode;
import dev.lauren.astrotwin.Model.UserChartModel;
import dev.lauren.astrotwin.Model.UserForm;
import dev.lauren.astrotwin.Model.UserModel;
import dev.lauren.astrotwin.Model.ZodiacModel;
import dev.lauren.astrotwin.Repositories.UserChartsRepository;
import dev.lauren.astrotwin.Repositories.UsersRepository;

@Service
public class UserService {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UserChartsRepository userChartsRepository;
    //Map<UserModel, UserChartModel> userCache; 

    public UserService() {
        //userCache = new HashMap<>();
    }
    
    public UserModel createUser(UserForm userData, String AstrologFilePath) {
        try {
            System.out.println(userData.toString());
            UserModel user = new UserModel();
            user.setUserData(userData);
            
            AstrologModel userChart = ChartService.calculateChart(user, AstrologFilePath);
            
            List<ChartNode> chart = new ArrayList<>();
            
            for (Map.Entry<String,String> placement : userChart.getSignMap().entrySet()) {
                String planet = placement.getKey();
                String zodiac = placement.getValue();
                ChartNode chartNode = new ChartNode(planet,
                                                    zodiac,
                                                    ZodiacModel.getElement(zodiac),
                                                    ZodiacModel.getMode(zodiac),
                                                    userChart.getHouseMap().get(planet));

                chart.add(chartNode); 
            }
            
            UserChartModel userChartModel = userChartsRepository.insert(new UserChartModel(chart));
            user.setUserChart(userChartModel);

            user.setChartId(userChartModel.getId().toHexString());

            // set formatted date, time and location strings for frontend
            DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("h:mm a");
            user.setDayStr(dateformatter.format(user.getUserData().getDate()));
            user.setTimeStr(timeformatter.format(user.getUserData().getDate()));
            String locStr = userData.getLocation().getTown() 
                            + ", " + userData.getLocation().getRegion() 
                            + ", " + userData.getLocation().getCountry();
            user.setLocationStr(locStr);
            UserModel userDB = usersRepository.insert(user);
              
            return userDB;
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Optional<UserModel> searchUserById(String id) {
        return usersRepository.findByChartId(id);
    }
    
}
