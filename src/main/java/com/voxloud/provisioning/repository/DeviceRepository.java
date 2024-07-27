package com.voxloud.provisioning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<com.voxloud.provisioning.entity.Device, String> {
}
