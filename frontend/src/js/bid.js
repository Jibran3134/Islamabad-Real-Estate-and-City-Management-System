let currentHighestBid = 85000000;
let timeRemaining = 15; // Mocking a very short timer for demonstration (15 seconds)
let biddingActive = true;

const highestBidDisplay = document.getElementById('highestBidDisplay');
const bidAmountInput = document.getElementById('bidAmountInput');
const placeBidBtn = document.getElementById('placeBidBtn');
const bidHistoryList = document.getElementById('bidHistoryList');
const countdownTimer = document.getElementById('countdownTimer');
const activeBiddingArea = document.getElementById('activeBiddingArea');
const biddingClosedArea = document.getElementById('biddingClosedArea');
const winningBidAmount = document.getElementById('winningBidAmount');

// Timer Logic (Simulating UC8: Close Bidding Automatically)
const timerInterval = setInterval(() => {
    if (timeRemaining <= 0) {
        clearInterval(timerInterval);
        closeBidding();
    } else {
        timeRemaining--;
        updateTimerDisplay();
    }
}, 1000);

function updateTimerDisplay() {
    const minutes = Math.floor(timeRemaining / 60).toString().padStart(2, '0');
    const seconds = (timeRemaining % 60).toString().padStart(2, '0');
    countdownTimer.innerText = `00:${minutes}:${seconds}`;
    if (timeRemaining <= 10) {
        countdownTimer.style.color = 'darkred'; // Urgent
    }
}

// UC8 & UC9 Logic
function closeBidding() {
    biddingActive = false;
    countdownTimer.innerText = '00:00:00';
    activeBiddingArea.style.display = 'none';
    biddingClosedArea.style.display = 'block';
    
    // Simulate UC9 (Declare Winning Bidder)
    winningBidAmount.innerText = currentHighestBid.toLocaleString();
}

// Place Bid Logic (UC6)
placeBidBtn.addEventListener('click', () => {
    if (!biddingActive) {
        alert('Bidding is closed.');
        return;
    }

    const newBid = parseInt(bidAmountInput.value);
    if (isNaN(newBid)) {
        alert('Please enter a valid number.');
        return;
    }

    if (newBid <= currentHighestBid) {
        alert('Your bid must be higher than the current highest bid (' + formatCurrency(currentHighestBid) + ').');
        return;
    }

    // Accept Bid
    currentHighestBid = newBid;
    highestBidDisplay.innerText = formatCurrency(currentHighestBid);
    bidAmountInput.value = '';

    // Update History
    const newHistoryItem = document.createElement('div');
    newHistoryItem.className = 'bid-history-item';
    newHistoryItem.innerHTML = `
        <span>You (User #B-Current)</span>
        <strong>${formatCurrency(currentHighestBid)}</strong>
    `;
    bidHistoryList.insertBefore(newHistoryItem, bidHistoryList.firstChild);

    alert('Bid placed successfully!');
});
