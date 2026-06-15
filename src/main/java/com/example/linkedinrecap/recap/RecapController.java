package com.example.linkedinrecap.recap;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recaps")
public class RecapController {

    private final RecapService recapService;

    public RecapController(RecapService recapService) {
        this.recapService = recapService;
    }

    @PostMapping("/generate")
    GenerateRecapResponse generate(@Valid @RequestBody GenerateRecapRequest request) {
        return recapService.generate(request);
    }
}
