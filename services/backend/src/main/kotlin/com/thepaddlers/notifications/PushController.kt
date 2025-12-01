package com.thepaddlers.notifications

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/mobile/push")
class PushController(
    private val notificationService: NotificationService
) {
    @PostMapping("/register")
    fun registerDevice(@RequestBody req: RegisterDeviceRequest): ResponseEntity<Void> {
        notificationService.registerDevice(req)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{deviceId}")
    fun unregisterDevice(@PathVariable deviceId: String): ResponseEntity<Void> {
        notificationService.unregisterDevice(deviceId)
        return ResponseEntity.ok().build()
    }
}

data class RegisterDeviceRequest(
    val deviceId: String,
    val userId: UUID,
    val platform: String,
    val pushToken: String
)

