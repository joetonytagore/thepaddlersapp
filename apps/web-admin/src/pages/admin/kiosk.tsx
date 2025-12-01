import React, { useEffect, useState } from 'react';
import { KioskMatchCard } from '../../components/KioskMatchCard';
import { PrintableBracket } from '../../components/PrintableBracket';
import { useRouter } from 'next/router';

const orgId = 'myorg'; // Replace with actual orgId logic

const fetchMatches = async () => {
  // Replace with actual API call
  return [
    { id: '1', player1: 'Alice', player2: 'Bob', scheduledTime: new Date().toISOString(), checkedIn: false },
    { id: '2', player1: 'Carol', player2: 'Dave', scheduledTime: new Date().toISOString(), checkedIn: false },
  ];
};

const fetchBracket = async () => {
  // Replace with actual API call
  return [
    { id: '1', round: 1, player1: 'Alice', player2: 'Bob', winner: 'Alice' },
    { id: '2', round: 1, player1: 'Carol', player2: 'Dave', winner: 'Dave' },
    { id: '3', round: 2, player1: 'Alice', player2: 'Dave', winner: '' },
  ];
};

const AdminKiosk: React.FC = () => {
  const router = useRouter();
  // TODO: Fetch user role and orgId from auth context or session
  // Example: const { user, orgId, role } = useAuth();
  // Only allow ROLE_ADMIN or ROLE_STAFF
  // If not authorized, redirect or show error

  const [matches, setMatches] = useState<any[]>([]);
  const [search, setSearch] = useState('');
  const [bracket, setBracket] = useState<any[]>([]);

  useEffect(() => {
    // TODO: Add audit logging for page access
    fetchMatches().then(setMatches);
    fetchBracket().then(setBracket);
  }, []);

  const handleCheckIn = async (matchId: string) => {
    // TODO: Add audit logging for check-in
    await fetch(`/api/orgs/${orgId}/matches/${matchId}/checkin`, { method: 'POST' });
    setMatches(ms => ms.map(m => m.id === matchId ? { ...m, checkedIn: true } : m));
  };

  const filteredMatches = matches.filter(m =>
    !search || m.player1.toLowerCase().includes(search.toLowerCase()) || m.player2.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">Kiosk Check-in</h1>
      <input
        className="input mb-4 w-full"
        placeholder="Search by player name..."
        value={search}
        onChange={e => setSearch(e.target.value)}
      />
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
        {filteredMatches.map(match => (
          <KioskMatchCard key={match.id} match={match} orgId={orgId} onCheckIn={handleCheckIn} />
        ))}
      </div>
      <h2 className="text-xl font-bold mb-4">Printable Bracket</h2>
      <div className="bg-white p-4 rounded shadow">
        <PrintableBracket matches={bracket} />
        <button className="btn mt-4" onClick={() => window.print()}>Print Bracket</button>
      </div>
    </div>
  );
};

export default AdminKiosk;
