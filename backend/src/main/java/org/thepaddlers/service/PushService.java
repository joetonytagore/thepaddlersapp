package org.thepaddlers.service;

import org.springframework.stereotype.Service;
import org.thepaddlers.model.Device;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class PushService {
    public void sendPush(Device device, String title, String body) {
        if (device.getPlatform().equalsIgnoreCase("android")) {
            // FCM push
            Message msg = Message.builder()
                .setToken(device.getPushToken())
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
            try {
                String response = FirebaseMessaging.getInstance().send(msg);
                // log response or handle errors
            } catch (Exception e) {
                // log error
            }
        } else if (device.getPlatform().equalsIgnoreCase("ios")) {
            // APNs push (using FCM for APNs or direct APNs integration)
            Message msg = Message.builder()
                .setToken(device.getPushToken())
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .setApnsConfig(
                    com.google.firebase.messaging.ApnsConfig.builder()
                        .setAps(
                            com.google.firebase.messaging.Aps.builder().setAlert(
                                com.google.firebase.messaging.ApsAlert.builder().setTitle(title).setBody(body).build()
                            ).build()
                        ).build()
                )
                .build();
            try {
                String response = FirebaseMessaging.getInstance().send(msg);
                // log response or handle errors
            } catch (Exception e) {
                // log error
            }
        }
    }
}
