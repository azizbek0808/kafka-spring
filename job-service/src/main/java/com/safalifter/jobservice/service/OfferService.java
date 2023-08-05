package com.safalifter.jobservice.service;

import com.safalifter.jobservice.client.NotificationServiceClient;
import com.safalifter.jobservice.client.UserServiceClient;
import com.safalifter.jobservice.enums.OfferStatus;
import com.safalifter.jobservice.exc.NotFoundException;
import com.safalifter.jobservice.model.Advert;
import com.safalifter.jobservice.model.Offer;
import com.safalifter.jobservice.repository.OfferRepository;
import com.safalifter.jobservice.request.notification.SendNotificationRequest;
import com.safalifter.jobservice.request.offer.MakeAnOfferRequest;
import com.safalifter.jobservice.request.offer.OfferUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferService {
    private final OfferRepository offerRepository;
    private final AdvertService advertService;
    private final UserServiceClient userServiceclient;
    private final NotificationServiceClient notificationServiceClient;

    public Offer makeAnOffer(MakeAnOfferRequest request) {
        String userId = isTheUserRegistered(request.getUserId());
        Advert advert = advertService.getAdvertById(request.getAdvertId());
        Offer toSave = Offer.builder()
                .userId(userId)
                .advert(advert)
                .offeredPrice(request.getOfferedPrice())
                .status(OfferStatus.OPEN).build();
        offerRepository.save(toSave);
        notificationServiceClient.sendNotification(SendNotificationRequest.builder()
                .message("You have received an offer for your advertising.")
                .userId(advert.getUserId())
                .offerId(toSave.getId()).build());
        return toSave;
    }

    public Offer getOfferById(String id) {
        return findOfferById(id);
    }

    public List<Offer> getOffersByAdvertId(String id) {
        Advert advert = advertService.getAdvertById(id);
        return offerRepository.getOffersByAdvertId(advert.getId());
    }

    public List<Offer> getOffersByUserId(String id) {
        String userId = isTheUserRegistered(id);
        return offerRepository.getOffersByUserId(userId);
    }

    public Offer updateOfferById(OfferUpdateRequest request) {
        Offer toUpdate = findOfferById(request.getId());
        toUpdate.setOfferedPrice(Optional.of(request.getOfferedPrice()).orElse(toUpdate.getOfferedPrice()));
        toUpdate.setStatus(Optional.ofNullable(request.getStatus()).orElse(toUpdate.getStatus()));
        return offerRepository.save(toUpdate);
    }

    public void deleteOfferById(String id) {
        offerRepository.deleteById(id);
    }

    protected Offer findOfferById(String id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer not found"));
    }

    protected String isTheUserRegistered(String id) {
        return Objects.requireNonNull(userServiceclient.getUserById(id).getBody()).getId();
    }
}