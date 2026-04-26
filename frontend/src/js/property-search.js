const searchResultsGrid = document.getElementById('searchResultsGrid');
const applyFiltersBtn = document.getElementById('applyFiltersBtn');

// Render Properties
function renderProperties(properties) {
    searchResultsGrid.innerHTML = '';
    
    if (properties.length === 0) {
        searchResultsGrid.innerHTML = '<div class="no-results">No properties found matching your criteria. Try adjusting your filters.</div>';
        return;
    }

    properties.forEach(prop => {
        searchResultsGrid.innerHTML += `
            <div class="property-card">
                <img src="${prop.image}" alt="${prop.title}">
                <div class="property-content">
                    <span class="badge">For Sale</span>
                    <h3 class="property-title">${prop.title}</h3>
                    <div class="property-price">${formatCurrency(prop.price)}</div>
                    <div class="property-details">
                        <span><i class="fas fa-bed"></i> ${prop.beds > 0 ? prop.beds + ' Beds' : 'N/A'}</span>
                        <span><i class="fas fa-bath"></i> ${prop.baths > 0 ? prop.baths + ' Baths' : 'N/A'}</span>
                        <span><i class="fas fa-vector-square"></i> ${prop.area}</span>
                    </div>
                    <a href="../bidding/bid-room.html" class="btn btn-outline" style="display: block; text-align: center; margin-top: 15px;">View Details & Bid</a>
                </div>
            </div>
        `;
    });
}

// Initial Render (All Properties)
renderProperties(globalProperties);

// Multi-Filter Logic (UC4)
applyFiltersBtn.addEventListener('click', () => {
    const keyword = document.getElementById('searchKeyword').value.toLowerCase();
    const sector = document.getElementById('searchSector').value;
    const type = document.getElementById('searchType').value;
    const maxPrice = parseInt(document.getElementById('searchMaxPrice').value);

    const filteredData = globalProperties.filter(prop => {
        let matchesKeyword = true;
        let matchesSector = true;
        let matchesType = true;
        let matchesPrice = true;

        if (keyword) {
            matchesKeyword = prop.title.toLowerCase().includes(keyword) || prop.sector.toLowerCase().includes(keyword);
        }
        if (sector) {
            matchesSector = prop.sector === sector;
        }
        if (type) {
            matchesType = prop.type === type;
        }
        if (!isNaN(maxPrice) && maxPrice > 0) {
            matchesPrice = prop.price <= maxPrice;
        }

        return matchesKeyword && matchesSector && matchesType && matchesPrice;
    });

    renderProperties(filteredData);
});
