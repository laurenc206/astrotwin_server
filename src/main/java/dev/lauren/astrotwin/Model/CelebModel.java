package dev.lauren.astrotwin.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;


@Document(collection = "celebs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CelebModel {
    @Id
    private ObjectId id;
    private String name;
    private LocalDateTime bday;
    private AtlasModel blocation;
    @DocumentReference
    private CelebChartModel celebChart;
    private String imageUrl;
    

    public CelebModel(String name, LocalDateTime birthDateTime, String town, String country) {
        this.name = name;
        this.bday = birthDateTime;
        this.blocation = new AtlasModel();
        blocation.setTown(town);
        blocation.setCountry(country);
    }

    public CelebModel(String name, LocalDateTime birthDateTime, AtlasModel blocation) {
        this.name = name;
        this.bday = birthDateTime;
        this.blocation = blocation;
    }
}
