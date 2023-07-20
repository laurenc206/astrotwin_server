package dev.lauren.astrotwin.Model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class UserModel {
    @Id
    private ObjectId id;
    private UserForm userData;
    @DocumentReference 
    private UserChartModel userChart;

    // reference to user and chart for matching for front end
    private String chartId;
    // strings for front end chart displaying
    private String dayStr;
    private String timeStr;
    private String locationStr;
}
