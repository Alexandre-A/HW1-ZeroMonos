// Citizen Portal JavaScript
// Handles booking creation, status checking, and cancellation

const API_BASE = '/api/bookings';
let currentBookingToken = null;
let itemCounter = 0;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    loadMunicipalities();
    setMinDate();
    addBulkItem(); // Add first item by default
    
    // Setup form handlers
    document.getElementById('bookingForm').addEventListener('submit', handleBookingSubmit);
    document.getElementById('checkStatusForm').addEventListener('submit', handleCheckStatus);
});

// Load municipalities from API
async function loadMunicipalities() {
    try {
        const response = await fetch(`${API_BASE}/municipalities`);
        if (!response.ok) {
            throw new Error('Failed to load municipalities');
        }
        
        const municipalities = await response.json();
        const select = document.getElementById('municipality');
        
        // Clear loading option
        select.innerHTML = '<option value="">Select your municipality</option>';
        
        // Add municipalities
        municipalities.forEach(municipality => {
            const option = document.createElement('option');
            option.value = municipality;
            option.textContent = municipality;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading municipalities:', error);
        showAlert('Failed to load municipalities. Please refresh the page.', 'error');
        document.getElementById('municipality').innerHTML = 
            '<option value="">Error loading municipalities - please refresh</option>';
    }
}

// Set minimum date (1 day from today)
function setMinDate() {
    const today = new Date();
    today.setDate(today.getDate() + 1);
    const minDate = today.toISOString().split('T')[0];
    document.getElementById('collectionDate').min = minDate;
}

// Add bulk item input fields
function addBulkItem() {
    itemCounter++;
    const container = document.getElementById('bulkItemsContainer');
    
    const itemDiv = document.createElement('div');
    itemDiv.className = 'bulk-item';
    itemDiv.id = `item-${itemCounter}`;
    
    itemDiv.innerHTML = `
        <div class="form-group">
            <label for="itemName-${itemCounter}">Item Name *</label>
            <input type="text" id="itemName-${itemCounter}" 
                   placeholder="e.g., Sofa" required>
        </div>
        <div class="form-group">
            <label for="itemDesc-${itemCounter}">Description</label>
            <input type="text" id="itemDesc-${itemCounter}" 
                   placeholder="e.g., Large 3-seater">
        </div>
        <div class="form-group">
            <label for="itemWeight-${itemCounter}">Weight (kg) *</label>
            <input type="number" id="itemWeight-${itemCounter}" 
                   step="0.1" min="0.1" placeholder="50" required>
        </div>
        <div class="form-group">
            <label for="itemVolume-${itemCounter}">Volume (m³) *</label>
            <input type="number" id="itemVolume-${itemCounter}" 
                   step="0.1" min="0.1" placeholder="2.5" required>
        </div>
        <button type="button" class="btn-remove" onclick="removeBulkItem(${itemCounter})">
            ✕ Remove
        </button>
    `;
    
    container.appendChild(itemDiv);
}

// Remove bulk item
function removeBulkItem(id) {
    const items = document.querySelectorAll('.bulk-item');
    if (items.length > 1) {
        document.getElementById(`item-${id}`).remove();
    } else {
        showAlert('You must have at least one item', 'error');
    }
}

// Collect bulk items from form
function collectBulkItems() {
    const items = [];
    const itemDivs = document.querySelectorAll('.bulk-item');
    
    itemDivs.forEach((div, index) => {
        const id = div.id.split('-')[1];
        const name = document.getElementById(`itemName-${id}`).value;
        const description = document.getElementById(`itemDesc-${id}`).value;
        const weight = parseFloat(document.getElementById(`itemWeight-${id}`).value);
        const volume = parseFloat(document.getElementById(`itemVolume-${id}`).value);
        
        items.push({
            name: name,
            description: description || '',
            weight: weight,
            volume: volume
        });
    });
    
    return items;
}

// Handle booking form submission
async function handleBookingSubmit(event) {
    event.preventDefault();
    
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="loading"></span> Creating booking...';
    
    try {
        const bookingData = {
            municipality: document.getElementById('municipality').value,
            collectionDate: document.getElementById('collectionDate').value,
            timeSlot: document.getElementById('timeSlot').value,
            items: collectBulkItems()
        };
        
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(bookingData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to create booking');
        }
        
        const result = await response.json();
        
        // Show success message in bookingResponse div
        const responseDiv = document.getElementById('bookingResponse');
        responseDiv.textContent = 'Booking created successfully! Your access token is displayed below.';
        responseDiv.className = 'alert alert-success';
        responseDiv.classList.remove('hidden');
        
        // Show success and token (only set on the span, not the parent div)
        document.getElementById('accessTokenDisplay').textContent = result.accessToken;
        document.getElementById('createBookingCard').classList.add('hidden');
        document.getElementById('tokenCard').classList.remove('hidden');
        
        currentBookingToken = result.accessToken;
        
        showAlert('Booking created successfully! Save your access token.', 'success');
        
        // Scroll to token
        document.getElementById('tokenCard').scrollIntoView({ behavior: 'smooth' });
        
    } catch (error) {
        console.error('Error creating booking:', error);
        const responseDiv = document.getElementById('bookingResponse');
        responseDiv.textContent = error.message || 'Failed to create booking. Please try again.';
        responseDiv.className = 'alert alert-danger';
        responseDiv.classList.remove('hidden');
        showAlert(error.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Schedule Pickup';
    }
}

// Handle check status form
async function handleCheckStatus(event) {
    event.preventDefault();
    
    const token = document.getElementById('checkToken').value.trim();
    if (!token) {
        showAlert('Please enter a valid token', 'error');
        return;
    }
    
    await loadBookingDetails(token);
}

// Load booking details by token
async function loadBookingDetails(token) {
    try {
        const response = await fetch(`${API_BASE}/${token}/details`);
        
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Booking not found. Please check your token.');
            }
            throw new Error('Failed to load booking details');
        }
        
        const booking = await response.json();
        currentBookingToken = token;
        
        displayBookingDetails(booking);
        openDetailsModal();
        
    } catch (error) {
        console.error('Error loading booking:', error);
        showAlert(error.message, 'error');
    }
}

// Display booking details in modal
function displayBookingDetails(booking) {
    const content = document.getElementById('bookingDetailsContent');
    
    const statusClass = `status-${booking.currentStatus.toLowerCase()}`;
    
    let html = `
        <ul class="details-list">
            <li><strong>Access Token:</strong> <span style="font-family: monospace;">${booking.accessToken}</span></li>
            <li><strong>Municipality:</strong> <span>${booking.municipality}</span></li>
            <li><strong>Collection Date:</strong> <span>${formatDate(booking.collectionDate)}</span></li>
            <li><strong>Time Slot:</strong> <span>${formatTimeSlot(booking.timeSlot)}</span></li>
            <li><strong>Status:</strong> <span class="status-badge ${statusClass}">${booking.currentStatus}</span></li>
        </ul>
        
        <h3 class="mt-3 mb-2">Items to Collect</h3>
        <ul class="items-list">
    `;
    
    booking.items.forEach(item => {
        html += `
            <li>
                <strong>${item.name}</strong>
                <small>${item.description || 'No description'}</small><br>
                <small>Weight: ${item.weight} kg | Volume: ${item.volume} m³</small>
            </li>
        `;
    });
    
    html += '</ul>';
    
    // Add status history if available
    if (booking.statusHistory && booking.statusHistory.length > 0) {
        html += '<h3 class="mt-3 mb-2">Status History</h3><div class="history-timeline">';
        
        booking.statusHistory.forEach(history => {
            const historyStatusClass = `status-${history.status.toLowerCase()}`;
            html += `
                <div class="history-item">
                    <span class="status status-badge ${historyStatusClass}">${history.status}</span>
                    <span class="datetime">${formatDateTime(history.timestamp)}</span>
                </div>
            `;
        });
        
        html += '</div>';
    }
    
    content.innerHTML = html;
    
    // Show/hide cancel button based on status
    const cancelBtn = document.getElementById('cancelBookingBtn');
    if (booking.currentStatus === 'COMPLETED' || booking.currentStatus === 'CANCELLED') {
        cancelBtn.classList.add('hidden');
    } else {
        cancelBtn.classList.remove('hidden');
    }
}

// Cancel booking
async function cancelBooking() {
    if (!currentBookingToken) {
        showAlert('No booking selected', 'error');
        return;
    }
    
    if (!confirm('Are you sure you want to cancel this booking?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/${currentBookingToken}/cancel`, {
            method: 'PUT'
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to cancel booking');
        }
        
        showAlert('Booking cancelled successfully', 'success');
        closeDetailsModal();
        
        // If check token field has value, reload details
        const checkToken = document.getElementById('checkToken').value;
        if (checkToken) {
            setTimeout(() => loadBookingDetails(checkToken), 1000);
        }
        
    } catch (error) {
        console.error('Error cancelling booking:', error);
        showAlert(error.message, 'error');
    }
}

// Copy token to clipboard
function copyToken() {
    const token = document.getElementById('accessTokenDisplay').textContent;
    navigator.clipboard.writeText(token).then(() => {
        showAlert('Token copied to clipboard!', 'success');
    }).catch(() => {
        showAlert('Failed to copy token. Please copy manually.', 'error');
    });
}

// Reset form for new booking
function resetForm() {
    document.getElementById('bookingForm').reset();
    document.getElementById('createBookingCard').classList.remove('hidden');
    document.getElementById('tokenCard').classList.add('hidden');
    
    // Reset items
    document.getElementById('bulkItemsContainer').innerHTML = '';
    itemCounter = 0;
    addBulkItem();
    
    currentBookingToken = null;
    
    // Scroll to form
    document.getElementById('createBookingCard').scrollIntoView({ behavior: 'smooth' });
}

// Modal functions
function openDetailsModal() {
    document.getElementById('detailsModal').classList.add('show');
}

function closeDetailsModal() {
    document.getElementById('detailsModal').classList.remove('show');
}

// Alert utility
function showAlert(message, type = 'info') {
    const container = document.getElementById('alertContainer');
    
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} show`;
    alert.textContent = message;
    
    container.appendChild(alert);
    
    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 300);
    }, 5000);
}

// Date formatting utilities
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
}

function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatTimeSlot(slot) {
    const slots = {
        'morning': 'Morning (8:00 AM - 12:00 PM)',
        'afternoon': 'Afternoon (12:00 PM - 5:00 PM)',
        'evening': 'Evening (5:00 PM - 8:00 PM)'
    };
    return slots[slot] || slot;
}

// Close modal on outside click
window.onclick = function(event) {
    const modal = document.getElementById('detailsModal');
    if (event.target === modal) {
        closeDetailsModal();
    }
};
