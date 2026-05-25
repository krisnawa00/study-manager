// app.js — StudyManager frontend application logic

// ── Service Worker registration ───────────────────────────────────────────────
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/service-worker.js')
      .then(reg => console.log('SW registered', reg.scope))
      .catch(err => console.warn('SW failed', err));
  });
}

// ── State ─────────────────────────────────────────────────────────────────────
let currentUser  = null;
let coursesCache = [];

// ── Boot ──────────────────────────────────────────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
  if (Api.isLoggedIn()) {
    showMainApp();
  } else {
    showScreen('auth-screen');
  }

  document.getElementById('login-form').addEventListener('submit',    onLogin);
  document.getElementById('register-form').addEventListener('submit', onRegister);
  document.getElementById('course-form').addEventListener('submit',   onSaveCourse);
  document.getElementById('task-form').addEventListener('submit',     onSaveTask);
  document.getElementById('profile-form').addEventListener('submit',  onSaveProfile);
});

// ── Auth handlers ─────────────────────────────────────────────────────────────
async function onLogin(e) {
  e.preventDefault();
  const email    = document.getElementById('login-email').value;
  const password = document.getElementById('login-password').value;
  try {
    const res = await Api.auth.login({ email, password });
    currentUser = res.user;
    showMainApp();
  } catch (err) {
    showError('auth-error', err.message);
  }
}

async function onRegister(e) {
  e.preventDefault();
  try {
    const res = await Api.auth.register({
      firstName: document.getElementById('reg-first').value,
      lastName:  document.getElementById('reg-last').value,
      email:     document.getElementById('reg-email').value,
      password:  document.getElementById('reg-password').value
    });
    currentUser = res.user;
    showMainApp();
  } catch (err) {
    showError('auth-error', err.message);
  }
}

function logout() {
  Api.auth.logout();
  currentUser = null;
  showScreen('auth-screen');
}

// ── App init ──────────────────────────────────────────────────────────────────
async function showMainApp() {
  showScreen('main-screen');
  if (!currentUser) {
    try { currentUser = await Api.users.getProfile(); } catch { logout(); return; }
  }
  const name = currentUser.firstName;
  document.getElementById('user-name-nav').textContent     = name;
  document.getElementById('user-name-sidebar').textContent = currentUser.firstName + ' ' + currentUser.lastName;
  document.getElementById('user-greeting').textContent     = name;
  document.querySelectorAll('.admin-only').forEach(el => {
      el.classList.toggle('hidden', currentUser.role !== 'ADMIN');
  });
  showPage('dashboard');
}

// ── Navigation ────────────────────────────────────────────────────────────────
function showPage(name) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
  document.getElementById(`page-${name}`).classList.add('active');
  document.querySelectorAll('.nav-btn')[[
      'dashboard','courses','tasks','profile','admin'
  ].indexOf(name)]?.classList.add('active');

  const loaders = { dashboard: loadDashboard, courses: loadCourses, tasks: loadTasks, profile: loadProfile, admin: loadAdmin };
  loaders[name]?.();
}

// ── Dashboard ─────────────────────────────────────────────────────────────────
async function loadDashboard() {
  try {
    const [stats, today] = await Promise.all([
      Api.dashboard.getStats(),
      Api.tasks.getDueToday()
    ]);
    document.querySelector('#stat-courses .stat-num').textContent = stats.totalCourses;
    document.querySelector('#stat-tasks .stat-num').textContent   = stats.totalTasks;
    document.querySelector('#stat-overdue .stat-num').textContent = stats.overdueTasks;
    document.querySelector('#stat-done .stat-num').textContent    = stats.doneTasks;

    const list = document.getElementById('due-today-list');
    list.innerHTML = today.length
      ? today.map(taskCard).join('')
      : '<div class="empty-state"><div class="empty-icon">🎉</div>Šodien nav uzdevumu</div>';
  } catch (e) { toast('Neizdevās ielādēt datus'); }
}

// ── Courses ───────────────────────────────────────────────────────────────────
async function loadCourses() {
  try {
    coursesCache = await Api.courses.getAll();
    renderCourses();
  } catch { toast('Neizdevās ielādēt kursus'); }
}

function renderCourses() {
  document.getElementById('courses-list').innerHTML = coursesCache.length
    ? coursesCache.map(courseCard).join('')
    : '<div class="empty-state"><div class="empty-icon">📖</div>Nav pievienotu kursu</div>';
}

