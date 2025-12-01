import React, { useState } from 'react';
import PlayerSearch from '../../components/PlayerSearch';

const matches = [
  { id: 'match1', player1: 'Alice', player2: 'Bob', scheduled: '10:00', checkedIn: false },
  { id: 'match2', player1: 'Carol', player2: 'Dave', scheduled: '11:00', checkedIn: false },
];

const Kiosk: React.FC = () => {
  const [selectedPlayer, setSelectedPlayer] = useState<any>(null);
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleCheckIn = async (matchId: string) => {
    setLoading(true);
    setError('');
    setSuccess('');
    try {
      const res = await fetch(`/api/matches/${matchId}/checkin`, { method: 'POST' });
      if (!res.ok) throw new Error(await res.text());
      setSuccess('Checked in!');
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full h-screen flex flex-col items-center justify-center bg-gray-50">
      <h1 className="text-3xl font-bold mb-6">Kiosk Check-In</h1>
      <div className="mb-4 w-full max-w-md">
        <PlayerSearch onSelect={setSelectedPlayer} placeholder="Search by phone or email" />
        {selectedPlayer && <div className="mt-2 text-green-700">Selected: {selectedPlayer.name}</div>}
      </div>
      <div className="w-full max-w-2xl grid grid-cols-1 md:grid-cols-2 gap-6">
        {matches.filter(m =>
          !selectedPlayer || m.player1 === selectedPlayer.name || m.player2 === selectedPlayer.name
        ).map(match => (
          <button
            key={match.id}
            className="bg-blue-600 text-white text-2xl rounded-lg p-8 mb-4 w-full shadow-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
            onClick={() => handleCheckIn(match.id)}
            disabled={loading}
            aria-label={`Check in for match ${match.player1} vs ${match.player2}`}
          >
            {match.player1} vs {match.player2} <br />
            <span className="text-lg">{match.scheduled}</span>
          </button>
        ))}
      </div>
      {error && <div className="text-red-500 mt-4">{error}</div>}
      {success && <div className="text-green-500 mt-4">{success}</div>}
    </div>
  );
};

export default Kiosk;

