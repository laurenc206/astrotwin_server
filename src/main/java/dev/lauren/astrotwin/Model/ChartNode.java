package dev.lauren.astrotwin.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartNode {
    private String planet;
    private String zodiac;
    private String element;
    private String mode;
    private int house;
}
