import React, { useState } from 'react';
import { Match } from '../types';

interface Props {
  match: Match;
  onCheckIn: (matchId: string) => Promise<void>;
  onSubmitScore: (matchId: string, score: { playerId: string; points: number }[]) => Promise<void>;
  isCheckedIn: boolean;
  isCompleted: boolean;
}

export const MatchRow: React.FC<Props> = ({ match, onCheckIn, onSubmitScore, isCheckedIn, isCompleted }) => {
  const [checkingIn, setCheckingIn] = useState(false);
  const [score, setScore] = useState<{ playerId: string; points: number }[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleCheckIn = async () => {
    setCheckingIn(true);
    setError('');
    try {
      await onCheckIn(match.id);
      setSuccess('Checked in!');
    } catch (e: any) {
      setError(e.message);
    } finally {
      setCheckingIn(false);
    }
  };

  const handleScoreChange = (playerId: string, points: number) => {
    setScore(prev => {
      const idx = prev.findIndex(s => s.playerId === playerId);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = { playerId, points };
        return updated;
      }
      return [...prev, { playerId, points }];
    });
  };

  const handleSubmitScore = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    if (score.length !== 2 || score.some(s => isNaN(s.points) || s.points < 0)) {
      setError('Please enter valid scores for both players.');
      return;
    }
    try {
      await onSubmitScore(match.id, score);
      setSuccess('Score submitted!');
    } catch (e: any) {
      setError(e.message);
    }
  };

  return (
    <div className="border rounded p-2 mb-2 flex flex-col">
      <div className="flex justify-between items-center">
        <div>
          <span className="font-bold">{match.player1Name}</span> vs <span className="font-bold">{match.player2Name}</span>
        </div>
        <div>{match.scheduledTime}</div>
      </div>
      <div className="mt-2 flex gap-2">
        {!isCheckedIn && !isCompleted && (
          <button className="btn" onClick={handleCheckIn} disabled={checkingIn}>
            {checkingIn ? 'Checking in...' : 'Check-in'}
          </button>
        )}
        {isCompleted && (
          <form className="flex gap-2" onSubmit={handleSubmitScore}>
            <input
              type="number"
              className="input w-20"
              placeholder={match.player1Name}
              onChange={e => handleScoreChange(match.player1Id, Number(e.target.value))}
              required
            />
            <input
              type="number"
              className="input w-20"
              placeholder={match.player2Name}
              onChange={e => handleScoreChange(match.player2Id, Number(e.target.value))}
              required
            />
            <button type="submit" className="btn">Submit Score</button>
          </form>
        )}
      </div>
      {error && <div className="text-red-500 mt-2">{error}</div>}
      {success && <div className="text-green-500 mt-2">{success}</div>}
    </div>
  );
};

