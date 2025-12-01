package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.Device;

public interface DeviceRepository extends JpaRepository<Device, String> {
    void deleteByDeviceId(String deviceId);
}

