package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.service.ProvisioningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ProvisioningController {

    private final ProvisioningService provisioningService;

    public ProvisioningController(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @GetMapping("/provisioning/{macAddress}")
    public ResponseEntity<String> provision(@PathVariable String macAddress) {
        try {
            String config = provisioningService.getProvisioningFile(macAddress);
            return ResponseEntity.ok(config);
        } catch (DeviceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}