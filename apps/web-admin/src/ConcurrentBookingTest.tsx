import React, { useState } from 'react';
import { authFetch } from './api';

export default function ConcurrentBookingTest() {
  const [courtId, setCourtId] = useState<number>(1);
  const [userId, setUserId] = useState<number>(1);
  const [start, setStart] = useState('');
  const [end, setEnd] = useState('');
  const [count, setCount] = useState(5);
  const [results, setResults] = useState<string[]>([]);

  async function runTest() {
    setResults([]);
    const payload = { court: { id: courtId }, user: { id: userId }, startAt: start, endAt: end };
    const promises = Array(count).fill(0).map(() => authFetch('/api/bookings', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    }));
    const resArr = await Promise.all(promises);
    setResults(resArr.map((r, i) => `Attempt ${i + 1}: ${r.status}`));
  }

  return (
    <div className="mt-4 p-4 border rounded bg-gray-50">
      <h3 className="font-bold mb-2">Concurrent Booking Stress Test</h3>
      <form onSubmit={e => { e.preventDefault(); runTest(); }} className="space-y-2">
        <input type="number" value={courtId} onChange={e => setCourtId(Number(e.target.value))} placeholder="Court ID" />
        <input type="number" value={userId} onChange={e => setUserId(Number(e.target.value))} placeholder="User ID" />
        <input type="text" value={start} onChange={e => setStart(e.target.value)} placeholder="Start (ISO)" />
        <input type="text" value={end} onChange={e => setEnd(e.target.value)} placeholder="End (ISO)" />
        <input type="number" value={count} onChange={e => setCount(Number(e.target.value))} min={1} max={20} />
        <button type="submit" className="px-3 py-1 bg-blue-600 text-white rounded">Run Test</button>
      </form>
      <div className="mt-2">
        {results.map((r, i) => <div key={i}>{r}</div>)}
      </div>
    </div>
  );
}
