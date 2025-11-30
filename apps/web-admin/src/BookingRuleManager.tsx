import React, { useEffect, useState } from 'react';
import { authFetch } from './api';

type Court = { id: number, name: string };
type BookingRule = {
  id?: number;
  court: Court;
  businessHourStart: string;
  businessHourEnd: string;
  memberOnlyStart?: string;
  memberOnlyEnd?: string;
  maxDurationMinutes?: number;
  minLeadTimeMinutes?: number;
  maxLeadTimeDays?: number;
  cancellationPolicy?: string;
};

export default function BookingRuleManager() {
  const [courts, setCourts] = useState<Court[]>([]);
  const [selectedCourt, setSelectedCourt] = useState<number|null>(null);
  const [rule, setRule] = useState<Partial<BookingRule>>({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string|null>(null);

  useEffect(() => { fetchCourts(); }, []);
  useEffect(() => { if(selectedCourt) fetchRule(selectedCourt); }, [selectedCourt]);

  async function fetchCourts() {
    setLoading(true);
    const res = await authFetch('/api/courts');
    if(res.ok) setCourts(await res.json());
    setLoading(false);
  }

  async function fetchRule(courtId: number) {
    setLoading(true);
    const res = await authFetch(`/api/bookings/rules/${courtId}`);
    if(res.ok) {
      const rules = await res.json();
      setRule(rules[0] || { court: courts.find(c=>c.id===courtId) });
    } else {
      setRule({ court: courts.find(c=>c.id===courtId) });
    }
    setLoading(false);
  }

  async function saveRule() {
    setLoading(true);
    setMessage(null);
    const payload = { ...rule, court: { id: selectedCourt } };
    const res = await authFetch('/api/bookings/rules', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if(res.ok) setMessage('Saved!');
    else setMessage('Error saving rule');
    setLoading(false);
  }

  return (
    <div className="mt-4 p-4 border rounded bg-gray-50">
      <h3 className="font-bold mb-2">Booking Rule Management</h3>
      <div className="mb-2">
        <label className="mr-2">Select Court:</label>
        <select value={selectedCourt||''} onChange={e=>setSelectedCourt(Number(e.target.value))}>
          <option value="">-- Select --</option>
          {courts.map(c=> <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>
      {selectedCourt && (
        <form className="space-y-2" onSubmit={e=>{e.preventDefault();saveRule();}}>
          <div>
            <label>Business Hours:</label>
            <input type="time" value={rule.businessHourStart||''} onChange={e=>setRule(r=>({...r,businessHourStart:e.target.value}))} />
            <span> - </span>
            <input type="time" value={rule.businessHourEnd||''} onChange={e=>setRule(r=>({...r,businessHourEnd:e.target.value}))} />
          </div>
          <div>
            <label>Member-only Window:</label>
            <input type="time" value={rule.memberOnlyStart||''} onChange={e=>setRule(r=>({...r,memberOnlyStart:e.target.value}))} />
            <span> - </span>
            <input type="time" value={rule.memberOnlyEnd||''} onChange={e=>setRule(r=>({...r,memberOnlyEnd:e.target.value}))} />
          </div>
          <div>
            <label>Max Duration (min):</label>
            <input type="number" value={rule.maxDurationMinutes||''} onChange={e=>setRule(r=>({...r,maxDurationMinutes:Number(e.target.value)}))} />
          </div>
          <div>
            <label>Min Lead Time (min):</label>
            <input type="number" value={rule.minLeadTimeMinutes||''} onChange={e=>setRule(r=>({...r,minLeadTimeMinutes:Number(e.target.value)}))} />
          </div>
          <div>
            <label>Max Lead Time (days):</label>
            <input type="number" value={rule.maxLeadTimeDays||''} onChange={e=>setRule(r=>({...r,maxLeadTimeDays:Number(e.target.value)}))} />
          </div>
          <div>
            <label>Cancellation Policy:</label>
            <input type="text" value={rule.cancellationPolicy||''} onChange={e=>setRule(r=>({...r,cancellationPolicy:e.target.value}))} />
          </div>
          <button type="submit" className="px-3 py-1 bg-blue-600 text-white rounded">Save Rule</button>
          {message && <div className="mt-2 text-green-600">{message}</div>}
        </form>
      )}
    </div>
  );
}

