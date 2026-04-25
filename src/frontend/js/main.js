document.addEventListener('DOMContentLoaded', () => {
    console.log('Islamabad Real Estate Management System Frontend Initialized');
    
    // Simulate real-time bidding updates for the property details page
    const currentBidEl = document.getElementById('current-bid');
    if (currentBidEl) {
        setInterval(() => {
            // Randomly increase bid slightly for demonstration
            if (Math.random() > 0.7) {
                let currentVal = parseInt(currentBidEl.innerText.replace(/[^0-9]/g, ''));
                currentVal += (Math.floor(Math.random() * 5) + 1) * 100000; // Increase by 100k-500k
                currentBidEl.innerText = `PKR ${currentVal.toLocaleString()}`;
                currentBidEl.style.color = 'var(--primary-gold-light)';
                setTimeout(() => {
                    currentBidEl.style.color = 'var(--primary-gold)';
                }, 500);
            }
        }, 3000);
    }
});
