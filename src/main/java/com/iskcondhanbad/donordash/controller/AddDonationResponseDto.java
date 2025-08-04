package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.model.Donation; // Add this import, adjust the package if needed

public class AddDonationResponseDto {
    private boolean alreadyExists;
    Donation donation;
    public AddDonationResponseDto(boolean alreadyExists, Donation donation) {
        this.alreadyExists = alreadyExists;
        this.donation = donation;
    }

    public boolean isAlreadyExists() {
        return alreadyExists;
    }

    public void setAlreadyExists(boolean alreadyExists) {
        this.alreadyExists = alreadyExists;
    }

    public Donation getDonation() {
        return donation;
    }

    public void setDonation(Donation donation) {
        this.donation = donation;
    }
}

