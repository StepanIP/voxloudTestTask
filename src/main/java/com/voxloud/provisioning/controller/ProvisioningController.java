package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.service.ProvisioningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1")
public class ProvisioningController {

    private final ProvisioningService provisioningService;
    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");

    public ProvisioningController(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @GetMapping("/provisioning/{macAddress}")
    public ResponseEntity<String> provision(@PathVariable String macAddress) {
        if (!MAC_ADDRESS_PATTERN.matcher(macAddress).matches()) {
            return ResponseEntity.badRequest().body("Invalid MAC address format");
        }
        try {
            String config = provisioningService.getProvisioningFile(macAddress);
            return ResponseEntity.ok(config);
        } catch (DeviceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}