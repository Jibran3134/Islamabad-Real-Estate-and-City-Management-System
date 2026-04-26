// Use shared globalUsers from common.js
let usersList = [...globalUsers];

const userTableBody = document.getElementById('userTableBody');
const userSearch = document.getElementById('userSearch');
const userModal = document.getElementById('userModal');
const closeModalBtn = document.getElementById('closeModalBtn');
const addUserBtn = document.getElementById('addUserBtn');
const userForm = document.getElementById('userForm');

// Render Users
function renderUsers(usersToRender) {
    userTableBody.innerHTML = '';
    usersToRender.forEach(user => {
        let badgeColor = user.role === 'Agent' ? 'background: var(--bg-dark); color: var(--primary-gold);' : '';
        let statusColor = user.status === 'Suspended' ? 'color: red;' : 'color: green;';
        
        userTableBody.innerHTML += `
            <tr>
                <td>${user.id}</td>
                <td>${user.name}</td>
                <td>${user.email}</td>
                <td><span class="badge" style="position: static; padding: 3px 8px; font-size: 10px; ${badgeColor}">${user.role}</span></td>
                <td style="${statusColor}">${user.status}</td>
                <td>
                    <button class="btn btn-outline" style="padding: 3px 8px; font-size: 12px;" onclick="editUser('${user.id}')">Edit</button>
                    <button class="btn btn-outline" style="padding: 3px 8px; font-size: 12px; color: ${user.status === 'Active' ? 'red' : 'green'}; border-color: ${user.status === 'Active' ? 'red' : 'green'};" onclick="toggleStatus('${user.id}')">
                        ${user.status === 'Active' ? 'Suspend' : 'Activate'}
                    </button>
                </td>
            </tr>
        `;
    });
}

// Initial Render
renderUsers(usersList);

// Search Functionality
userSearch.addEventListener('input', (e) => {
    const term = e.target.value.toLowerCase();
    const filtered = usersList.filter(u => u.name.toLowerCase().includes(term) || u.email.toLowerCase().includes(term) || u.id.toLowerCase().includes(term));
    renderUsers(filtered);
});

// Modal Logic
addUserBtn.addEventListener('click', () => {
    document.getElementById('modalTitle').innerText = 'Add New User';
    userForm.reset();
    userModal.style.display = 'block';
});

closeModalBtn.addEventListener('click', () => {
    userModal.style.display = 'none';
});

// Toggle Status Logic
function toggleStatus(id) {
    const user = usersList.find(u => u.id === id);
    if(user) {
        user.status = user.status === 'Active' ? 'Suspended' : 'Active';
        renderUsers(usersList); // Re-render
    }
}

// Edit User Logic
function editUser(id) {
    const user = usersList.find(u => u.id === id);
    if(user) {
        document.getElementById('modalTitle').innerText = 'Edit User';
        document.getElementById('userName').value = user.name;
        document.getElementById('userEmail').value = user.email;
        document.getElementById('userRole').value = user.role;
        document.getElementById('userStatus').value = user.status;
        userModal.style.display = 'block';
    }
}

// Form Submit
userForm.addEventListener('submit', (e) => {
    e.preventDefault();
    alert('User saved! (Backend logic to be implemented)');
    userModal.style.display = 'none';
});
