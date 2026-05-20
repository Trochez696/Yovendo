import { useCallback, useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import './App.css';

const API_URL = 'http://localhost:8080/api';

// Tipos que reflejan los DTO principales del backend.
type User = { id: number; username: string; email: string; active: boolean; roles: string[] };
type Role = { id: number; name: string; description: string };
type Inventory = {
  id: number;
  name: string;
  quantity: number;
  minStock: number;
  maxStock: number;
  unit: string;
  price: number;
  description: string;
};
type Sale = { id: number; clientName: string; amount: number; saleDate: string; description: string; consultantName: string };
type Call = { id: number; clientName: string; callDate: string; notes: string; callType: string; durationMinutes: number; consultantName: string };
type Notification = { id: number; message: string; type: string; read: boolean; createdAt: string };

// Cliente HTTP comun: agrega JSON y el token Bearer cuando existe sesion.
async function api<T>(path: string, token?: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
  });
  if (!response.ok) throw new Error(await response.text());
  return response.json();
}

function App() {
  // Estado global simple para manejar sesion, datos principales y panel por rol.
  const [token, setToken] = useState(localStorage.getItem('token') || '');
  const [me, setMe] = useState<User | null>(null);
  const [error, setError] = useState('');
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [inventory, setInventory] = useState<Inventory[]>([]);
  const [sales, setSales] = useState<Sale[]>([]);
  const [calls, setCalls] = useState<Call[]>([]);
  const [summary, setSummary] = useState<Record<string, number>>({});

  const role = useMemo(() => me?.roles?.[0] ?? '', [me]);

  const consultantBoard = useMemo(() => {
    const grouped: Record<string, { salesCount: number; salesAmount: number; callsCount: number; callsMinutes: number }> = {};
    sales.forEach((s) => {
      if (!grouped[s.consultantName]) {
        grouped[s.consultantName] = { salesCount: 0, salesAmount: 0, callsCount: 0, callsMinutes: 0 };
      }
      grouped[s.consultantName].salesCount += 1;
      grouped[s.consultantName].salesAmount += s.amount;
    });
    calls.forEach((c) => {
      if (!grouped[c.consultantName]) {
        grouped[c.consultantName] = { salesCount: 0, salesAmount: 0, callsCount: 0, callsMinutes: 0 };
      }
      grouped[c.consultantName].callsCount += 1;
      grouped[c.consultantName].callsMinutes += c.durationMinutes;
    });
    return Object.entries(grouped).map(([name, data]) => ({
      name,
      ...data,
      effectiveness: data.callsCount > 0 ? data.salesCount / data.callsCount : 0,
      avgTicket: data.salesCount > 0 ? data.salesAmount / data.salesCount : 0,
    }));
  }, [sales, calls]);

  const loadInventory = useCallback(async () => {
    const items = await api<Inventory[]>('/inventory', token);
    setInventory(items);
  }, [token]);

  useEffect(() => {
    if (!token) return;
    // Al iniciar con token se recupera el usuario actual y sus notificaciones.
    localStorage.setItem('token', token);
    Promise.all([
      api<User>('/auth/me', token).then(setMe),
      api<Notification[]>('/notifications', token).then(setNotifications),
    ]).catch(() => logout());
  }, [token]);

  useEffect(() => {
    if (!token || !role) return;
    // Cada rol carga solo los modulos que puede ver en la interfaz.
    if (role === 'ADMIN') {
      Promise.all([api<User[]>('/users', token).then(setUsers), api<Role[]>('/users/roles', token).then(setRoles)]);
    }
    if (role === 'SUPERVISOR' || role === 'DIRECTOR' || role === 'ADMIN') loadInventory().catch((err) => setError(err.message));
    if (role === 'CONSULTOR' || role === 'DIRECTOR' || role === 'ADMIN') {
      Promise.all([api<Sale[]>('/sales', token).then(setSales), api<Call[]>('/calls', token).then(setCalls)]);
    }
    if (role === 'DIRECTOR' || role === 'ADMIN') api<Record<string, number>>('/activities/summary', token).then(setSummary);
  }, [role, token, loadInventory]);

  const logout = () => {
    // Limpia sesion tanto en React como en localStorage.
    setToken('');
    setMe(null);
    localStorage.removeItem('token');
  };

  const login = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    const form = new FormData(event.currentTarget);
    try {
      // Si el backend autentica, guarda el JWT para siguientes peticiones.
      const data = await api<{ token: string }>('/auth/login', undefined, {
        method: 'POST',
        body: JSON.stringify({ username: form.get('username'), password: form.get('password') }),
      });
      setToken(data.token);
    } catch {
      setError('Credenciales invalidas o usuario inactivo');
    }
  };

  if (!token) {
    return (
      <main className="container login-page">
        <div className="bg-shape bg-shape-a" />
        <div className="bg-shape bg-shape-b" />
        <section className="login-card card reveal website-login">
          <p className="eyebrow">Plataforma comercial</p>
          <h1>Yovendo</h1>
          <p>Gestiona ventas, inventario y rendimiento del equipo en un solo lugar.</p>
          <form onSubmit={login} className="form-stack">
            <input name="username" placeholder="Usuario" />
            <input name="password" type="password" placeholder="Contrasena" />
            <button type="submit">Entrar</button>
            {error && <small className="error">{error}</small>}
          </form>
        </section>
      </main>
    );
  }

  return (
    <main className="container">
      <div className="bg-shape bg-shape-a" />
      <div className="bg-shape bg-shape-b" />
      <header className="top-nav reveal">
        <strong className="brand">Yovendo</strong>
        <div className="row">
          <span className="badge role">Perfil: {role}</span>
          <button onClick={logout}>Cerrar sesion</button>
        </div>
      </header>

      <section className="hero card reveal">
        <p className="eyebrow">Panel inteligente</p>
        <h1>Hola, {me?.username}</h1>
        <p>
          Este espacio concentra tu operacion diaria con una experiencia visual de sitio web moderna y dinamica.
        </p>
      </section>

      <section className="card reveal">
        <h2>Notificaciones</h2>
        {notifications.length === 0 ? (
          <p>Sin notificaciones.</p>
        ) : (
          <div className="list">
            {notifications.map((n) => (
              <p key={n.id} className="list-item">
                <span className="badge">{n.type}</span> {n.message}
              </p>
            ))}
          </div>
        )}
      </section>

      <div className="dashboard-grid">
        {role === 'ADMIN' && (
        <section className="card reveal">
          <h2>Administracion de usuarios</h2>
          {/* Formulario para crear usuarios rapidamente */}
          <CreateUserForm token={token} roles={roles} onCreated={(u) => setUsers((prev) => [u, ...prev])} />
          <div className="list">
            {users.map((u) => (
              <p className="list-item" key={u.id}>
                <strong>{u.username}</strong> | {u.email} | <span className={u.active ? 'ok' : 'warn'}>{u.active ? 'Activo' : 'Inactivo'}</span> | {u.roles.join(', ')}
              </p>
            ))}
          </div>
        </section>
      )}

      {(role === 'SUPERVISOR' || role === 'ADMIN') && (
        <section className="card reveal">
          <h2>Control de inventario</h2>
          <InventoryForm token={token} onCreated={loadInventory} />
          {inventory.map((i) => (
            <p key={i.id}>
              {i.name}: {i.quantity} {i.unit || 'unid.'} (min: {i.minStock}, max: {i.maxStock || 'N/A'})
            </p>
          ))}
        </section>
      )}

      {role === 'CONSULTOR' && (
        <section className="card reveal">
          <h2>Historial de ventas y llamadas</h2>
          <SalesForm token={token} onCreated={(item) => setSales((prev) => [item, ...prev])} />
          <CallsForm token={token} onCreated={(item) => setCalls((prev) => [item, ...prev])} />
          <div className="list">
            {sales.map((s) => <p className="list-item" key={s.id}>Venta {s.clientName} - ${s.amount}</p>)}
            {calls.map((c) => <p className="list-item" key={c.id}>Llamada {c.clientName} - {c.durationMinutes} min</p>)}
          </div>
        </section>
      )}

      {role === 'DIRECTOR' && (
        <section className="card reveal">
          <h2>Tablero de seguimiento de consultores</h2>
          {/* KPIs para toma de decisiones del director */}
          <div className="stats">
            <p className="stat-box">Total ventas: <strong>{summary.totalSales ?? 0}</strong></p>
            <p className="stat-box">Total llamadas: <strong>{summary.totalCalls ?? 0}</strong></p>
            <p className="stat-box">Stock bajo: <strong>{summary.lowStockItems ?? 0}</strong></p>
            <p className="stat-box">Consultores activos: <strong>{consultantBoard.length}</strong></p>
          </div>

          {/* Ranking de desempeno comercial */}
          <h3>Ranking por consultor</h3>
          {consultantBoard.length === 0 ? (
            <p>Aun no hay actividad de consultores.</p>
          ) : (
            <div className="table-wrap">
              <table className="kpi-table">
                <thead>
                  <tr>
                    <th>Consultor</th>
                    <th>Ventas</th>
                    <th>Monto total</th>
                    <th>Llamadas</th>
                    <th>Minutos</th>
                    <th>Efectividad</th>
                    <th>Ticket prom.</th>
                  </tr>
                </thead>
                <tbody>
                  {consultantBoard.map((c) => (
                    <tr key={c.name}>
                      <td>{c.name}</td>
                      <td>{c.salesCount}</td>
                      <td>${c.salesAmount.toFixed(2)}</td>
                      <td>{c.callsCount}</td>
                      <td>{c.callsMinutes}</td>
                      <td>{(c.effectiveness * 100).toFixed(1)}%</td>
                      <td>${c.avgTicket.toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Actividad reciente para seguimiento operativo */}
          <h3>Ultimas ventas registradas</h3>
          <div className="list">
            {sales.slice(0, 6).map((s) => (
              <p className="list-item" key={s.id}>
                {s.consultantName} {'->'} ${s.amount} | cliente: {s.clientName}
              </p>
            ))}
          </div>
        </section>
      )}
      </div>
    </main>
  );
}

function CreateUserForm({ token, roles, onCreated }: { token: string; roles: Role[]; onCreated: (u: User) => void }) {
  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    // Envia el alta al backend y agrega el usuario al listado local.
    const created = await api<User>('/users', token, {
      method: 'POST',
      body: JSON.stringify({
        username: form.get('username'),
        password: form.get('password'),
        email: form.get('email'),
        roles: [form.get('role')],
      }),
    });
    onCreated(created);
    e.currentTarget.reset();
  };
  return (
    <form className="row form-grid" onSubmit={submit}>
      <input name="username" placeholder="Usuario" />
      <input name="email" placeholder="Correo" />
      <input name="password" type="password" placeholder="Contrasena" />
      <select name="role">{roles.map((r) => <option key={r.id} value={r.name}>{r.name}</option>)}</select>
      <button type="submit">Crear</button>
    </form>
  );
}

function InventoryForm({ token, onCreated }: { token: string; onCreated: () => void | Promise<void> }) {
  const [message, setMessage] = useState('');
  const [saving, setSaving] = useState(false);

  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formElement = e.currentTarget;
    setMessage('');
    setSaving(true);
    const f = new FormData(formElement);
    try {
      // Convierte los campos numericos antes de enviarlos al backend.
      await api<Inventory>('/inventory', token, {
        method: 'POST',
        body: JSON.stringify({
          name: f.get('name'),
          quantity: Number(f.get('quantity')),
          minStock: Number(f.get('minStock')),
          maxStock: Number(f.get('maxStock')),
          unit: f.get('unit'),
          price: Number(f.get('price') || 0),
          description: f.get('description'),
        }),
      });
      await onCreated();
      formElement.reset();
      setMessage('Insumo guardado en inventario.');
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'No se pudo guardar el insumo.');
    } finally {
      setSaving(false);
    }
  };
  return (
    <form className="row" onSubmit={submit}>
      <input name="name" placeholder="Insumo" required />
      <input name="quantity" type="number" placeholder="Cantidad" min="0" required />
      <input name="minStock" type="number" placeholder="Stock minimo" min="0" required />
      <input name="maxStock" type="number" placeholder="Stock maximo" min="0" />
      <input name="unit" placeholder="Unidad" />
      <input name="price" type="number" placeholder="Precio" min="0" step="0.01" />
      <input name="description" placeholder="Descripcion" />
      <button type="submit" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</button>
      {message && <small className="form-message">{message}</small>}
    </form>
  );
}

function SalesForm({ token, onCreated }: { token: string; onCreated: (s: Sale) => void }) {
  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    // Registra una venta del consultor autenticado.
    const created = await api<Sale>('/sales', token, {
      method: 'POST',
      body: JSON.stringify({ clientName: f.get('clientName'), amount: Number(f.get('amount')), description: f.get('description') }),
    });
    onCreated(created);
    e.currentTarget.reset();
  };
  return (
    <form className="row form-grid" onSubmit={submit}>
      <input name="clientName" placeholder="Cliente venta" />
      <input name="amount" type="number" placeholder="Monto" />
      <input name="description" placeholder="Descripcion" />
      <button type="submit">Registrar venta</button>
    </form>
  );
}

function CallsForm({ token, onCreated }: { token: string; onCreated: (c: Call) => void }) {
  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    // Registra una llamada del consultor autenticado.
    const created = await api<Call>('/calls', token, {
      method: 'POST',
      body: JSON.stringify({
        clientName: f.get('clientName'),
        notes: f.get('notes'),
        callType: f.get('callType'),
        durationMinutes: Number(f.get('duration')),
      }),
    });
    onCreated(created);
    e.currentTarget.reset();
  };
  return (
    <form className="row form-grid" onSubmit={submit}>
      <input name="clientName" placeholder="Cliente llamada" />
      <input name="callType" placeholder="Tipo" />
      <input name="duration" type="number" placeholder="Duracion min" />
      <input name="notes" placeholder="Notas" />
      <button type="submit">Registrar llamada</button>
    </form>
  );
}

export default App;