function courseCard(c) {
  return `
  <div class="course-card">
    <div class="course-dot" style="background:${c.color}"></div>
    <div class="course-info">
      <div class="course-name">${esc(c.name)}</div>
      <div class="course-meta">${c.instructor || '—'} · ${c.credits || '?'} KP · ${c.status}</div>
      <div class="progress-bar"><div class="progress-fill" style="width:${c.completionPercent}%"></div></div>
      <div class="course-meta">${c.doneTasks}/${c.totalTasks} uzdevumi · ${Math.round(c.completionPercent)}%</div>
    </div>
    <div class="course-actions">
      <button class="icon-btn" title="Labot" onclick="openCourseModal(${JSON.stringify(c).split('"').join('&quot;')})">✏️</button>
      <button class="icon-btn" title="Dzēst" onclick="deleteCourse(${c.id})">🗑️</button>
    </div>
  </div>`;
}

function openCourseModal(course) {
  document.getElementById('course-id').value         = course?.id || '';
  document.getElementById('course-name').value       = course?.name || '';
  document.getElementById('course-instructor').value = course?.instructor || '';
  document.getElementById('course-credits').value    = course?.credits || '';
  document.getElementById('course-category').value   = course?.category || 'MANDATORY';
  document.getElementById('course-color').value      = course?.color || '#2E75B6';
  document.getElementById('course-desc').value       = course?.description || '';
  document.getElementById('course-modal-title').textContent = course ? 'Labot kursu' : 'Jauns kurss';
  openModal('course-modal');
}

async function onSaveCourse(e) {
  e.preventDefault();
  const id = document.getElementById('course-id').value;
  const data = {
    name:        document.getElementById('course-name').value,
    instructor:  document.getElementById('course-instructor').value,
    credits:     parseInt(document.getElementById('course-credits').value) || null,
    category:    document.getElementById('course-category').value,
    color:       document.getElementById('course-color').value,
    description: document.getElementById('course-desc').value
  };
  try {
    if (id) { await Api.courses.update(id, data); }
    else    { await Api.courses.create(data); }
    closeModal('course-modal');
    await loadCourses();
    toast(id ? 'Kurss atjaunināts' : 'Kurss pievienots');
  } catch (err) { toast('Kļūda: ' + err.message); }
}

async function deleteCourse(id) {
  if (!confirm('Dzēst kursu un visus tā uzdevumus?')) return;
  try { await Api.courses.delete(id); await loadCourses(); toast('Kurss dzēsts'); }
  catch (e) { toast('Kļūda: ' + e.message); }
}

// ── Tasks ─────────────────────────────────────────────────────────────────────
async function loadTasks() {
  try {
    const tasks  = await Api.tasks.getAll();
    const filter = document.getElementById('task-filter-status').value;
    const filtered = filter ? tasks.filter(t => t.status === filter) : tasks;
    document.getElementById('tasks-list').innerHTML = filtered.length
      ? filtered.map(taskCard).join('')
      : '<div class="empty-state"><div class="empty-icon">✅</div>Nav uzdevumu</div>';
  } catch { toast('Neizdevās ielādēt uzdevumus'); }
}

function taskCard(t) {
  const done    = t.status === 'DONE';
  const overdue = t.overdue ? 'overdue' : '';
  const dueText = t.dueDate ? `📅 ${t.dueDate}` : '';
  const badge   = t.courseName
    ? `<span class="badge-course" style="background:${t.courseColor}">${esc(t.courseName)}</span>` : '';

  return `
  <div class="task-card priority-${t.priority} ${done ? 'done' : ''}">
    <div class="task-check ${done ? 'checked' : ''}"
         onclick="toggleTask(${t.id},'${done ? 'TODO' : 'DONE'}')"></div>
    <div class="task-body">
      <div class="task-title ${done ? 'done-text' : ''}">${esc(t.title)}</div>
      <div class="task-meta">
        <span>${priorityLabel(t.priority)}</span>
        ${dueText ? `<span class="task-due ${overdue}">${dueText}</span>` : ''}
        ${badge}
      </div>
    </div>
    <div class="task-actions">
      <button class="icon-btn" title="Labot" onclick="openTaskModal(${t.id})">✏️</button>
      <button class="icon-btn" title="Dzēst" onclick="deleteTask(${t.id})">🗑️</button>
    </div>
  </div>`;
}

