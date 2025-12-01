import React from 'react';
import { League } from '../types';

interface Props {
  league: League;
  onGenerateSchedule: (id: string) => void;
  onViewStandings: (id: string) => void;
}

export const LeagueCard: React.FC<Props> = ({ league, onGenerateSchedule, onViewStandings }) => (
  <div className="border rounded p-4 mb-4 shadow">
    <div className="font-bold text-lg">{league.name}</div>
    <div className="text-sm text-gray-600">{league.description}</div>
    <div className="mt-2 flex gap-2">
      <button className="btn" onClick={() => onGenerateSchedule(league.id)}>Generate Schedule</button>
      <button className="btn" onClick={() => onViewStandings(league.id)}>View Standings</button>
    </div>
  </div>
);

