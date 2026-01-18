package com.iskcondhanbad.donordash.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.iskcondhanbad.donordash.dto.CreateDonorRequest;

class Donation {
  int id;
  String status;
  DonorCultivator donorCultivator;

  public Donation(int id, String status, DonorCultivator donorCultivator) {
    this.id = id;
    this.status = status;
    this.donorCultivator = donorCultivator;
  }
}

class DonorCultivator {
  String shortName;
  int donationsVerified;

  public DonorCultivator(String shortName, int donationsVerified) {
    this.shortName = shortName;
    this.donationsVerified = donationsVerified;
  }
}

@Service
public class Services {
  private static List<Donation> donations = new ArrayList<>();

  static {
    donations.add(new Donation(1, "verified", new DonorCultivator("ABC", 1)));
    donations.add(new Donation(2, "pending", new DonorCultivator("DEF", 0)));
  }

  public String generateUsername(CreateDonorRequest createDonorRequest, Integer id) {
    String namePart = createDonorRequest.getName().length() > 15 ? createDonorRequest.getName().substring(0, 15) : createDonorRequest.getName();
    String addressPart = createDonorRequest.getAddress().length() > 15 ? createDonorRequest.getAddress().substring(0, 15) : createDonorRequest.getAddress();
    return id + "_" + namePart + "_" + addressPart;
  }

  
}