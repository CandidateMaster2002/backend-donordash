package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.NityaSevaDonorStatusDTO;
import com.iskcondhanbad.donordash.service.NityaSevaStatusService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nitya-seva")
public class NityaSevaStatusController {


@Autowired
NityaSevaStatusService nityaSevaStatusService;


     @GetMapping("/status")
    public List<NityaSevaDonorStatusDTO> getNityaSevaStatus(
        @RequestParam(value = "cultivatorId", required = false) Integer cultivatorId) {
        return nityaSevaStatusService.getNityaSevaStatus(cultivatorId);
    }
   
}