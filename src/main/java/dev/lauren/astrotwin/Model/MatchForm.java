package dev.lauren.astrotwin.Model;

import java.util.Map;

import lombok.Data;

@Data
public class MatchForm {
    private String userId;
    private Boolean isVarsModified;
    private Map<String, Double> vars;
}
