// api.js — REST API client with auto-refresh and IndexedDB offline queue
const API_BASE = '';   // empty = same origin, works for both dev and Docker

const Api = (() => {
  let accessToken  = localStorage.getItem('access_token')  || null;
  let refreshToken = localStorage.getItem('refresh_token') || null;

  function saveTokens(at, rt) {
    accessToken  = at;
    refreshToken = rt;
    localStorage.setItem('access_token',  at);
    localStorage.setItem('refresh_token', rt);
  }

  function clearTokens() {
    accessToken = refreshToken = null;
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  }

  async function request(method, path, body, retry = true) {
    const opts = {
      method,
      headers: { 'Content-Type': 'application/json' }
    };
    if (accessToken) opts.headers['Authorization'] = `Bearer ${accessToken}`;
    if (body)        opts.body = JSON.stringify(body);

    let res;
    try {
      res = await fetch(API_BASE + path, opts);
    } catch (networkErr) {
      // Network failure (server down, no connection, CORS preflight blocked)
      console.error('Network error:', networkErr);
      throw new Error('Serveris nav pieejams. Pārbaudiet savienojumu.');
    }

    // Token expired — try refresh once
    if (res.status === 401 && retry && refreshToken) {
      try {
        const rr = await fetch('/api/auth/refresh', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken })
        });
        if (rr.ok) {
          const data = await rr.json();
          saveTokens(data.accessToken, data.refreshToken);
          return request(method, path, body, false);
        }
      } catch (_) {}
      clearTokens();
      location.reload();
      return;
    }

    if (res.status === 204) return null;

    // Parse body — always try JSON
    let data;
    try {
      data = await res.json();
    } catch (_) {
      throw new Error(`Servera kļūda (${res.status})`);
    }

    if (!res.ok) {
      // Show field-level validation errors if present
      if (data.fields) {
        const msgs = Object.entries(data.fields).map(([f, m]) => `${f}: ${m}`).join(', ');
        throw new Error(msgs);
      }
      console.error('API error:', res.status, data);
      throw new Error(data.message || data.error || `Kļūda ${res.status}`);
    }

    return data;
  }

  return {
    isLoggedIn: () => !!accessToken,
    getAccessToken: () => accessToken,

    auth: {
      register: (d) => request('POST', '/api/auth/register', d)
                         .then(r => { saveTokens(r.accessToken, r.refreshToken); return r; }),
      login:    (d) => request('POST', '/api/auth/login', d)
                         .then(r => { saveTokens(r.accessToken, r.refreshToken); return r; }),
      logout:   ()  => clearTokens()
    },

    users: {
      getProfile:     ()  => request('GET', '/api/users/profile'),
      updateProfile:  (d) => request('PUT', '/api/users/profile',  d),
      changePassword: (d) => request('PUT', '/api/users/password', d)
    },

    courses: {
      getAll:  ()      => request('GET',    '/api/courses'),
      create:  (d)     => request('POST',   '/api/courses',       d),
      update:  (id, d) => request('PUT',    `/api/courses/${id}`, d),
      delete:  (id)    => request('DELETE', `/api/courses/${id}`)
    },
	admin: {
	            getStats:   ()       => request('GET',    '/api/admin/stats'),
	            getUsers:   ()       => request('GET',    '/api/admin/users'),
	            getUser:    (id)     => request('GET',    `/api/admin/users/${id}`),
	            changeRole: (id, d)  => request('PATCH',  `/api/admin/users/${id}/role`, d),
	            deleteUser: (id)     => request('DELETE', `/api/admin/users/${id}`)
	        },
    tasks: {
      getAll:       (params) => request('GET',   '/api/tasks' + (params ? '?' + new URLSearchParams(params) : '')),
      getDueToday:  ()       => request('GET',   '/api/tasks/due-today'),
      getOverdue:   ()       => request('GET',   '/api/tasks/overdue'),
      create:       (d)      => request('POST',  '/api/tasks',              d),
      update:       (id, d)  => request('PUT',   `/api/tasks/${id}`,        d),
      updateStatus: (id, s)  => request('PATCH', `/api/tasks/${id}/status`, { status: s }),
      delete:       (id)     => request('DELETE',`/api/tasks/${id}`)
    },

    dashboard: {
      getStats:        () => request('GET', '/api/dashboard'),
      getAchievements: () => request('GET', '/api/achievements')
    }
	
	
  };
})();

// ── IndexedDB offline queue ───────────────────────────────────────────────────
const OfflineQueue = (() => {
  const DB_NAME = 'study-manager-offline';
  let db;

  function open() {
    return new Promise((resolve, reject) => {
      const req = indexedDB.open(DB_NAME, 1);
      req.onupgradeneeded = e => {
        e.target.result.createObjectStore('queue', { keyPath: 'id', autoIncrement: true });
      };
      req.onsuccess = e => { db = e.target.result; resolve(db); };
      req.onerror   = () => reject(req.error);
    });
  }

  async function add(action) {
    await open();
    return new Promise((res, rej) => {
      const tx = db.transaction('queue', 'readwrite');
      tx.objectStore('queue').add({ ...action, timestamp: Date.now() });
      tx.oncomplete = () => res();
      tx.onerror    = () => rej(tx.error);
    });
  }

  async function getAll() {
    await open();
    return new Promise((res, rej) => {
      const tx  = db.transaction('queue', 'readonly');
      const req = tx.objectStore('queue').getAll();
      req.onsuccess = () => res(req.result);
      req.onerror   = () => rej(req.error);
    });
  }

  async function remove(id) {
    await open();
    return new Promise((res, rej) => {
      const tx = db.transaction('queue', 'readwrite');
      tx.objectStore('queue').delete(id);
      tx.oncomplete = () => res();
      tx.onerror    = () => rej(tx.error);
    });
  }

  return { add, getAll, remove };
})();
