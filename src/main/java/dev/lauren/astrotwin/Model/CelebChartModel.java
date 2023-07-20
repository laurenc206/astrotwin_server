package dev.lauren.astrotwin.Model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "celeb_charts")
@AllArgsConstructor
@NoArgsConstructor
public class CelebChartModel {
    @Id
    private ObjectId id;
    List<ChartNode> chart;


    public CelebChartModel(List<ChartNode> chart) {
        
        this.chart = chart;
    }
}
