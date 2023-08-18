package dev.lauren.astrotwin.Model;

import lombok.Data;

@Data
public class ContactForm {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String message;
}
