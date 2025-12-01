export async function fetchWithAuth(url: string, options: RequestInit = {}) {
}
  return res.json();
  }
    throw new Error(error.message || 'API error');
    const error = await res.json().catch(() => ({ message: res.statusText }));
  if (!res.ok) {
  const res = await fetch(url, { ...options, headers });
  };
    'Content-Type': 'application/json',
    Authorization: token ? `Bearer ${token}` : '',
    ...(options.headers || {}),
  const headers = {
  const token = localStorage.getItem('token');

