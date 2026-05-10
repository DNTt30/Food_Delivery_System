package com.duong.salesmanagement.service;

import com.duong.salesmanagement.dto.ContactInfoResponse;
import org.springframework.stereotype.Service;

/**
 * Thin adapter kept only for backward-compatibility.
 * All logic lives in {@link ContactService}.
 */
@Service
public class OrderContactService {

    private final ContactService contactService;

    public OrderContactService(ContactService contactService) {
        this.contactService = contactService;
    }

    /** @see ContactService#getContactInfo(Long, String) */
    public ContactInfoResponse getContactInfo(Long orderId, String currentUsername) {
        return contactService.getContactInfo(orderId, currentUsername);
    }
}
