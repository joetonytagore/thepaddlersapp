package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.model.Device;
import org.thepaddlers.model.User;
import org.thepaddlers.repository.DeviceRepository;
import org.thepaddlers.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceController(DeviceRepository deviceRepository, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> registerDevice(@RequestBody Map<String, String> body) {
        String deviceId = body.get("deviceId");
        String platform = body.get("platform");
        String pushToken = body.get("pushToken");
        String appVersion = body.get("appVersion");
        String userIdStr = body.get("userId");
        if (deviceId == null || platform == null || pushToken == null || appVersion == null || userIdStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }
        Long userId = Long.valueOf(userIdStr);
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setPlatform(platform);
        device.setPushToken(pushToken);
        device.setAppVersion(appVersion);
        device.setUser(userOpt.get());
        deviceRepository.save(device);
        return ResponseEntity.ok(Map.of("status", "registered"));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> unregisterDevice(@PathVariable String deviceId) {
        deviceRepository.deleteByDeviceId(deviceId);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}

