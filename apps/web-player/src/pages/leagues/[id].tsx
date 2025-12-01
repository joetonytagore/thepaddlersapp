import React, { useEffect, useState } from 'react';
import { fetchWithAuth } from '../../utils/fetchWithAuth';
import { LeagueCard } from '../../components/LeagueCard';
import { MatchRow } from '../../components/MatchRow';
import { League, Match } from '../../types';
import { useRouter } from 'next/router';

const LeagueDetails: React.FC = () => {
  const router = useRouter();
  const { id } = router.query;
  const [league, setLeague] = useState<League | null>(null);
  const [matches, setMatches] = useState<Match[]>([]);
  const [error, setError] = useState('');
  const [userId, setUserId] = useState('');

  useEffect(() => {
    // Fetch userId from auth context or localStorage
    setUserId(localStorage.getItem('userId') || '');
    if (id) {
      fetchWithAuth(`/api/orgs/myorg/leagues/${id}`)
        .then(setLeague)
        .catch(e => setError(e.message));
      fetchWithAuth(`/api/orgs/myorg/leagues/${id}/matches?userId=${userId}`)
        .then(setMatches)
        .catch(e => setError(e.message));
    }
  }, [id, userId]);

  const handleCheckIn = async (matchId: string) => {
    await fetchWithAuth(`/api/orgs/myorg/matches/${matchId}/checkin`, { method: 'POST' });
    setMatches(matches => matches.map(m => m.id === matchId ? { ...m, checkedIn: true } : m));
  };

  const handleSubmitScore = async (matchId: string, score: { playerId: string; points: number }[]) => {
    await fetchWithAuth(`/api/orgs/myorg/matches/${matchId}/submit-score`, {
      method: 'POST',
      body: JSON.stringify({ submittedByUserId: userId, score }),
    });
    setMatches(matches => matches.map(m => m.id === matchId ? { ...m, scoreSubmitted: true } : m));
  };

  return (
    <div className="p-8">
      {error && <div className="text-red-500 mb-2">{error}</div>}
      {league && <LeagueCard league={league} />}
      <h2 className="text-xl font-bold mt-6 mb-2">Upcoming Matches</h2>
      {matches.map(match => (
        <MatchRow
          key={match.id}
          match={match}
          onCheckIn={handleCheckIn}
          onSubmitScore={handleSubmitScore}
          isCheckedIn={!!match.checkedIn}
          isCompleted={!!match.isCompleted}
        />
      ))}
    </div>
  );
};

export default LeagueDetails;

