import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Ban, BarChart3, Check, Clipboard, Link2, LogOut, RefreshCw, Shield, Trash2, UserCog, UserRound } from 'lucide-react';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

async function api(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  });

  if (!response.ok) {
    const payload = await response.json().catch(() => ({ message: 'Request failed.' }));
    throw { status: response.status, ...payload };
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

function App() {
  const [user, setUser] = useState(null);
  const [checkingAuth, setCheckingAuth] = useState(true);
  const [authMode, setAuthMode] = useState('login');
  const [authError, setAuthError] = useState('');

  useEffect(() => {
    api('/api/auth/me')
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setCheckingAuth(false));
  }, []);

  async function handleAuth({ email, password }) {
    setAuthError('');
    try {
      const signedIn = await api(`/api/auth/${authMode}`, {
        method: 'POST',
        body: JSON.stringify({ email, password })
      });
      setUser(signedIn);
    } catch (err) {
      const fieldError = err.fields?.email || err.fields?.password;
      setAuthError(fieldError || err.message || 'Authentication failed.');
    }
  }

  async function logout() {
    await api('/api/auth/logout', { method: 'POST' });
    setUser(null);
  }

  if (checkingAuth) {
    return <main className="shell"><section className="panel">Loading...</section></main>;
  }

  if (!user) {
    return (
      <AuthScreen
        mode={authMode}
        setMode={setAuthMode}
        onSubmit={handleAuth}
        error={authError}
      />
    );
  }

  return <Dashboard user={user} onLogout={logout} />;
}

function BrandMark({ compact = false }) {
  return (
    <div className={compact ? 'brand compact' : 'brand'}>
      <span className="brandIcon">
        <Link2 size={compact ? 18 : 24} />
      </span>
      <div>
        <strong>LinkVaults</strong>
        {!compact && <span>Secure URL Manager</span>}
      </div>
    </div>
  );
}

function AuthScreen({ mode, setMode, onSubmit, error }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    try {
      await onSubmit({ email, password });
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="authShell">
      <section className="authPanel">
        <BrandMark />
        <div className="panelTitle">
          <UserRound size={22} />
          <h1>{mode === 'login' ? 'Sign in' : 'Create account'}</h1>
        </div>
        <form className="authForm" onSubmit={submit}>
          <input
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
          <input
            type="password"
            placeholder="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            minLength={6}
            required
          />
          <button type="submit" disabled={loading}>
            {loading ? 'Please wait...' : mode === 'login' ? 'Sign in' : 'Register'}
          </button>
        </form>
        {error && <p className="error">{error}</p>}
        <button
          type="button"
          className="textButton"
          onClick={() => setMode(mode === 'login' ? 'register' : 'login')}
        >
          {mode === 'login' ? 'Need an account? Register' : 'Already have an account? Sign in'}
        </button>
      </section>
    </main>
  );
}

