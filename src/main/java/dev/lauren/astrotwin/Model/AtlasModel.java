package dev.lauren.astrotwin.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class AtlasModel {
    private String longitude;
    private String latitude;
    private String zone;
    private String town;
    private String country;
}
