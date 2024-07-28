package com.voxloud.provisioning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.repository.DeviceRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
class ProvisioningServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private ProvisioningServiceImpl provisioningService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(provisioningService, "domain", "sip.voxloud.com");
        ReflectionTestUtils.setField(provisioningService, "port", "5060");
        ReflectionTestUtils.setField(provisioningService, "codecs", "G711,G729,OPUS");
    }


    @Test
    void provisioningFileGeneratedForDeskDevice() {
        Device device = new Device();
        device.setMacAddress("aa-bb-cc-dd-ee-ff");
        device.setModel(Device.DeviceModel.DESK);
        device.setUsername("john");
        device.setPassword("doe");

        when(deviceRepository.findById("aa-bb-cc-dd-ee-ff")).thenReturn(Optional.of(device));

        String result = provisioningService.getProvisioningFile("aa-bb-cc-dd-ee-ff");
        assertEquals("username=john\npassword=doe\ndomain=sip.voxloud.com\nport=5060\ncodecs=G711,G729,OPUS", result);
    }

    @Test
    void provisioningFileGeneratedForConferenceDevice() {
        Device device = new Device();
        device.setMacAddress("aa-bb-cc-11-22-33");
        device.setModel(Device.DeviceModel.CONFERENCE);
        device.setUsername("john");
        device.setPassword("doe");

        when(deviceRepository.findById("aa-bb-cc-11-22-33")).thenReturn(Optional.of(device));

        String result = provisioningService.getProvisioningFile("aa-bb-cc-11-22-33");
        assertEquals("{\n  \"username\" : \"john\",\n  \"password\" : \"doe\",\n  \"domain\" : \"sip.voxloud.com\",\n  \"port\" : \"5060\",\n  \"codecs\" : [\"G711\",\"G729\",\"OPUS\"]\n}", result);
    }

    @Test
    void provisioningFileGeneratedWithOverrideForDeskDevice() {
        Device device = new Device();
        device.setMacAddress("aa-bb-cc-11-22-33");
        device.setModel(Device.DeviceModel.DESK);
        device.setUsername("john");
        device.setPassword("doe");
        device.setOverrideFragment("domain=sip.anotherdomain.com\nport=5161\ntimeout=10");

        when(deviceRepository.findById("aa-bb-cc-11-22-33")).thenReturn(Optional.of(device));

        String result = provisioningService.getProvisioningFile("aa-bb-cc-11-22-33");
        assertEquals("username=john\npassword=doe\ndomain=sip.anotherdomain.com\nport=5161\ncodecs=G711,G729,OPUS\ntimeout=10", result);
    }

    @SneakyThrows
    @Test
    void provisioningFileGeneratedWithOverrideForConferenceDevice() {
        Device device = new Device();
        device.setMacAddress("aa-bb-cc-11-22-33");
        device.setModel(Device.DeviceModel.CONFERENCE);
        device.setUsername("john");
        device.setPassword("doe");
        device.setOverrideFragment("{\n  \"domain\" : \"sip.anotherdomain.com\",\n  \"port\" : \"5161\",\n  \"timeout\" : 10\n}");

        when(deviceRepository.findById("aa-bb-cc-11-22-33")).thenReturn(Optional.of(device));

        String result = provisioningService.getProvisioningFile("aa-bb-cc-11-22-33");
        JsonNode actualJson = new ObjectMapper().readTree(result);
        JsonNode expectedJson = new ObjectMapper().readTree("{\n  \"username\" : \"john\",\n  \"password\" : \"doe\",\n  \"domain\" : \"sip.anotherdomain.com\",\n  \"port\" : \"5161\",\n  \"codecs\" : [ \"G711\", \"G729\", \"OPUS\" ],\n  \"timeout\" : 10\n}");
        assertEquals(expectedJson, actualJson);    }

    @Test
    void deviceNotFoundThrowsException() {
        when(deviceRepository.findById("aa-bb-cc-11-22-33")).thenReturn(Optional.empty());

        DeviceNotFoundException exception = assertThrows(DeviceNotFoundException.class, () -> {
            provisioningService.getProvisioningFile("aa-bb-cc-11-22-33");
        });

        assertEquals("Device not found for MAC address: aa-bb-cc-11-22-33", exception.getMessage());
    }
}