async function toggleTask(id, newStatus) {
  try {
    await Api.tasks.updateStatus(id, newStatus);
    loadTasks();
  } catch { toast('Kļūda atjauninot statusu'); }
}

async function openTaskModal(idOrObj) {
  if (!coursesCache.length) coursesCache = await Api.courses.getAll().catch(() => []);
  const sel = document.getElementById('task-course');
  sel.innerHTML = '<option value="">Bez kursa</option>' +
    coursesCache.map(c => `<option value="${c.id}">${esc(c.name)}</option>`).join('');

  let task = null;
  if (typeof idOrObj === 'number') {
    const all = await Api.tasks.getAll().catch(() => []);
    task = all.find(t => t.id === idOrObj) || null;
  }

  document.getElementById('task-id').value       = task?.id || '';
  document.getElementById('task-title').value    = task?.title || '';
  document.getElementById('task-desc').value     = task?.description || '';
  document.getElementById('task-priority').value = task?.priority || 'MEDIUM';
  document.getElementById('task-course').value   = task?.courseId || '';
  document.getElementById('task-due').value      = task?.dueDate || '';
  document.getElementById('task-modal-title').textContent = task ? 'Labot uzdevumu' : 'Jauns uzdevums';
  openModal('task-modal');
}

async function onSaveTask(e) {
  e.preventDefault();
  const id       = document.getElementById('task-id').value;
  const courseId = document.getElementById('task-course').value;
  const data = {
    title:       document.getElementById('task-title').value,
    description: document.getElementById('task-desc').value,
    priority:    document.getElementById('task-priority').value,
    courseId:    courseId ? parseInt(courseId) : null,
    dueDate:     document.getElementById('task-due').value || null
  };
  try {
    if (id) { await Api.tasks.update(id, data); }
    else    { await Api.tasks.create(data); }
    closeModal('task-modal');
    loadTasks();
    toast(id ? 'Uzdevums atjaunināts' : 'Uzdevums pievienots');
  } catch (err) { toast('Kļūda: ' + err.message); }
}

async function deleteTask(id) {
  if (!confirm('Dzēst uzdevumu?')) return;
  try { await Api.tasks.delete(id); loadTasks(); toast('Uzdevums dzēsts'); }
  catch (e) { toast('Kļūda: ' + e.message); }
}

// ── Profile ───────────────────────────────────────────────────────────────────
async function loadProfile() {
  if (!currentUser) currentUser = await Api.users.getProfile();
  document.getElementById('prof-first').value = currentUser.firstName;
  document.getElementById('prof-last').value  = currentUser.lastName;
  document.getElementById('prof-email').value = currentUser.email;

  try {
    const ach = await Api.dashboard.getAchievements();
    document.getElementById('achievements-list').innerHTML = ach.length
      ? ach.map(a => `
          <div class="achievement-badge">
            <span class="badge-icon">${badgeIcon(a.badgeType)}</span>
            ${esc(a.badgeType)}
          </div>`).join('')
      : '<p style="color:var(--text-muted)">Vēl nav sasniegumu</p>';
  } catch {}
}

async function onSaveProfile(e) {
  e.preventDefault();
  try {
    currentUser = await Api.users.updateProfile({
      firstName: document.getElementById('prof-first').value,
      lastName:  document.getElementById('prof-last').value
    });
    document.getElementById('user-name-nav').textContent     = currentUser.firstName;
    document.getElementById('user-name-sidebar').textContent = currentUser.firstName + ' ' + currentUser.lastName;
    toast('Profils saglabāts');
  } catch (err) { toast('Kļūda: ' + err.message); }
}

// ── Utilities ─────────────────────────────────────────────────────────────────
function showScreen(id) {
  document.querySelectorAll('.screen').forEach(s => {
    s.classList.remove('active');
    s.classList.add('hidden');
  });
  const el = document.getElementById(id);
  el.classList.remove('hidden');
  el.classList.add('active');
}

function showTab(name) {
  document.getElementById('login-form').classList.toggle('hidden', name !== 'login');
  document.getElementById('register-form').classList.toggle('hidden', name !== 'register');
  document.querySelectorAll('.tab').forEach((t, i) =>
    t.classList.toggle('active', (i === 0) === (name === 'login'))
  );
}

