package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.model.StoredDonation; // Add this import, adjust the package if needed

public class AddDonationResponseDto {
    private boolean alreadyExists;
    StoredDonation donation;
    public AddDonationResponseDto(boolean alreadyExists, StoredDonation donation) {
        this.alreadyExists = alreadyExists;
        this.donation = donation;
    }

    public boolean isAlreadyExists() {
        return alreadyExists;
    }

    public void setAlreadyExists(boolean alreadyExists) {
        this.alreadyExists = alreadyExists;
    }

    public StoredDonation getDonation() {
        return donation;
    }

    public void setDonation(StoredDonation donation) {
        this.donation = donation;
    }
}

