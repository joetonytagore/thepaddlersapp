import React, { useEffect, useState } from 'react';
import { authFetch } from './api';

export default function POSManager() {
  const [invoices, setInvoices] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  useEffect(() => { fetchInvoices(); }, []);
  async function fetchInvoices() {
    setLoading(true);
    const res = await authFetch('/api/invoices');
    if(res.ok) setInvoices(await res.json());
    setLoading(false);
  }
  async function refundInvoice(id: number) {
    await authFetch(`/api/invoices/${id}/refund`, { method: 'POST' });
    fetchInvoices();
  }
  return (
    <div className="mt-4 p-4 border rounded bg-gray-50">
      <h3 className="font-bold mb-2">Payments / Invoicing & POS</h3>
      {loading ? <div>Loading...</div> : (
        <table className="table-auto w-full text-sm"><thead><tr><th>ID</th><th>Amount</th><th>Status</th><th>Actions</th></tr></thead><tbody>
          {invoices.map(inv => <tr key={inv.id}><td>{inv.id}</td><td>{inv.amount}</td><td>{inv.status}</td><td><button onClick={()=>refundInvoice(inv.id)} className="px-2 py-1 bg-red-600 text-white rounded">Refund</button></td></tr>)}
        </tbody></table>
      )}
    </div>
  );
}

