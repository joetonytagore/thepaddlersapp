import React, { useState } from 'react';
import PlayerSearch from '../../components/PlayerSearch';

const courts = [
  { id: 'court1', name: 'Court 1' },
  { id: 'court2', name: 'Court 2' },
];

const QuickBook: React.FC = () => {
  const [courtId, setCourtId] = useState('');
  const [start, setStart] = useState('');
  const [end, setEnd] = useState('');
  const [player, setPlayer] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleBook = async () => {
    setLoading(true);
    setError('');
    setSuccess('');
    try {
      const res = await fetch('/api/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ courtId, start, end, playerId: player?.id }),
      });
      if (!res.ok) throw new Error(await res.text());
      setSuccess('Booking created!');
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-lg mx-auto p-8">
      <h1 className="text-2xl font-bold mb-4">Quick Book</h1>
      <div className="mb-4">
        <label className="block mb-1">Court</label>
        <select className="input w-full" value={courtId} onChange={e => setCourtId(e.target.value)}>
          <option value="">Select court</option>
          {courts.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>
      <div className="mb-4">
        <label className="block mb-1">Start Time</label>
        <input type="datetime-local" className="input w-full" value={start} onChange={e => setStart(e.target.value)} />
      </div>
      <div className="mb-4">
        <label className="block mb-1">End Time</label>
        <input type="datetime-local" className="input w-full" value={end} onChange={e => setEnd(e.target.value)} />
      </div>
      <div className="mb-4">
        <PlayerSearch onSelect={setPlayer} />
        {player && <div className="mt-2 text-green-700">Selected: {player.name}</div>}
      </div>
      <button className="btn w-full" onClick={handleBook} disabled={loading || !courtId || !start || !end || !player}>
        {loading ? 'Booking...' : 'Create Booking'}
      </button>
      {error && <div className="text-red-500 mt-2">{error}</div>}
      {success && <div className="text-green-500 mt-2">{success}</div>}
    </div>
  );
};

export default QuickBook;

