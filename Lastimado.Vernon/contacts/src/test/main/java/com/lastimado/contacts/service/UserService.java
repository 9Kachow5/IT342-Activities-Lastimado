package com.lastimado.contacts.service;

import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;

public class PeopleService {
    ListConnectionsResponse response = PeopleService.people().connections().list("people/me")
            .setPersonFields("names,emailAddresses")
            .execute();
    List<Person> people = response.getConnections();
}
