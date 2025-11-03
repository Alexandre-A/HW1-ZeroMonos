// Staff Portal JavaScript
// Handles viewing, filtering, and managing bookings

const API_BASE = '/api/staff/bookings';
let allBookings = [];
let currentBooking = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    loadMunicipalities();
    loadBookings();
});

// Load municipalities for filter
async function loadMunicipalities() {
    try {
        const response = await fetch('/api/bookings/municipalities');
        if (!response.ok) throw new Error('Failed to load municipalities');
        
        const municipalities = await response.json();
        const select = document.getElementById('municipalityFilter');
        
        municipalities.forEach(municipality => {
            const option = document.createElement('option');
            option.value = municipality;
            option.textContent = municipality;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading municipalities:', error);
        showAlert('Failed to load municipalities for filter', 'error');
    }
}

// Load all bookings with optional filters
async function loadBookings() {
    const municipality = document.getElementById('municipalityFilter').value;
    const status = document.getElementById('statusFilter').value;
    
    const tbody = document.getElementById('bookingsTableBody');
    tbody.innerHTML = '<tr><td colspan="7" class="text-center"><div class="loading"></div> Loading bookings...</td></tr>';
    
    try {
        let url = API_BASE;
        
        // Apply filters
        if (municipality) {
            url = `${API_BASE}/municipality/${encodeURIComponent(municipality)}`;
        } else if (status) {
            url = `${API_BASE}/status/${status}`;
        }
        
        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to load bookings');
        
        allBookings = await response.json();
        
        displayBookings(allBookings);
        document.getElementById('bookingCount').textContent = allBookings.length;
        
    } catch (error) {
        console.error('Error loading bookings:', error);
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Error loading bookings. Please try again.</td></tr>';
        showAlert('Failed to load bookings', 'error');
    }
}

// Display bookings in table
function displayBookings(bookings) {
    const tbody = document.getElementById('bookingsTableBody');
    
    if (bookings.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center">
                    <div class="empty-state">
                        <p>No bookings found matching the filters.</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = '';
    
    bookings.forEach((booking, index) => {
        const row = document.createElement('tr');
        const statusClass = `status-${booking.currentStatus.toLowerCase()}`;
        
        row.innerHTML = `
            <td>${booking.id}</td>
            <td>${booking.municipality}</td>
            <td>${formatDate(booking.collectionDate)}</td>
            <td>${formatTimeSlot(booking.timeSlot)}</td>
            <td><span class="status-badge ${statusClass}">${booking.currentStatus}</span></td>
            <td>${formatDate(booking.collectionDate)}</td>
            <td>
                <button class="btn btn-primary" onclick="viewBookingDetails(${booking.id})" 
                        style="padding: 0.5rem 1rem; font-size: 0.875rem;">
                    View Details
                </button>
            </td>
        `;
        
        tbody.appendChild(row);
    });
}

// View booking details
async function viewBookingDetails(bookingId) {
    try {
        const response = await fetch(`${API_BASE}/${bookingId}`);
        if (!response.ok) throw new Error('Failed to load booking details');
        
        const booking = await response.json();
        currentBooking = booking;
        
        displayBookingDetails(booking);
        openDetailsModal();
        
    } catch (error) {
        console.error('Error loading booking details:', error);
        showAlert('Failed to load booking details', 'error');
    }
}

// Display booking details in modal
function displayBookingDetails(booking) {
    const content = document.getElementById('bookingDetailsContent');
    const statusClass = `status-${booking.currentStatus.toLowerCase()}`;
    
    let html = `
        <ul class="details-list">
            <li><strong>Access Token:</strong> <span style="font-family: monospace; font-size: 0.875rem;">${booking.accessToken}</span></li>
            <li><strong>Municipality:</strong> <span>${booking.municipality}</span></li>
            <li><strong>Collection Date:</strong> <span>${formatDate(booking.collectionDate)}</span></li>
            <li><strong>Time Slot:</strong> <span>${formatTimeSlot(booking.timeSlot)}</span></li>
            <li><strong>Current Status:</strong> <span class="status-badge ${statusClass}">${booking.currentStatus}</span></li>
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
    
    // Add status history
    if (booking.statusHistory && booking.statusHistory.length > 0) {
        html += '<h3 class="mt-3 mb-2">Status History</h3><div class="history-timeline">';
        
        // Sort by timestamp (newest first)
        const sortedHistory = [...booking.statusHistory].sort((a, b) => 
            new Date(b.timestamp) - new Date(a.timestamp)
        );
        
        sortedHistory.forEach(history => {
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
    
    // Update modal footer with action buttons
    updateModalFooter(booking);
}

// Update modal footer with status action buttons
function updateModalFooter(booking) {
    const footer = document.getElementById('modalFooter');
    const status = booking.currentStatus;
    
    let buttons = '<div class="btn-group">';
    
    // Add buttons based on current status
    if (status === 'RECEIVED') {
        buttons += `
            <button class="btn btn-warning" onclick="updateBookingStatus('ASSIGNED')">
                📋 Assign
            </button>
        `;
    } else if (status === 'ASSIGNED') {
        buttons += `
            <button class="btn btn-primary" onclick="updateBookingStatus('IN_PROGRESS')">
                🚚 Start Collection
            </button>
        `;
    } else if (status === 'IN_PROGRESS') {
        buttons += `
            <button class="btn btn-success" onclick="updateBookingStatus('COMPLETED')">
                ✅ Mark Complete
            </button>
        `;
    }
    
    // Add cancel button (except for completed/cancelled)
    if (status !== 'COMPLETED' && status !== 'CANCELLED') {
        buttons += `
            <button class="btn btn-danger" onclick="updateBookingStatus('CANCELLED')">
                ❌ Cancel
            </button>
        `;
    }
    
    buttons += '</div>';
    
    footer.innerHTML = `
        ${buttons}
        <button class="btn btn-secondary" onclick="closeDetailsModal()">Close</button>
    `;
}

// Update booking status
async function updateBookingStatus(newStatus) {
    if (!currentBooking) {
        showAlert('No booking selected', 'error');
        return;
    }
    
    const confirmMessages = {
        'ASSIGNED': 'Assign this booking to a collection team?',
        'IN_PROGRESS': 'Mark this booking as in progress (collection started)?',
        'COMPLETED': 'Mark this booking as completed?',
        'CANCELLED': 'Cancel this booking?'
    };
    
    if (!confirm(confirmMessages[newStatus])) {
        return;
    }
    
    try {
        let endpoint;
        let method = 'PUT';
        
        // Determine endpoint based on status using booking ID
        if (newStatus === 'ASSIGNED') {
            endpoint = `${API_BASE}/${currentBooking.id}/assign`;
        } else if (newStatus === 'IN_PROGRESS') {
            endpoint = `${API_BASE}/${currentBooking.id}/start`;
        } else if (newStatus === 'COMPLETED') {
            endpoint = `${API_BASE}/${currentBooking.id}/complete`;
        } else if (newStatus === 'CANCELLED') {
            endpoint = `${API_BASE}/${currentBooking.id}/cancel`;
        }
        
        const response = await fetch(endpoint, { method });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to update booking status');
        }
        
        showAlert(`Booking status updated to ${newStatus}`, 'success');
        
        // Reload booking details
        await viewBookingDetails(currentBooking.id);
        
        // Reload bookings list
        loadBookings();
        
    } catch (error) {
        console.error('Error updating booking status:', error);
        showAlert(error.message, 'error');
    }
}

// Modal functions
function openDetailsModal() {
    document.getElementById('detailsModal').classList.add('show');
}

function closeDetailsModal() {
    document.getElementById('detailsModal').classList.remove('show');
    currentBooking = null;
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
        month: 'short', 
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
