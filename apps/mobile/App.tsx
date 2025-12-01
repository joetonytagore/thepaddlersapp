import React, { useState } from 'react';
import { View, TextInput, Button, Text } from 'react-native';
import * as SecureStore from 'expo-secure-store';
import * as Notifications from 'expo-notifications';

const API_BASE = 'https://your-api.example.com/api';

async function login(email: string, password: string) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  const data = await res.json();
  await SecureStore.setItemAsync('accessToken', data.accessToken);
  await SecureStore.setItemAsync('refreshToken', data.refreshToken);
  return data;
}

async function registerDevice(userId: string) {
  const pushToken = (await Notifications.getExpoPushTokenAsync()).data;
  await fetch(`${API_BASE}/devices`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      deviceId: 'expo-device-id',
      platform: 'expo',
      pushToken,
      appVersion: '1.0.0',
      userId,
    }),
  });
}

async function createBooking(courtId: number, userId: string, startIso: string, endIso: string) {
  const accessToken = await SecureStore.getItemAsync('accessToken');
  await fetch(`${API_BASE}/bookings`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify({ courtId, userId, startIso, endIso }),
  });
}

export default function App() {
  const [email, setEmail] = useState('demo@paddlers.test');
  const [password, setPassword] = useState('testpass');
  const [userId, setUserId] = useState('');
  const [msg, setMsg] = useState('');

  return (
    <View style={{ padding: 16 }}>
      <TextInput placeholder="Email" value={email} onChangeText={setEmail} style={{borderWidth:1,marginBottom:8,padding:8}} />
      <TextInput placeholder="Password" value={password} onChangeText={setPassword} secureTextEntry style={{borderWidth:1,marginBottom:8,padding:8}} />
      <Button
        title="Login"
        onPress={async () => {
          const data = await login(email, password);
          setUserId(data.user.id);
          setMsg('Logged in!');
          await registerDevice(data.user.id);
        }}
      />
      <Button
        title="Create Booking"
        onPress={async () => {
          await createBooking(1, userId, new Date().toISOString(), new Date(Date.now() + 3600000).toISOString());
          setMsg('Booking created!');
        }}
      />
      <Text style={{marginTop:16}}>{msg}</Text>
    </View>
  );
}