function Dashboard({ user, onLogout }) {
  const [url, setUrl] = useState('');
  const [customAlias, setCustomAlias] = useState('');
  const [created, setCreated] = useState(null);
  const [summary, setSummary] = useState({ totalLinks: 0, totalClicks: 0, activeLinks: 0 });
  const [recentLinks, setRecentLinks] = useState([]);
  const [topLinks, setTopLinks] = useState([]);
  const [recentClicks, setRecentClicks] = useState([]);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(false);

  const sortedRecentLinks = useMemo(() => recentLinks.slice(0, 6), [recentLinks]);

  function showNotice(message) {
    setNotice(message);
    window.setTimeout(() => setNotice(''), 1800);
  }

  async function loadDashboard() {
    const [summaryData, recentData, topData, clicksData] = await Promise.all([
      api('/api/dashboard/summary'),
      api('/api/urls/recent'),
      api('/api/urls/top'),
      api('/api/clicks/recent')
    ]);
    setSummary(summaryData);
    setRecentLinks(recentData);
    setTopLinks(topData);
    setRecentClicks(clicksData);
  }

  useEffect(() => {
    loadDashboard().catch((err) => setError(err.message || 'Could not load dashboard data.'));
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    setCreated(null);

    try {
      const result = await api('/api/urls', {
        method: 'POST',
        body: JSON.stringify({ originalUrl: url, customAlias })
      });
      setCreated(result);
      setUrl('');
      setCustomAlias('');
      await loadDashboard();
    } catch (err) {
      const fieldError = err.fields?.originalUrl || err.fields?.customAlias;
      setError(fieldError || err.message || 'Could not shorten this URL.');
    } finally {
      setLoading(false);
    }
  }

  async function copy(value) {
    await navigator.clipboard.writeText(value);
    showNotice('Short URL copied');
  }

  async function deleteLink(id) {
    await api(`/api/urls/${id}`, { method: 'DELETE' });
    await loadDashboard();
  }

  return (
    <main className="shell">
      <header className="topbar">
        <div className="headerIdentity">
          <BrandMark compact />
          <div>
            <h1>URL Shortener</h1>
            <p>Signed in as {user.email} - {user.role}</p>
          </div>
        </div>
        <div className="topActions">
          <button className="iconButton" type="button" onClick={() => loadDashboard()} title="Refresh dashboard">
            <RefreshCw size={18} />
          </button>
          <button className="iconButton" type="button" onClick={onLogout} title="Sign out">
            <LogOut size={18} />
          </button>
        </div>
      </header>

      <section className="panel shortener">
        <div className="panelTitle">
          <Link2 size={20} />
          <h2>Shorten a URL</h2>
        </div>
        <form onSubmit={handleSubmit} className="form">
          <input
            type="url"
            placeholder="https://example.com/very/long/path"
            value={url}
            onChange={(event) => setUrl(event.target.value)}
            onPaste={() => showNotice('Long URL pasted')}
            required
          />
          <input
            type="text"
            placeholder="custom-alias"
            value={customAlias}
            onChange={(event) => setCustomAlias(event.target.value)}
            maxLength={32}
          />
          <button type="submit" disabled={loading}>
            {loading ? 'Creating...' : 'Shorten'}
          </button>
        </form>
        {error && <p className="error">{error}</p>}
        {notice && <p className="success">{notice}</p>}
        {created && (
          <div className="result">
            <a href={created.shortUrl} target="_blank" rel="noreferrer">{created.shortUrl}</a>
            <button type="button" className="iconButton" onClick={() => copy(created.shortUrl)} title="Copy URL">
              <Clipboard size={18} />
            </button>
          </div>
        )}
      </section>

      {user.role === 'ADMIN' && <AdminPanel />}

      <section className="metrics">
        <Metric label="Total links" value={summary.totalLinks} />
        <Metric label="Total clicks" value={summary.totalClicks} />
        <Metric label="Active links" value={summary.activeLinks} />
      </section>

      <section className="grid">
        <div className="panel">
          <div className="panelTitle">
            <BarChart3 size={20} />
            <h2>Top Links</h2>
          </div>
          <LinkTable links={topLinks} onCopy={copy} onDelete={deleteLink} />
        </div>

        <div className="panel">
          <h2>Recent Links</h2>
          <LinkTable links={sortedRecentLinks} onCopy={copy} onDelete={deleteLink} />
        </div>
      </section>

      <section className="panel">
        <h2>Recent Clicks</h2>
        <div className="tableWrap">
          <table>
            <thead>
              <tr>
                <th>Code</th>
                <th>Clicked</th>
                <th>IP</th>
                <th>Referrer</th>
              </tr>
            </thead>
            <tbody>
              {recentClicks.length === 0 && (
                <tr><td colSpan="4" className="empty">No clicks yet.</td></tr>
              )}
              {recentClicks.map((click, index) => (
                <tr key={`${click.shortCode}-${click.clickedAt}-${index}`}>
                  <td>{click.shortCode}</td>
                  <td>{formatDate(click.clickedAt)}</td>
                  <td>{click.ipAddress || '-'}</td>
                  <td>{click.referrer || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}

function AdminPanel() {
  const [users, setUsers] = useState([]);
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function loadUsers() {
    setUsers(await api('/api/admin/users'));
  }

  useEffect(() => {
    loadUsers().catch((err) => setError(err.message || 'Could not load users.'));
  }, []);

  async function runAction(path, method = 'POST') {
    setMessage('');
    setError('');
    try {
      await api(path, {
        method,
        body: JSON.stringify({ email })
      });
      setMessage('Admin action completed.');
      setEmail('');
      await loadUsers();
    } catch (err) {
      setError(err.message || 'Admin action failed.');
    }
  }

  return (
    <section className="panel adminPanel">
      <div className="panelTitle">
        <UserCog size={20} />
        <h2>Admin Dashboard</h2>
      </div>
      <div className="adminControls">
        <input
          type="email"
          placeholder="user@example.com"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
        />
        <button type="button" onClick={() => runAction('/api/admin/users/admin')}>
          <Shield size={16} /> Make admin
        </button>
        <button type="button" onClick={() => runAction('/api/admin/users/user')}>
          <UserRound size={16} /> Make user
        </button>
        <button type="button" onClick={() => runAction('/api/admin/users/block')}>
          <Ban size={16} /> Block
        </button>
        <button type="button" onClick={() => runAction('/api/admin/users/unblock')}>
          <Check size={16} /> Unblock
        </button>
        <button type="button" className="dangerButton" onClick={() => runAction('/api/admin/users', 'DELETE')}>
          <Trash2 size={16} /> Delete
        </button>
      </div>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}
      <div className="tableWrap">
        <table>
          <thead>
            <tr>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 && (
              <tr><td colSpan="4" className="empty">No users found.</td></tr>
            )}
            {users.map((item) => (
              <tr key={item.id}>
                <td>{item.email}</td>
                <td>{item.role}</td>
                <td>{item.blocked ? 'Blocked' : 'Active'}</td>
                <td>{formatDate(item.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function Metric({ label, value }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function LinkTable({ links, onCopy, onDelete }) {
  return (
    <div className="tableWrap">
      <table>
        <thead>
          <tr>
            <th>Short URL</th>
            <th>Original</th>
            <th>Clicks</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {links.length === 0 && (
            <tr><td colSpan="4" className="empty">No links created yet.</td></tr>
          )}
          {links.map((link) => (
            <tr key={link.id}>
              <td><a href={link.shortUrl} target="_blank" rel="noreferrer">{link.shortCode}</a></td>
              <td className="urlCell" title={link.originalUrl}>{link.originalUrl}</td>
              <td>{link.clickCount}</td>
              <td className="actions">
                <button type="button" className="iconButton" onClick={() => onCopy(link.shortUrl)} title="Copy URL">
                  <Clipboard size={16} />
                </button>
                <button type="button" className="iconButton danger" onClick={() => onDelete(link.id)} title="Delete URL">
                  <Trash2 size={16} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function formatDate(value) {
  if (!value) {
    return '-';
  }
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
}

createRoot(document.getElementById('root')).render(<App />);
