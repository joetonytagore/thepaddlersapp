import React, { useEffect, useState } from 'react';
import { authFetch } from './api';

type Membership = { id: number, userId: number, status: string, creditsRemaining: number, stripeSubscriptionId?: string };

export default function MembershipManager() {
  const [memberships, setMemberships] = useState<Membership[]>([]);
  const [loading, setLoading] = useState(false);
  useEffect(() => { fetchMemberships(); }, []);
  async function fetchMemberships() {
    setLoading(true);
    const res = await authFetch('/api/memberships');
    if(res.ok) setMemberships(await res.json());
    setLoading(false);
  }
  return (
    <div className="mt-4 p-4 border rounded bg-gray-50">
      <h3 className="font-bold mb-2">Memberships & Recurring Billing</h3>
      {loading ? <div>Loading...</div> : (
        <table className="table-auto w-full text-sm"><thead><tr><th>ID</th><th>User</th><th>Status</th><th>Credits</th><th>Stripe Sub</th></tr></thead><tbody>
          {memberships.map(m => <tr key={m.id}><td>{m.id}</td><td>{m.userId}</td><td>{m.status}</td><td>{m.creditsRemaining}</td><td>{m.stripeSubscriptionId||'-'}</td></tr>)}
        </tbody></table>
      )}
    </div>
  );
}

