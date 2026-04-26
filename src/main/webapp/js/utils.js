// Shared Utilities
function formatCurrency(amount) {
    return 'Rs. ' + parseInt(amount).toLocaleString();
}

// Shared Mock Data
const globalUsers = [
    { id: '#USR-9982', name: 'Ahmed Khan', email: 'ahmed@email.com', role: 'Buyer', status: 'Active' },
    { id: '#USR-9983', name: 'Property Masters', email: 'contact@pm.com', role: 'Agent', status: 'Suspended' },
    { id: '#USR-9984', name: 'CDA Official', email: 'official@cda.gov.pk', role: 'Authority', status: 'Active' },
    { id: '#B-4092', name: 'Buyer 4092', email: 'buyer4092@email.com', role: 'Buyer', status: 'Active' }
];

const globalSectors = [
    { id: 'SEC-1', name: 'Sector F-7', capacity: 1500, current: 1450, isFrozen: false },
    { id: 'SEC-2', name: 'Sector F-8', capacity: 1200, current: 1250, isFrozen: true },
    { id: 'SEC-3', name: 'Sector E-7', capacity: 800, current: 600, isFrozen: false },
    { id: 'SEC-4', name: 'Sector G-11', capacity: 3000, current: 2800, isFrozen: false },
    { id: 'SEC-5', name: 'Sector D-12', capacity: 2000, current: 500, isFrozen: false }
];

const globalProperties = [
    { id: 'LST-101', title: 'Luxury Villa - F-7/2', sector: 'F-7', type: 'House', price: 85000000, beds: 5, baths: 6, area: '500 Sq Yd', status: 'Active', image: 'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&q=80&w=800' },
    { id: 'LST-102', title: 'Modern Apartment - E-7', sector: 'E-7', type: 'Apartment', price: 35000000, beds: 3, baths: 3, area: '2000 Sq Ft', status: 'Under Auction', image: 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&q=80&w=800' },
    { id: 'LST-103', title: 'Commercial Plaza - F-8 Markaz', sector: 'F-8', type: 'Commercial', price: 250000000, beds: 0, baths: 10, area: '1000 Sq Yd', status: 'Active', image: 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&q=80&w=800' },
    { id: 'LST-104', title: 'Corner Plot - G-11/3', sector: 'G-11', type: 'Plot', price: 40000000, beds: 0, baths: 0, area: '600 Sq Yd', status: 'Sold', image: 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&q=80&w=800' },
    { id: 'LST-105', title: 'Designer House - D-12', sector: 'D-12', type: 'House', price: 95000000, beds: 6, baths: 7, area: '1 Kanal', status: 'Active', image: 'https://images.unsplash.com/photo-1600607687920-4e2a09cf159d?auto=format&fit=crop&q=80&w=800' }
];