function openModal(id) {
  const el = document.getElementById(id);
  el.classList.remove('hidden');
  el.classList.add('active');
}
function closeModal(id) {
  const el = document.getElementById(id);
  el.classList.remove('active');
  el.classList.add('hidden');
}

function showError(id, msg) {
  const el = document.getElementById(id);
  el.textContent = msg;
  el.classList.remove('hidden');
}

let toastTimer;
function toast(msg) {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.classList.remove('hidden');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.add('hidden'), 3000);
}

function esc(str) {
  return String(str || '')
    .replace(/&/g,'&amp;').replace(/</g,'&lt;')
    .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function priorityLabel(p) {
  return { LOW:'🔵 Zema', MEDIUM:'🟡 Vidēja', HIGH:'🟠 Augsta', CRITICAL:'🔴 Kritiska' }[p] || p;
}

function badgeIcon(type) {
  return {
    FIRST_TASK:'🌱', WEEK_STREAK:'🔥', COURSE_COMPLETE:'🎓',
    EARLY_BIRD:'🐦', CENTURY:'💯', PERFECT_WEEK:'⭐'
  }[type] || '🏅';
}

async function loadAdmin() {
    if (currentUser?.role !== 'ADMIN') {
        showPage('dashboard');
        toast('Piekļuve liegta');
        return;
    }
    try {
        const [stats, users] = await Promise.all([
            Api.admin.getStats(),
            Api.admin.getUsers()
        ]);
        document.querySelector('#admin-stat-users   .stat-num').textContent = stats.totalUsers;
        document.querySelector('#admin-stat-courses .stat-num').textContent = stats.totalCourses;
        document.querySelector('#admin-stat-done    .stat-num').textContent = stats.doneTasks;
        document.querySelector('#admin-stat-overdue .stat-num').textContent = stats.overdueTasks;

        document.getElementById('admin-users-list').innerHTML = users.length
            ? users.map(adminUserCard).join('')
            : '<div class="empty-state"><div class="empty-icon">👤</div>Nav lietotāju</div>';

        document.getElementById('role-form').onsubmit = onSaveRole;
    } catch (e) { toast('Kļūda: ' + e.message); }
}

function adminUserCard(u) {
    const initials  = (u.firstName[0] + u.lastName[0]).toUpperCase();
    const roleClass = u.role === 'ADMIN' ? 'admin' : 'student';
    const roleIcon  = u.role === 'ADMIN' ? '🛡️' : '👤';
    const isSelf    = currentUser?.email === u.email;

    return `
    <div class="admin-user-card">
      <div class="admin-user-avatar">${initials}</div>
      <div class="admin-user-info">
        <div class="admin-user-name">
          ${esc(u.firstName)} ${esc(u.lastName)}
          <span class="role-badge ${roleClass}">${roleIcon} ${u.role}</span>
        </div>
        <div class="admin-user-meta">
          ${esc(u.email)} · ${u.totalCourses} kursi · ${u.totalTasks} uzd.
        </div>
      </div>
      <div class="admin-user-actions">
        <button class="icon-btn"
                onclick="openRoleModal(${u.id},'${esc(u.firstName)} ${esc(u.lastName)}','${u.role}')">
          ✏️
        </button>
        ${!isSelf ? `<button class="icon-btn"
                onclick="adminDeleteUser(${u.id},'${esc(u.firstName)} ${esc(u.lastName)}')">
          🗑️
        </button>` : ''}
      </div>
    </div>`;
}

function openRoleModal(userId, userName, currentRole) {
    document.getElementById('role-user-id').value         = userId;
    document.getElementById('role-user-name').textContent = 'Lietotājs: ' + userName;
    document.getElementById('role-select').value           = currentRole;
    openModal('role-modal');
}

async function onSaveRole(e) {
    e.preventDefault();
    const userId = document.getElementById('role-user-id').value;
    const role   = document.getElementById('role-select').value;
    try {
        await Api.admin.changeRole(userId, { role });
        closeModal('role-modal');
        await loadAdmin();
        toast('Loma mainīta uz ' + role);
    } catch (err) { toast('Kļūda: ' + err.message); }
}

async function adminDeleteUser(id, name) {
    if (!confirm(`Dzēst lietotāju "${name}" un visus viņa datus?`)) return;
    try {
        await Api.admin.deleteUser(id);
        await loadAdmin();
        toast('Lietotājs dzēsts');
    } catch (e) { toast('Kļūda: ' + e.message); }
}