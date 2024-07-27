package com.voxloud.provisioning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProvisioningServiceImpl implements ProvisioningService {

    private static final Logger logger = LoggerFactory.getLogger(ProvisioningServiceImpl.class);

    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${provisioning.domain}")
    private String domain;

    @Value("${provisioning.port}")
    private String port;

    @Value("${provisioning.codecs}")
    private String codecs;

    public ProvisioningServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public String getProvisioningFile(String macAddress) {
        logger.debug("Fetching provisioning file for MAC address: {}", macAddress);
        Optional<Device> optionalDevice = deviceRepository.findById(macAddress);

        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();
            logger.debug("Device found: {}", device);
            return generateProvisioningFile(device);
        } else {
            logger.warn("Device not found for MAC address: {}", macAddress);
            throw new DeviceNotFoundException("Device not found for MAC address: " + macAddress);
        }
    }

    private String generateProvisioningFile(Device device) {
        logger.debug("Generating provisioning file for device: {}", device);
        String config;

        if (device.getModel() == Device.DeviceModel.DESK) {
            config = String.format("username=%s\npassword=%s\ndomain=%s\nport=%s\ncodecs=%s",
                    device.getUsername(), device.getPassword(), domain, port, codecs);
            logger.debug("Generated config for DESK model: {}", config);
        } else if (device.getModel() == Device.DeviceModel.CONFERENCE) {
            config = String.format("{\n  \"username\" : \"%s\",\n  \"password\" : \"%s\",\n  \"domain\" : \"%s\",\n  \"port\" : \"%s\",\n  \"codecs\" : [%s]\n}",
                    device.getUsername(), device.getPassword(), domain, port, formatCodecsAsJsonArray());
            logger.debug("Generated config for CONFERENCE model: {}", config);
        } else {
            logger.error("Unsupported device model: {}", device.getModel());
            throw new IllegalArgumentException("Unsupported device model: " + device.getModel());
        }

        if (device.getOverrideFragment() != null) {
            logger.debug("Applying override fragment: {}", device.getOverrideFragment());
            config = applyOverrideFragment(config, device.getOverrideFragment(), device.getModel());
            logger.debug("Config after applying override fragment: {}", config);
        }

        return config;
    }

    private String formatCodecsAsJsonArray() {
        String[] codecsArray = codecs.split(",");
        StringBuilder jsonArray = new StringBuilder();
        for (int i = 0; i < codecsArray.length; i++) {
            jsonArray.append("\"").append(codecsArray[i]).append("\"");
            if (i < codecsArray.length - 1) {
                jsonArray.append(",");
            }
        }
        return jsonArray.toString();
    }

    private String applyOverrideFragment(String config, String overrideFragment, Device.DeviceModel model) {
        try {
            if (model == Device.DeviceModel.DESK) {
                String[] lines = overrideFragment.split("\n");
                StringBuilder configBuilder = new StringBuilder(config);
                for (String line : lines) {
                    String[] keyValue = line.split("=");
                    String key = keyValue[0];
                    String value = keyValue[1];

                    if (configBuilder.toString().contains(key)) {
                        configBuilder = new StringBuilder(configBuilder.toString().replaceAll(key + "=[^\\n]*", key + "=" + value));
                    } else {
                        configBuilder.append("\n").append(key).append("=").append(value);
                    }
                }
                config = configBuilder.toString();
            } else if (model == Device.DeviceModel.CONFERENCE) {
                config = mergeJsonFragments(config, overrideFragment);
            }
        } catch (IOException e) {
            logger.error("Failed to apply override fragment", e);
            throw new RuntimeException("Failed to apply override fragment", e);
        }
        return config;
    }

    private String mergeJsonFragments(String originalJson, String overrideJson) throws IOException {
        JsonNode originalNode = objectMapper.readTree(originalJson);
        JsonNode overrideNode = objectMapper.readTree(overrideJson);

        ((ObjectNode) originalNode).setAll((ObjectNode) overrideNode);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(originalNode);
    }
}