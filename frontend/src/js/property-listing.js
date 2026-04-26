// Use shared data from common.js
let agentListings = [...globalProperties];

const listingsTableBody = document.getElementById('listingsTableBody');
const listingModal = document.getElementById('listingModal');
const closeModalBtn = document.getElementById('closeModalBtn');
const addListingBtn = document.getElementById('addListingBtn');
const listingForm = document.getElementById('listingForm');

let isEditMode = false;
let currentEditingId = null;

function renderListings() {
    listingsTableBody.innerHTML = '';
    agentListings.forEach(listing => {
        let statusBadge = '';
        if (listing.status === 'Active') statusBadge = '<span class="badge" style="background: green; color: white;">Active</span>';
        else if (listing.status === 'Under Auction') statusBadge = '<span class="badge" style="background: var(--primary-gold); color: white;">Auction</span>';
        else statusBadge = '<span class="badge" style="background: gray; color: white;">Sold</span>';

        listingsTableBody.innerHTML += `
            <tr>
                <td>${listing.id}</td>
                <td>${listing.title}</td>
                <td>${listing.sector}</td>
                <td>${listing.type}</td>
                <td>${formatCurrency(listing.price)}</td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn btn-outline" style="padding: 3px 8px; font-size: 12px;" onclick="editListing('${listing.id}')">Modify (UC5)</button>
                    <button class="btn btn-outline" style="padding: 3px 8px; font-size: 12px; color: red; border-color: red;" onclick="deleteListing('${listing.id}')">Delete</button>
                </td>
            </tr>
        `;
    });
}

// Initial Render
renderListings();

// Modal Logic
addListingBtn.addEventListener('click', () => {
    isEditMode = false;
    currentEditingId = null;
    document.getElementById('modalTitle').innerText = 'Add Property Listing (UC3)';
    listingForm.reset();
    listingModal.style.display = 'block';
});

closeModalBtn.addEventListener('click', () => {
    listingModal.style.display = 'none';
});

function editListing(id) {
    const listing = agentListings.find(l => l.id === id);
    if(listing) {
        isEditMode = true;
        currentEditingId = id;
        document.getElementById('modalTitle').innerText = 'Modify Property Listing (UC5)';
        
        document.getElementById('propTitle').value = listing.title;
        document.getElementById('propSector').value = listing.sector;
        document.getElementById('propType').value = listing.type;
        document.getElementById('propPrice').value = listing.price;
        document.getElementById('propBeds').value = listing.beds;
        document.getElementById('propBaths').value = listing.baths;
        
        listingModal.style.display = 'block';
    }
}

function deleteListing(id) {
    if(confirm('Are you sure you want to delete this listing?')) {
        agentListings = agentListings.filter(l => l.id !== id);
        renderListings();
    }
}

// Form Submit (UC3/UC5 Logic)
listingForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    const newListing = {
        id: isEditMode ? currentEditingId : 'LST-' + Math.floor(Math.random() * 1000),
        title: document.getElementById('propTitle').value,
        sector: document.getElementById('propSector').value,
        type: document.getElementById('propType').value,
        price: document.getElementById('propPrice').value,
        beds: document.getElementById('propBeds').value,
        baths: document.getElementById('propBaths').value,
        status: 'Active'
    };

    if(isEditMode) {
        // UC5 Modify
        const index = agentListings.findIndex(l => l.id === currentEditingId);
        if(index !== -1) {
            agentListings[index] = { ...agentListings[index], ...newListing };
            alert('Listing updated successfully! (UC5)');
        }
    } else {
        // UC3 Add
        // Mock UC2 verification (Check if sector is frozen)
        if(newListing.sector === 'F-8') {
            alert('Cannot add listing. Sector F-8 is currently frozen by the Authority. (Enforcing UC2)');
            return;
        }
        
        agentListings.push(newListing);
        alert('Listing added successfully! (UC3)');
    }

    listingModal.style.display = 'none';
    renderListings();
});
