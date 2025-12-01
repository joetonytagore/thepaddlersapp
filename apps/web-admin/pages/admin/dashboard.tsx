import React, { useEffect, useState } from 'react';

const Dashboard: React.FC = () => {
  const [matches, setMatches] = useState<any[]>([]);
  const [noShows, setNoShows] = useState<number>(0);
  const [revenue, setRevenue] = useState<number>(0);
  const [events, setEvents] = useState<any[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    // Fetch today's matches
    fetch('/api/dashboard/matches').then(res => res.json()).then(setMatches).catch(e => setError(e.message));
    fetch('/api/dashboard/noshows').then(res => res.json()).then(setNoShows).catch(e => setError(e.message));
    fetch('/api/dashboard/revenue').then(res => res.json()).then(setRevenue).catch(e => setError(e.message));
    fetch('/api/dashboard/events').then(res => res.json()).then(setEvents).catch(e => setError(e.message));
  }, []);

  return (
    <div className="max-w-4xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-6">Admin Dashboard</h1>
      {error && <div className="text-red-500 mb-4">{error}</div>}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white rounded shadow p-6">
          <div className="text-lg font-semibold">Today's Matches</div>
          <div className="text-3xl font-bold">{matches.length}</div>
        </div>
        <div className="bg-white rounded shadow p-6">
          <div className="text-lg font-semibold">No-Shows</div>
          <div className="text-3xl font-bold">{noShows}</div>
        </div>
        <div className="bg-white rounded shadow p-6">
          <div className="text-lg font-semibold">Revenue Today</div>
          <div className="text-3xl font-bold">${revenue.toFixed(2)}</div>
        </div>
      </div>
      <h2 className="text-xl font-bold mb-4">Upcoming Events</h2>
      <ul className="bg-white rounded shadow">
        {events.map(ev => (
          <li key={ev.id} className="p-4 border-b last:border-b-0">
            <span className="font-semibold">{ev.name}</span> <span className="text-xs text-gray-600">{ev.date}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Dashboard;

