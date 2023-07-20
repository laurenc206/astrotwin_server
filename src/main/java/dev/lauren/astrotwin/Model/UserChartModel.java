package dev.lauren.astrotwin.Model;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Document(collection = "user_charts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChartModel {
    @Id
    private ObjectId id;
    List<ChartNode> chart;

    public UserChartModel(List<ChartNode> chart) {
        this.chart = chart;
    }
}