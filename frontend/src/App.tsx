import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import './App.css';

const API_URL = 'http://localhost:8080/api';

type User = { id: number; username: string; email: string; active: boolean; roles: string[] };
type Role = { id: number; name: string; description: string };
type Inventory = { id: number; name: string; quantity: number; minStock: number; unit: string; description: string };
type Sale = { id: number; clientName: string; amount: number; saleDate: string; description: string; consultantName: string };
type Call = { id: number; clientName: string; callDate: string; notes: string; callType: string; durationMinutes: number; consultantName: string };
type Notification = { id: number; message: string; type: string; read: boolean; createdAt: string };

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

  useEffect(() => {
    if (!token) return;
    localStorage.setItem('token', token);
    Promise.all([
      api<User>('/auth/me', token).then(setMe),
      api<Notification[]>('/notifications', token).then(setNotifications),
    ]).catch(() => logout());
  }, [token]);

  useEffect(() => {
    if (!token || !role) return;
    if (role === 'ADMIN') {
      Promise.all([api<User[]>('/users', token).then(setUsers), api<Role[]>('/users/roles', token).then(setRoles)]);
    }
    if (role === 'SUPERVISOR' || role === 'DIRECTOR' || role === 'ADMIN') api<Inventory[]>('/inventory', token).then(setInventory);
    if (role === 'CONSULTOR' || role === 'DIRECTOR' || role === 'ADMIN') {
      Promise.all([api<Sale[]>('/sales', token).then(setSales), api<Call[]>('/calls', token).then(setCalls)]);
    }
    if (role === 'DIRECTOR' || role === 'ADMIN') api<Record<string, number>>('/activities/summary', token).then(setSummary);
  }, [role, token]);

  const logout = () => {
    setToken('');
    setMe(null);
    localStorage.removeItem('token');
  };

  const login = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    const form = new FormData(event.currentTarget);
    try {
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
      <main className="container">
        <h1>Yovendo</h1>
        <p>Inicia sesion para acceder segun tu perfil.</p>
        <form onSubmit={login} className="card">
          <input name="username" placeholder="Usuario" />
          <input name="password" type="password" placeholder="Contrasena" />
          <button type="submit">Entrar</button>
          {error && <small>{error}</small>}
        </form>
      </main>
    );
  }

  return (
    <main className="container">
      <header className="row">
        <div>
          <h1>Yovendo Panel</h1>
          <p>{me?.username} | perfil: {role}</p>
        </div>
        <button onClick={logout}>Cerrar sesion</button>
      </header>

      <section className="card">
        <h2>Notificaciones</h2>
        {notifications.length === 0 ? <p>Sin notificaciones.</p> : notifications.map((n) => <p key={n.id}>[{n.type}] {n.message}</p>)}
      </section>

      {role === 'ADMIN' && (
        <section className="card">
          <h2>Administracion de usuarios</h2>
          <CreateUserForm token={token} roles={roles} onCreated={(u) => setUsers((prev) => [u, ...prev])} />
          {users.map((u) => <p key={u.id}>{u.username} | {u.email} | {u.active ? 'Activo' : 'Inactivo'} | {u.roles.join(', ')}</p>)}
        </section>
      )}

      {(role === 'SUPERVISOR' || role === 'ADMIN') && (
        <section className="card">
          <h2>Control de inventario</h2>
          <InventoryForm token={token} onCreated={(item) => setInventory((prev) => [item, ...prev])} />
          {inventory.map((i) => <p key={i.id}>{i.name}: {i.quantity} {i.unit} (min: {i.minStock})</p>)}
        </section>
      )}

      {role === 'CONSULTOR' && (
        <section className="card">
          <h2>Historial de ventas y llamadas</h2>
          <SalesForm token={token} onCreated={(item) => setSales((prev) => [item, ...prev])} />
          <CallsForm token={token} onCreated={(item) => setCalls((prev) => [item, ...prev])} />
          {sales.map((s) => <p key={s.id}>Venta {s.clientName} - ${s.amount}</p>)}
          {calls.map((c) => <p key={c.id}>Llamada {c.clientName} - {c.durationMinutes} min</p>)}
        </section>
      )}

      {role === 'DIRECTOR' && (
        <section className="card">
          <h2>Seguimiento de directores</h2>
          <p>Total ventas: {summary.totalSales ?? 0}</p>
          <p>Total llamadas: {summary.totalCalls ?? 0}</p>
          <p>Insumos en stock bajo: {summary.lowStockItems ?? 0}</p>
          {sales.slice(0, 5).map((s) => <p key={s.id}>Venta reciente: {s.consultantName} {'->'} ${s.amount}</p>)}
        </section>
      )}
    </main>
  );
}

function CreateUserForm({ token, roles, onCreated }: { token: string; roles: Role[]; onCreated: (u: User) => void }) {
  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
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
    <form className="row" onSubmit={submit}>
      <input name="username" placeholder="Usuario" />
      <input name="email" placeholder="Correo" />
      <input name="password" type="password" placeholder="Contrasena" />
      <select name="role">{roles.map((r) => <option key={r.id} value={r.name}>{r.name}</option>)}</select>
      <button type="submit">Crear</button>
    </form>
  );
}

function InventoryForm({ token, onCreated }: { token: string; onCreated: (i: Inventory) => void }) {
  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    const created = await api<Inventory>('/inventory', token, {
      method: 'POST',
      body: JSON.stringify({
        name: f.get('name'),
        quantity: Number(f.get('quantity')),
        minStock: Number(f.get('minStock')),
        unit: f.get('unit'),
        description: f.get('description'),
      }),
    });
    onCreated(created);
    e.currentTarget.reset();
  };
  return (
    <form className="row" onSubmit={submit}>
      <input name="name" placeholder="Insumo" />
      <input name="quantity" type="number" placeholder="Cantidad" />
      <input name="minStock" type="number" placeholder="Stock minimo" />
      <input name="unit" placeholder="Unidad" />
      <input name="description" placeholder="Descripcion" />
      <button type="submit">Guardar</button>
    </form>
  );
}

function SalesForm({ token, onCreated }: { token: string; onCreated: (s: Sale) => void }) {
  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    const created = await api<Sale>('/sales', token, {
      method: 'POST',
      body: JSON.stringify({ clientName: f.get('clientName'), amount: Number(f.get('amount')), description: f.get('description') }),
    });
    onCreated(created);
    e.currentTarget.reset();
  };
  return (
    <form className="row" onSubmit={submit}>
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
    <form className="row" onSubmit={submit}>
      <input name="clientName" placeholder="Cliente llamada" />
      <input name="callType" placeholder="Tipo" />
      <input name="duration" type="number" placeholder="Duracion min" />
      <input name="notes" placeholder="Notas" />
      <button type="submit">Registrar llamada</button>
    </form>
  );
}

export default App;
