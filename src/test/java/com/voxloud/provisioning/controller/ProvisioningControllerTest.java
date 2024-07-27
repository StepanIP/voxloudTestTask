package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.controller.ProvisioningController;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.service.ProvisioningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(ProvisioningController.class)
public class ProvisioningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProvisioningService provisioningService;

    @Test
    public void provision_DeviceNotFound() throws Exception {
        when(provisioningService.getProvisioningFile("aa-bb-cc-11-22-33")).thenThrow(new DeviceNotFoundException("Device not found"));

        mockMvc.perform(get("/api/v1/provisioning/aa-bb-cc-11-22-33"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Device not found"));
    }

    @Test
    public void provision_DeviceFound() throws Exception {
        when(provisioningService.getProvisioningFile("aa-bb-cc-11-22-33")).thenReturn("config");

        mockMvc.perform(get("/api/v1/provisioning/aa-bb-cc-11-22-33"))
                .andExpect(status().isOk())
                .andExpect(content().string("config"));
    }

    @Test
    public void provision_InvalidMacAddressFormat() throws Exception {
        mockMvc.perform(get("/api/v1/provisioning/invalid-mac"))
                .andExpect(status().isBadRequest());
    }
}