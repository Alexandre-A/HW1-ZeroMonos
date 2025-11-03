// dashboard.js - load summary and render charts
(() => {
  const summaryUrl = '/api/staff/bookings/summary';
  const refreshBtn = document.getElementById('refreshBtn');
  const lastUpdated = document.getElementById('lastUpdated');

  let statusChart = null;
  let municipalityChart = null;

  function fmtDate(timestamp) {
    return new Date(timestamp).toLocaleString();
  }

  async function loadSummary() {
    try {
      const res = await fetch(summaryUrl);
      if (!res.ok) throw new Error('Failed to fetch summary: ' + res.status);
      const data = await res.json();
      render(data);
      const now = Date.now();
      lastUpdated.textContent = 'Last updated: ' + new Date(now).toLocaleString();
    } catch (err) {
      console.error('Dashboard error:', err);
      lastUpdated.textContent = 'Error loading summary';
    }
  }

  function render(summary) {
    const byStatus = summary.byStatus || {};
    const byMunicipality = summary.byMunicipality || {};
    const total = summary.total || 0;

    // Update total bookings
    const totalBookingsEl = document.getElementById('totalBookings');
    if (totalBookingsEl) {
      totalBookingsEl.textContent = total;
    }

    // Status chart
    const statusLabels = Object.keys(byStatus);
    const statusValues = statusLabels.map(k => byStatus[k]);

    const statusCtx = document.getElementById('statusChart').getContext('2d');
    if (statusChart) statusChart.destroy();
    statusChart = new Chart(statusCtx, {
      type: 'doughnut',
      data: {
        labels: statusLabels,
        datasets: [{
          data: statusValues,
          backgroundColor: [
            '#4caf50','#2196f3','#ff9800','#9c27b0','#f44336'
          ],
        }]
      },
      options: { responsive: true }
    });

    // Status breakdown table
    const statusTbody = document.querySelector('#statusBreakdown tbody');
    if (statusTbody) {
      statusTbody.innerHTML = '';
      for (const [status, count] of Object.entries(byStatus)) {
        const tr = document.createElement('tr');
        const t1 = document.createElement('td'); t1.textContent = status;
        const t2 = document.createElement('td'); t2.textContent = count;
        tr.appendChild(t1); tr.appendChild(t2);
        statusTbody.appendChild(tr);
      }
    }

    // Municipality chart - show top 10
    const muniEntries = Object.entries(byMunicipality || {}).sort((a,b) => b[1]-a[1]);
    const top = muniEntries.slice(0, 10);
    const muniLabels = top.map(e => e[0]);
    const muniValues = top.map(e => e[1]);

    const muniCtx = document.getElementById('municipalityChart').getContext('2d');
    if (municipalityChart) municipalityChart.destroy();
    municipalityChart = new Chart(muniCtx, {
      type: 'bar',
      data: {
        labels: muniLabels,
        datasets: [{
          label: 'Bookings',
          data: muniValues,
          backgroundColor: '#3f51b5'
        }]
      },
      options: { responsive: true, scales: { y: { beginAtZero: true } } }
    });

    // Top municipalities table
    const tbody = document.querySelector('#topMunicipalities tbody');
    tbody.innerHTML = '';
    for (const [mun, cnt] of top) {
      const tr = document.createElement('tr');
      const t1 = document.createElement('td'); t1.textContent = mun;
      const t2 = document.createElement('td'); t2.textContent = cnt;
      tr.appendChild(t1); tr.appendChild(t2);
      tbody.appendChild(tr);
    }
    
    // Municipality breakdown table (for test compatibility)
    const muniBreakdownTbody = document.querySelector('#municipalityBreakdown tbody');
    if (muniBreakdownTbody) {
      muniBreakdownTbody.innerHTML = '';
      for (const [mun, cnt] of muniEntries) {
        const tr = document.createElement('tr');
        const t1 = document.createElement('td'); t1.textContent = mun;
        const t2 = document.createElement('td'); t2.textContent = cnt;
        tr.appendChild(t1); tr.appendChild(t2);
        muniBreakdownTbody.appendChild(tr);
      }
    }
  }

  refreshBtn.addEventListener('click', () => loadSummary());

  // initial load
  loadSummary();
  // auto-refresh every 60s
  setInterval(loadSummary, 60_000);
})();
