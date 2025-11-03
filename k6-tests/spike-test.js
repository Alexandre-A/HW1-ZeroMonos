import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Spike test configuration - quick burst of traffic
export const options = {
  stages: [
    { duration: '10s', target: 10 },   // Warm up to 10 users
    { duration: '10s', target: 100 },  // Spike to 100 users quickly
    { duration: '30s', target: 100 },  // Stay at 100 users for 30s
    { duration: '10s', target: 10 },   // Scale down
    { duration: '10s', target: 0 },    // Ramp down completely
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000', 'p(99)<2000'], // More lenient during spike
    http_req_failed: ['rate<0.2'],  // Allow up to 20% errors during spike
    errors: ['rate<0.2'],
  },
};

const BASE_URL = 'http://localhost:8080/api';

function generateBookingRequest() {
  const municipalities = ['Porto', 'Lisboa', 'Coimbra', 'Braga', 'Faro', 'Aveiro', 'Setúbal', 'Évora'];
  const timeSlots = ['morning', 'afternoon', 'evening'];
  
  const futureDate = new Date();
  futureDate.setDate(futureDate.getDate() + Math.floor(Math.random() * 90) + 30); // 30-120 days
  
  return {
    municipality: municipalities[Math.floor(Math.random() * municipalities.length)],
    collectionDate: futureDate.toISOString().split('T')[0],
    timeSlot: timeSlots[Math.floor(Math.random() * timeSlots.length)],
    items: [
      {
        name: `Item-${Math.random().toString(36).substring(7)}`,
        description: 'Bulk waste item',
        weight: Math.random() * 50 + 10,
        volume: Math.random() * 3 + 0.5,
      },
    ],
  };
}

export default function () {
  // Simulate real user behavior with multiple operations
  const scenario = Math.random();
  
  if (scenario < 0.5) {
    // 50% - Create booking
    createBooking();
  } else if (scenario < 0.8) {
    // 30% - List bookings
    listBookings();
  } else {
    // 20% - Check dashboard
    checkDashboard();
  }

  sleep(0.5); // Shorter sleep during spike test
}

function createBooking() {
  const payload = JSON.stringify(generateBookingRequest());
  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'SpikeCreateBooking' },
  };

  const response = http.post(`${BASE_URL}/bookings`, payload, params);
  
  const success = check(response, {
    'spike create: status 201 or 409': (r) => r.status === 201 || r.status === 409,
    'spike create: response time < 1s': (r) => r.timings.duration < 1000,
  });

  errorRate.add(!success && response.status !== 409);
}

function listBookings() {
  const params = {
    tags: { name: 'SpikeListBookings' },
  };

  const response = http.get(`${BASE_URL}/staff/bookings`, params);
  
  const success = check(response, {
    'spike list: status 200': (r) => r.status === 200,
    'spike list: response time < 800ms': (r) => r.timings.duration < 800,
  });

  errorRate.add(!success);
}

function checkDashboard() {
  const params = {
    tags: { name: 'SpikeDashboard' },
  };

  const response = http.get(`${BASE_URL}/staff/dashboard/summary`, params);
  
  const success = check(response, {
    'spike dashboard: status 200': (r) => r.status === 200,
    'spike dashboard: response time < 600ms': (r) => r.timings.duration < 600,
  });

  errorRate.add(!success);
}

export function handleSummary(data) {
  console.log('\n=== SPIKE TEST RESULTS ===\n');
  console.log(`Total requests: ${data.metrics.http_reqs.values.count}`);
  console.log(`Failed requests: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);
  console.log(`Average response time: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`95th percentile: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  
  // Safely handle p(99) which might be undefined under extreme load
  const p99 = data.metrics.http_req_duration.values['p(99)'];
  console.log(`99th percentile: ${p99 ? p99.toFixed(2) + 'ms' : 'N/A'}`);
  console.log(`Max VUs: ${data.metrics.vus.values.max}`);
  
  return {
    'k6-spike-test-results.json': JSON.stringify(data),
  };
}
