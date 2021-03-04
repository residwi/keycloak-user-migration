package com.danielfrak.code.keycloak.providers.rest.kafka.model;

import java.util.UUID;

public class UserProfileDto {

    private String firstName;

    private String lastName;

    private UUID userId;

    private String phoneNumber;

    private String birthDate;

    private String address;

    private String city;

    private String photoPath;

    private String linkedinUrl;

    private String zipCode;

    private String cvPath;

    private String profession;

    private String lastEducationPlace;

    private boolean isSubscribeNewsletter;

    private boolean darkMode;

    private String referralCode;

    private String referredBy;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCvPath() {
        return cvPath;
    }

    public void setCvPath(String cvPath) {
        this.cvPath = cvPath;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getLastEducationPlace() {
        return lastEducationPlace;
    }

    public void setLastEducationPlace(String lastEducationPlace) {
        this.lastEducationPlace = lastEducationPlace;
    }

    public boolean isSubscribeNewsletter() {
        return isSubscribeNewsletter;
    }

    public void setSubscribeNewsletter(boolean subscribeNewsletter) {
        isSubscribeNewsletter = subscribeNewsletter;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }
}
