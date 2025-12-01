import http from 'k6/http';
}
  sleep(0.1);
  });
    'latency < 200ms': r => r.timings.duration < 200,
    'status is 200 or 409': r => r.status === 200 || r.status === 409,
  check(res, {
  });
    headers: { 'Content-Type': 'application/json' },
  const res = http.post('http://localhost:8080/api/reservations', payload, {
  const payload = JSON.stringify({ courtId, userId, start, end });
  const end = new Date(Date.now() + 7200 * 1000).toISOString();
  const start = new Date(Date.now() + 3600 * 1000).toISOString();
  const userId = userIds[Math.floor(Math.random() * userIds.length)];
  const courtId = courts[Math.floor(Math.random() * courts.length)];
export default function () {

const userIds = Array.from({ length: 100 }, (_, i) => `user${i}`);
const courts = ['court1', 'court2', 'court3'];

};
  },
    http_req_duration: ['p(50)<200'], // median < 200ms
    http_req_failed: ['rate<0.01'], // <1% errors
  thresholds: {
  duration: '60s',
  vus: 100,
export let options = {

import { check, sleep } from 'k6';

