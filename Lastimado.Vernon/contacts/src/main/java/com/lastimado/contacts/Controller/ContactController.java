package com.lastimado.contacts.Controller;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.lastimado.contacts.Service.CredentialService;

import java.util.List;

@Controller
public class ContactController {

    private final CredentialService credentialService;

    public ContactController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping("/contacts")
    public String createContact(@RequestParam String givenName,
                                @RequestParam String familyName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phoneNumber,
                                OAuth2AuthenticationToken authentication) throws Exception {

        Credential credential = credentialService.getGoogleCredential(authentication);

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Lastimado OAuth2").build();

        Person contactToCreate = new Person();

        contactToCreate.setNames(List.of(new Name().setGivenName(givenName).setFamilyName(familyName)));

        if (email != null && !email.isEmpty()) {
            contactToCreate.setEmailAddresses(List.of(new EmailAddress().setValue(email)));
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            contactToCreate.setPhoneNumbers(List.of(new PhoneNumber().setValue(phoneNumber)));
        }

        peopleService.people().createContact(contactToCreate).execute();

        return "redirect:/contacts";
    }

    @GetMapping("/contacts")
    public String showContacts(OAuth2AuthenticationToken authentication, Model model) throws Exception {
        Credential credential = credentialService.getGoogleCredential(authentication);

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Lastimado OAuth2").build();

        ListConnectionsResponse response = peopleService.people().connections().list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        model.addAttribute("contacts", response.getConnections());
        return "contacts";
    }

    @GetMapping("/add-contact")
    public String showAddContactForm() {
        return "Create";
    }

    @GetMapping("/Edit")
    public String editContact(@RequestParam String resourceName, Model model, OAuth2AuthenticationToken authentication) throws Exception {
        Credential credential = credentialService.getGoogleCredential(authentication);

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Lastimado OAuth2").build();

        Person contact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        model.addAttribute("contact", contact);
        return "Edit";  // Shows the edit page
    }

    @PostMapping("/update-contact")
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            OAuth2AuthenticationToken authentication) throws Exception {

        Credential credential = credentialService.getGoogleCredential(authentication);
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Lastimado OAuth2").build();

        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        existingContact.setNames(List.of(new Name().setGivenName(givenName).setFamilyName(familyName)));

        if (email != null && !email.isEmpty()) {
            existingContact.setEmailAddresses(List.of(new EmailAddress().setValue(email)));
        } else {
            existingContact.setEmailAddresses(null);
        }

        if (phone != null && !phone.isEmpty()) {
            existingContact.setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));
        } else {
            existingContact.setPhoneNumbers(null);
        }

        peopleService.people().updateContact(resourceName, existingContact)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        return "redirect:/contacts";
    }


    @PostMapping("/delete-contact")
    public String deleteContact(@RequestParam String resourceName, OAuth2AuthenticationToken authentication) throws Exception {
        Credential credential = credentialService.getGoogleCredential(authentication);

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Lastimado OAuth2").build();

        peopleService.people().deleteContact(resourceName).execute();

        return "redirect:/contacts";  // Go back to contact list
    }
}
