import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// Custom metrics
const bookingCreationTime = new Trend('booking_creation_time');
const bookingRetrievalTime = new Trend('booking_retrieval_time');
const errorRate = new Rate('errors');
const successfulBookings = new Counter('successful_bookings');

// Smoke test - minimal load to verify API works
export const options = {
  vus: 5,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.05'], // Less than 5% errors
    errors: ['rate<0.05'],
  },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
  // Test 1: Create a booking
  const booking = createBooking();
  
  if (booking && booking.accessToken) {
    sleep(1);
    
    // Test 2: Retrieve the booking by token
    retrieveBooking(booking.accessToken);
    
    sleep(1);
    
    // Test 3: Assign the booking
    assignBooking(booking.accessToken);
  }
  
  sleep(2);
}

function createBooking() {
  // Use unique dates to avoid capacity limits (10 bookings per municipality/date)
  const municipalities = ['Porto', 'Lisboa', 'Coimbra', 'Braga', 'Faro'];
  const vuId = __VU;
  const iterationId = __ITER;
  
  // Each VU uses different dates to avoid collisions
  const daysAhead = 30 + (vuId * 20) + (iterationId * 2);
  const futureDate = new Date();
  futureDate.setDate(futureDate.getDate() + daysAhead);
  
  const payload = JSON.stringify({
    municipality: municipalities[vuId % municipalities.length],
    collectionDate: futureDate.toISOString().split('T')[0],
    timeSlot: 'morning',
    items: [
      {
        name: 'Test Item',
        description: 'Smoke test item',
        weight: 25.0,
        volume: 1.5,
      },
    ],
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'SmokeCreateBooking' },
  };

  const start = Date.now();
  const response = http.post(`${BASE_URL}/bookings`, payload, params);
  bookingCreationTime.add(Date.now() - start);
  
  const success = check(response, {
    'smoke create: status 201 or 409': (r) => r.status === 201 || r.status === 409,
    'smoke create: has token if 201': (r) => {
      if (r.status === 201) {
        try {
          return JSON.parse(r.body).accessToken !== undefined;
        } catch {
          return false;
        }
      }
      return true; // Don't fail if 409 (capacity reached)
    },
  });

  // Only count as error if it's not a capacity issue
  errorRate.add(!success && response.status !== 409);
  
  if (response.status === 201) {
    successfulBookings.add(1);
    return JSON.parse(response.body);
  }
  return null;
}

function retrieveBooking(token) {
  const params = {
    tags: { name: 'SmokeRetrieveBooking' },
  };

  const start = Date.now();
  const response = http.get(`${BASE_URL}/bookings/token/${token}`, params);
  bookingRetrievalTime.add(Date.now() - start);
  
  const success = check(response, {
    'smoke retrieve: status 200': (r) => r.status === 200,
    'smoke retrieve: correct token': (r) => {
      try {
        return JSON.parse(r.body).accessToken === token;
      } catch {
        return false;
      }
    },
  });

  errorRate.add(!success);
}

function assignBooking(token) {
  const params = {
    tags: { name: 'SmokeAssignBooking' },
  };

  const response = http.put(`${BASE_URL}/staff/bookings/token/${token}/assign`, null, params);
  
  const success = check(response, {
    'smoke assign: status 200 or 400': (r) => r.status === 200 || r.status === 400,
  });

  // Don't count invalid state transitions as errors
  errorRate.add(!success && response.status !== 400);
}

export function handleSummary(data) {
  console.log('\n=== SMOKE TEST RESULTS ===\n');
  console.log(`✓ Total checks passed: ${data.metrics.checks.values.passes}`);
  console.log(`✗ Total checks failed: ${data.metrics.checks.values.fails}`);
  console.log(`✓ Successful bookings: ${data.metrics.successful_bookings ? data.metrics.successful_bookings.values.count : 0}`);
  console.log(`⏱  Average booking creation: ${data.metrics.booking_creation_time ? data.metrics.booking_creation_time.values.avg.toFixed(2) : 'N/A'}ms`);
  console.log(`⏱  Average booking retrieval: ${data.metrics.booking_retrieval_time ? data.metrics.booking_retrieval_time.values.avg.toFixed(2) : 'N/A'}ms`);
  
  return {
    'k6-smoke-test-results.json': JSON.stringify(data),
  };
}
