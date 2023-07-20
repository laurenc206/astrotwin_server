package dev.lauren.astrotwin.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchModel {
   String userId;
   String celebName;
   String celebChartId;
   double percentMatch;
}
