// Use shared data from common.js
let authoritySectors = [...globalSectors];

const sectorsContainer = document.getElementById('sectorsContainer');
const sectorModal = document.getElementById('sectorModal');
const closeModalBtn = document.getElementById('closeModalBtn');
const sectorForm = document.getElementById('sectorForm');
let currentEditingSectorId = null;

function renderSectors() {
    sectorsContainer.innerHTML = '';
    authoritySectors.forEach(sector => {
        const capacityPercentage = (sector.current / sector.capacity) * 100;
        const statusClass = sector.isFrozen ? 'frozen-sector' : 'active-sector';
        const freezeActionText = sector.isFrozen ? 'Unfreeze Sector' : 'Freeze Sector (UC2)';
        const freezeActionColor = sector.isFrozen ? 'green' : 'red';

        sectorsContainer.innerHTML += `
            <div class="sector-card ${statusClass}">
                <div style="display: flex; justify-content: space-between;">
                    <h3 style="margin: 0;">${sector.name}</h3>
                    <span class="badge" style="background: ${sector.isFrozen ? 'red' : 'green'}; color: white;">${sector.isFrozen ? 'FROZEN' : 'ACTIVE'}</span>
                </div>
                <div style="margin: 15px 0;">
                    <p style="margin: 5px 0;"><strong>Capacity Limit:</strong> ${sector.capacity} properties</p>
                    <p style="margin: 5px 0;"><strong>Current Listings:</strong> ${sector.current} properties</p>
                    <div style="width: 100%; background: #eee; height: 10px; border-radius: 5px; margin-top: 10px; overflow: hidden;">
                        <div style="width: ${Math.min(capacityPercentage, 100)}%; background: ${capacityPercentage > 90 ? 'red' : 'var(--primary-gold)'}; height: 100%;"></div>
                    </div>
                    <small style="color: var(--text-muted);">${capacityPercentage.toFixed(1)}% full</small>
                </div>
                <div style="display: flex; gap: 10px; margin-top: 20px;">
                    <button class="btn btn-outline" style="flex: 1;" onclick="openLimitModal('${sector.id}')">Set Limits (UC1)</button>
                    <button class="btn btn-outline" style="flex: 1; border-color: ${freezeActionColor}; color: ${freezeActionColor};" onclick="toggleFreeze('${sector.id}')">${freezeActionText}</button>
                </div>
            </div>
        `;
    });
}

function openLimitModal(id) {
    const sector = authoritySectors.find(s => s.id === id);
    if(sector) {
        currentEditingSectorId = sector.id;
        document.getElementById('sectorName').value = sector.name;
        document.getElementById('sectorCapacity').value = sector.capacity;
        document.getElementById('currentProperties').value = sector.current;
        sectorModal.style.display = 'block';
    }
}

function toggleFreeze(id) {
    const sector = authoritySectors.find(s => s.id === id);
    if(sector) {
        sector.isFrozen = !sector.isFrozen;
        // Simulate cascading effect for UC2
        if(sector.isFrozen) {
            alert(`Sector ${sector.name} has been frozen. New listings are now blocked in this sector. Agents have been notified.`);
        } else {
            alert(`Sector ${sector.name} has been unfrozen. New listings are now permitted.`);
        }
        renderSectors();
    }
}

closeModalBtn.addEventListener('click', () => {
    sectorModal.style.display = 'none';
});

sectorForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const sector = authoritySectors.find(s => s.id === currentEditingSectorId);
    if(sector) {
        const newCapacity = parseInt(document.getElementById('sectorCapacity').value);
        sector.capacity = newCapacity;
        alert(`Capacity limit for ${sector.name} updated to ${newCapacity} successfully! (UC1 Logic)`);
        sectorModal.style.display = 'none';
        renderSectors();
    }
});

// Initial render
renderSectors();
