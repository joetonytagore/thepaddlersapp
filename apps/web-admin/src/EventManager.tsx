import React, { useEffect, useState } from 'react';
import { authFetch } from './api';

type Event = { id: number, title: string, startsAt: string, endsAt: string, capacity: number };
type Registration = { id: number, userId: number, status: string };

export default function EventManager() {
  const [events, setEvents] = useState<Event[]>([]);
  const [registrations, setRegistrations] = useState<Registration[]>([]);
  const [loading, setLoading] = useState(false);
  useEffect(() => { fetchEvents(); }, []);
  async function fetchEvents() {
    setLoading(true);
    const res = await authFetch('/api/orgs/1/events');
    if(res.ok) setEvents(await res.json());
    setLoading(false);
  }
  async function fetchRegistrations(eventId: number) {
    setLoading(true);
    const res = await authFetch(`/api/orgs/1/events/${eventId}/registrations`);
    if(res.ok) setRegistrations(await res.json());
    setLoading(false);
  }
  return (
    <div className="mt-4 p-4 border rounded bg-gray-50">
      <h3 className="font-bold mb-2">Events / Programs / Waitlist</h3>
      <div>
        <h4 className="font-semibold">Events</h4>
        <ul>{events.map(e => <li key={e.id}>{e.title} ({e.startsAt} - {e.endsAt}) Capacity: {e.capacity} <button onClick={()=>fetchRegistrations(e.id)} className="ml-2 px-2 py-1 bg-blue-600 text-white rounded">View Registrations</button></li>)}</ul>
      </div>
      <div className="mt-2">
        <h4 className="font-semibold">Registrations</h4>
        <ul>{registrations.map(r => <li key={r.id}>User: {r.userId} | Status: {r.status}</li>)}</ul>
      </div>
    </div>
  );
}

