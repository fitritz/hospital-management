const apiStatus = document.getElementById('apiStatus');
const patientCount = document.getElementById('patientCount');
const doctorCount = document.getElementById('doctorCount');
const appointmentCount = document.getElementById('appointmentCount');

const patientsList = document.getElementById('patientsList');
const doctorsList = document.getElementById('doctorsList');
const appointmentsList = document.getElementById('appointmentsList');

const patientSelect = document.getElementById('patientSelect');
const doctorSelect = document.getElementById('doctorSelect');

const template = document.getElementById('rowTemplate');

const state = { patients: [], doctors: [], appointments: [] };

function formatAppointment(appointment) {
  const patient = state.patients.find((item) => item.id === appointment.patientId);
  const doctor = state.doctors.find((item) => item.id === appointment.doctorId);
  return `${patient ? patient.name : `Patient #${appointment.patientId}`} · ${doctor ? doctor.name : `Doctor #${appointment.doctorId}`} · ${appointment.scheduledAt}`;
}

function renderList(container, items, titleFn, bodyFn, deleteFn) {
  container.innerHTML = '';
  if (!items.length) {
    container.innerHTML = '<div class="row-item"><div class="row-main"><strong>No records yet</strong><p>Add one using the form above.</p></div></div>';
    return;
  }

  items.forEach((item) => {
    const node = template.content.cloneNode(true);
    node.querySelector('strong').textContent = titleFn(item);
    node.querySelector('p').textContent = bodyFn(item);
    const deleteButton = node.querySelector('.delete');
    deleteButton.addEventListener('click', async () => {
      await deleteFn(item.id);
      await loadAll();
    });
    container.appendChild(node);
  });
}

function syncSelect(select, items, labelFn, placeholder) {
  select.innerHTML = `<option value="">${placeholder}</option>`;
  items.forEach((item) => {
    const option = document.createElement('option');
    option.value = item.id;
    option.textContent = labelFn(item);
    select.appendChild(option);
  });
}

async function api(path, options) {
  const response = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `${response.status} ${response.statusText}`);
  }
  if (response.status === 204) return null;
  return response.json();
}

async function loadAll() {
  try {
    apiStatus.textContent = 'Loading';
    const [patients, doctors, appointments] = await Promise.all([
      api('/api/patients'),
      api('/api/doctors'),
      api('/api/appointments')
    ]);

    state.patients = patients;
    state.doctors = doctors;
    state.appointments = appointments;

    patientCount.textContent = patients.length;
    doctorCount.textContent = doctors.length;
    appointmentCount.textContent = appointments.length;

    apiStatus.textContent = 'Online';
    syncSelect(patientSelect, patients, (item) => `${item.name} (#${item.id})`, 'Choose patient');
    syncSelect(doctorSelect, doctors, (item) => `${item.name} (#${item.id})`, 'Choose doctor');

    renderList(patientsList, patients, (item) => `${item.name} · ${item.age}`, (item) => `${item.gender} · ${item.contact}`, (id) => api(`/api/patients/${id}`, { method: 'DELETE' }));
    renderList(doctorsList, doctors, (item) => `${item.name} · ${item.specialization}`, (item) => item.contact, (id) => api(`/api/doctors/${id}`, { method: 'DELETE' }));
    renderList(appointmentsList, appointments, (item) => `Appointment #${item.id}`, (item) => formatAppointment(item), (id) => api(`/api/appointments/${id}`, { method: 'DELETE' }));
  } catch (error) {
    apiStatus.textContent = 'Offline';
    const message = `<div class="row-item"><div class="row-main"><strong>Could not load data</strong><p>${error.message}</p></div></div>`;
    patientsList.innerHTML = message;
    doctorsList.innerHTML = message;
    appointmentsList.innerHTML = message;
  }
}

document.getElementById('reloadPatients').addEventListener('click', loadAll);
document.getElementById('reloadDoctors').addEventListener('click', loadAll);
document.getElementById('reloadAppointments').addEventListener('click', loadAll);

document.getElementById('patientForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const formData = new FormData(event.currentTarget);
  await api('/api/patients', {
    method: 'POST',
    body: JSON.stringify({
      name: formData.get('name'),
      age: Number(formData.get('age')),
      gender: formData.get('gender'),
      contact: formData.get('contact')
    })
  });
  event.currentTarget.reset();
  await loadAll();
});

document.getElementById('doctorForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const formData = new FormData(event.currentTarget);
  await api('/api/doctors', {
    method: 'POST',
    body: JSON.stringify({
      name: formData.get('name'),
      specialization: formData.get('specialization'),
      contact: formData.get('contact')
    })
  });
  event.currentTarget.reset();
  await loadAll();
});

document.getElementById('appointmentForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const formData = new FormData(event.currentTarget);
  await api('/api/appointments', {
    method: 'POST',
    body: JSON.stringify({
      patientId: Number(formData.get('patientId')),
      doctorId: Number(formData.get('doctorId')),
      scheduledAt: formData.get('scheduledAt')
    })
  });
  event.currentTarget.reset();
  await loadAll();
});

loadAll();