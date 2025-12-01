import React, { useState } from 'react';
};
  );
    </div>
      {success && <div className="text-green-600 mt-2">Check-in successful!</div>}
      </button>
        {match.checkedIn ? 'Checked In' : loading ? 'Checking In...' : 'Check-in'}
      >
        disabled={loading || match.checkedIn}
        onClick={handleCheckIn}
        className={`btn text-xl px-8 py-4 ${match.checkedIn ? 'bg-green-400' : 'bg-blue-500'}`}
      <button
      <div className="mb-2">{new Date(match.scheduledTime).toLocaleTimeString()}</div>
      <div className="text-lg font-bold mb-2">{match.player1} vs {match.player2}</div>
    <div className="border rounded p-4 mb-4 flex flex-col items-center">
  return (
  };
    }
      setLoading(false);
    } finally {
      setTimeout(() => setSuccess(false), 2000);
      setSuccess(true);
      await onCheckIn(match.id);
    try {
    setLoading(true);
  const handleCheckIn = async () => {
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
export const KioskMatchCard: React.FC<Props> = ({ match, orgId, onCheckIn }) => {

}
  onCheckIn: (matchId: string) => Promise<void>;
  orgId: string;
  };
    checkedIn: boolean;
    scheduledTime: string;
    player2: string;
    player1: string;
    id: string;
  match: {
interface Props {


