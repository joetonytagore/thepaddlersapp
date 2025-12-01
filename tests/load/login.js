import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 50,
  duration: '60s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(50)<200'],
  },
};

const users = Array.from({ length: 50 }, (_, i) => ({ email: `user${i}@test.com`, password: 'testpass' }));

export default function () {
  const user = users[Math.floor(Math.random() * users.length)];
  const loginRes = http.post('http://localhost:8080/auth/login', JSON.stringify(user), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(loginRes, {
    'login status 200 or 401': r => r.status === 200 || r.status === 401,
    'login latency < 200ms': r => r.timings.duration < 200,
  });
  if (loginRes.status === 200) {
    const { refreshToken } = loginRes.json();
    const refreshRes = http.post('http://localhost:8080/auth/refresh', JSON.stringify({ refreshToken }), {
      headers: { 'Content-Type': 'application/json' },
    });
    check(refreshRes, {
      'refresh status 200 or 401': r => r.status === 200 || r.status === 401,
      'refresh latency < 200ms': r => r.timings.duration < 200,
    });
  }
  sleep(0.2);
}

