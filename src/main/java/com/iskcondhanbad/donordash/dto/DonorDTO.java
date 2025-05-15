package com.iskcondhanbad.donordash.dto;

import lombok.Data;

@Data
public class DonorDTO {
    private Integer id;
    private String name;
    private String username;
    private String category;
    private String photoPath;

     public DonorDTO(Integer id, String name, String username, String category, String photoPath) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.category = category;
        this.photoPath = photoPath;
    }
}
