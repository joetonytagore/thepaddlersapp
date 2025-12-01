import React, { useState } from 'react';
export default PlayerSearch;

};
  );
    </div>
      </ul>
        ))}
          </li>
            <span className="font-semibold">{player.name}</span> <span className="text-xs text-gray-600">{player.email} {player.phone}</span>
          >
            aria-label={`Select player ${player.name}`}
            tabIndex={0}
            onClick={() => onSelect(player)}
            className="p-2 hover:bg-blue-100 cursor-pointer"
            key={player.id}
          <li
        {results.map(player => (
      <ul className="bg-white rounded shadow">
      {error && <div className="text-red-500">{error}</div>}
      {loading && <div className="text-gray-500">Searching...</div>}
      />
        aria-label="Player search"
        onChange={e => handleSearch(e.target.value)}
        value={query}
        placeholder={placeholder || 'Search player by name, email, or phone'}
        className="input w-full mb-2"
      <input
    <div className="w-full">
  return (

  };
    }
      setLoading(false);
    } finally {
      setResults([]);
      setError(e.message);
    } catch (e: any) {
      setResults(await res.json());
      if (!res.ok) throw new Error('Search failed');
      const res = await fetch(`/api/users?query=${encodeURIComponent(q)}`);
    try {
    setError('');
    setLoading(true);
    setQuery(q);
  const handleSearch = async (q: string) => {

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState<Player[]>([]);
  const [query, setQuery] = useState('');
const PlayerSearch: React.FC<PlayerSearchProps> = ({ onSelect, placeholder }) => {

}
  placeholder?: string;
  onSelect: (player: Player) => void;
interface PlayerSearchProps {

}
  phone?: string;
  email: string;
  name: string;
  id: string;
interface Player {


