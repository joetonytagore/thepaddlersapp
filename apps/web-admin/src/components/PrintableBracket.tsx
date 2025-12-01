import React from 'react';

interface Match {
  id: string;
  round: number;
  player1: string;
  player2: string;
  winner?: string;
}

interface Props {
  matches: Match[];
}

export const PrintableBracket: React.FC<Props> = ({ matches }) => {
  // Simple SVG bracket rendering
  const rounds = Array.from(new Set(matches.map(m => m.round))).sort((a, b) => a - b);
  const roundMatches = rounds.map(r => matches.filter(m => m.round === r));
  const width = 200 * rounds.length;
  const height = 80 * Math.max(...roundMatches.map(rm => rm.length));
  return (
    <svg width={width} height={height}>
      {roundMatches.map((rm, i) =>
        rm.map((m, j) => (
          <g key={m.id}>
            <rect x={i * 200 + 10} y={j * 80 + 10} width={180} height={60} rx={10} fill="#fff" stroke="#333" />
            <text x={i * 200 + 20} y={j * 80 + 35} fontSize={16} fontWeight="bold">{m.player1}</text>
            <text x={i * 200 + 20} y={j * 80 + 55} fontSize={16}>{m.player2}</text>
            {m.winner && <text x={i * 200 + 120} y={j * 80 + 35} fontSize={14} fill="green">Winner: {m.winner}</text>}
          </g>
        ))
      )}
    </svg>
  );
};

