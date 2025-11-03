import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Ramp up to 20 users over 30s
    { duration: '1m', target: 20 },   // Stay at 20 users for 1 minute
    { duration: '30s', target: 50 },  // Spike to 50 users over 30s
    { duration: '1m', target: 50 },   // Stay at 50 users for 1 minute
    { duration: '30s', target: 0 },   // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% of requests under 500ms, 99% under 1s
    http_req_failed: ['rate<0.1'],  // Less than 10% errors
    errors: ['rate<0.1'],
  },
};

const BASE_URL = 'http://localhost:8080/api';

// Test data generator
function generateBookingRequest() {
  const municipalities = ['Porto', 'Lisboa', 'Coimbra', 'Braga', 'Faro'];
  const timeSlots = ['morning', 'afternoon', 'evening'];
  const items = [
    { name: 'Sofa', description: 'Old sofa', weight: 50.0, volume: 2.5 },
    { name: 'Table', description: 'Wooden table', weight: 30.0, volume: 1.2 },
    { name: 'Chair', description: 'Office chair', weight: 15.0, volume: 0.8 },
  ];

  const futureDate = new Date();
  futureDate.setDate(futureDate.getDate() + Math.floor(Math.random() * 60) + 30); // 30-90 days ahead
  
  return {
    municipality: municipalities[Math.floor(Math.random() * municipalities.length)],
    collectionDate: futureDate.toISOString().split('T')[0],
    timeSlot: timeSlots[Math.floor(Math.random() * timeSlots.length)],
    items: [items[Math.floor(Math.random() * items.length)]],
  };
}

export default function () {
  const scenarios = [
    { weight: 40, fn: testCreateBooking },
    { weight: 30, fn: testGetBookings },
    { weight: 20, fn: testGetBookingByToken },
    { weight: 10, fn: testStaffOperations },
  ];

  // Weighted random scenario selection
  const random = Math.random() * 100;
  let cumulative = 0;
  
  for (const scenario of scenarios) {
    cumulative += scenario.weight;
    if (random < cumulative) {
      scenario.fn();
      break;
    }
  }

  sleep(Math.random() * 2 + 1); // Random sleep 1-3 seconds
}

function testCreateBooking() {
  const payload = JSON.stringify(generateBookingRequest());
  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'CreateBooking' },
  };

  const response = http.post(`${BASE_URL}/bookings`, payload, params);
  
  const success = check(response, {
    'create booking: status 201': (r) => r.status === 201,
    'create booking: has access token': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.accessToken && body.accessToken.length > 0;
      } catch {
        return false;
      }
    },
    'create booking: response time < 500ms': (r) => r.timings.duration < 500,
  });

  errorRate.add(!success);
  
  // Store token for later use
  if (response.status === 201) {
    try {
      const body = JSON.parse(response.body);
      if (body.accessToken) {
        __ENV.LAST_TOKEN = body.accessToken;
      }
    } catch (e) {
      // Ignore parse errors
    }
  }
}

function testGetBookings() {
  const params = {
    tags: { name: 'GetAllBookings' },
  };

  const response = http.get(`${BASE_URL}/staff/bookings`, params);
  
  const success = check(response, {
    'get bookings: status 200': (r) => r.status === 200,
    'get bookings: is array': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body);
      } catch {
        return false;
      }
    },
    'get bookings: response time < 300ms': (r) => r.timings.duration < 300,
  });

  errorRate.add(!success);
}

function testGetBookingByToken() {
  // Use a recently created token if available
  const token = __ENV.LAST_TOKEN || 'PORTO-2025-TESTTOKEN';
  const params = {
    tags: { name: 'GetBookingByToken' },
  };

  const response = http.get(`${BASE_URL}/bookings/token/${token}`, params);
  
  const success = check(response, {
    'get by token: status 200 or 404': (r) => r.status === 200 || r.status === 404,
    'get by token: response time < 200ms': (r) => r.timings.duration < 200,
  });

  errorRate.add(!success && response.status !== 404);
}

function testStaffOperations() {
  const token = __ENV.LAST_TOKEN;
  if (!token) {
    return; // Skip if no token available
  }

  const operations = ['assign', 'start', 'complete'];
  const operation = operations[Math.floor(Math.random() * operations.length)];
  
  const params = {
    tags: { name: `Staff${operation}` },
  };

  const response = http.put(`${BASE_URL}/staff/bookings/token/${token}/${operation}`, null, params);
  
  const success = check(response, {
    'staff operation: status 200 or 400': (r) => r.status === 200 || r.status === 400,
    'staff operation: response time < 400ms': (r) => r.timings.duration < 400,
  });

  errorRate.add(!success && response.status !== 400);
}

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'k6-test-results.json': JSON.stringify(data),
  };
}

function textSummary(data, options) {
  const indent = options.indent || '';
  let summary = '\n';
  
  summary += `${indent}✓ checks.........................: ${(data.metrics.checks.passes / data.metrics.checks.fails * 100).toFixed(2)}% pass\n`;
  summary += `${indent}✓ http_req_duration..............: avg=${data.metrics.http_req_duration.values.avg.toFixed(2)}ms p(95)=${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms\n`;
  summary += `${indent}✓ http_req_failed................: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}% fail\n`;
  summary += `${indent}✓ http_reqs......................: ${data.metrics.http_reqs.values.count} total\n`;
  summary += `${indent}✓ vus............................: ${data.metrics.vus.values.max} max\n`;
  
  return summary;
}
