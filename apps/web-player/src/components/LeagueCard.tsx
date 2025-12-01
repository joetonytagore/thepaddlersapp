import React from 'react';
import { League } from '../types';

interface Props {
  league: League;
}

export const LeagueCard: React.FC<Props> = ({ league }) => (
  <div className="border rounded p-4 mb-4 shadow">
    <div className="font-bold text-lg">{league.name}</div>
    <div className="text-sm text-gray-600">{league.description}</div>
    <div className="mt-2">Format: {league.format}</div>
    <div className="mt-2">Start: {league.startTime}</div>
    <div className="mt-2">End: {league.endTime}</div>
  </div>
);

