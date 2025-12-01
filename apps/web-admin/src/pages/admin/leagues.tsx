import React, { useEffect, useState } from 'react';
import { fetchWithAuth } from '../../utils/fetchWithAuth';
import { LeagueCard } from '../../components/LeagueCard';
import { League } from '../../types';

const AdminLeagues: React.FC = () => {
  const [leagues, setLeagues] = useState<League[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchWithAuth('/api/orgs/myorg/leagues')
      .then(setLeagues)
      .catch(e => setError(e.message));
  }, []);

  const validateLeagueForm = (data: any) => {
    if (!data.name || !data.format) return 'Name and format are required.';
    if (data.startTime && isNaN(Date.parse(data.startTime))) return 'Invalid start time.';
    if (data.endTime && isNaN(Date.parse(data.endTime))) return 'Invalid end time.';
    return '';
  };

  const handleCreateLeague = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.currentTarget;
    const data = {
      name: form.name.value,
      format: form.format.value,
      description: form.description.value,
      startTime: form.startTime.value,
      endTime: form.endTime.value,
    };
    const validationError = validateLeagueForm(data);
    if (validationError) {
      setError(validationError);
      return;
    }
    try {
      await fetchWithAuth('/api/orgs/myorg/leagues', {
        method: 'POST',
        body: JSON.stringify(data),
      });
      setShowModal(false);
      setError('');
      // Refresh list
      fetchWithAuth('/api/orgs/myorg/leagues').then(setLeagues);
    } catch (e: any) {
      setError(e.message);
    }
  };

  const handleGenerateSchedule = async (id: string) => {
    try {
      await fetchWithAuth(`/api/orgs/myorg/leagues/${id}/generate-schedule`, { method: 'POST' });
      alert('Schedule generated!');
    } catch (e: any) {
      setError(e.message);
    }
  };

  const handleViewStandings = (id: string) => {
    window.location.href = `/admin/leagues/${id}/standings`;
  };

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">Leagues</h1>
      <button className="btn mb-4" onClick={() => setShowModal(true)}>Create League</button>
      {error && <div className="text-red-500 mb-2">{error}</div>}
      {leagues.map(league => (
        <LeagueCard
          key={league.id}
          league={league}
          onGenerateSchedule={handleGenerateSchedule}
          onViewStandings={handleViewStandings}
        />
      ))}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <form className="bg-white p-6 rounded shadow w-96" onSubmit={handleCreateLeague}>
            <h2 className="text-lg font-bold mb-2">Create League</h2>
            <input name="name" className="input mb-2 w-full" placeholder="Name" required />
            <input name="format" className="input mb-2 w-full" placeholder="Format" required />
            <input name="description" className="input mb-2 w-full" placeholder="Description" />
            <input name="startTime" className="input mb-2 w-full" type="datetime-local" placeholder="Start Time" />
            <input name="endTime" className="input mb-2 w-full" type="datetime-local" placeholder="End Time" />
            <div className="flex gap-2 mt-4">
              <button type="submit" className="btn">Create</button>
              <button type="button" className="btn" onClick={() => setShowModal(false)}>Cancel</button>
            </div>
            {error && <div className="text-red-500 mt-2">{error}</div>}
          </form>
        </div>
      )}
    </div>
  );
};

export default AdminLeagues;
