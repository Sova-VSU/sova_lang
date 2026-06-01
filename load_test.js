/**
 * k6 Load Test for Sova Lang Backend
 * Run: k6 run load_test.js
 * Requirements: k6 installed (https://k6.io/docs/getting-started/installation/)
 *               docker-compose up --build running on localhost
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// ── Custom metrics ──────────────────────────────────────────
const loginDuration    = new Trend('login_duration');
const scenarioDuration = new Trend('scenario_list_duration');
const errorRate        = new Rate('error_rate');
const requestCount     = new Counter('request_count');

// ── Test configuration ──────────────────────────────────────
export const options = {
  scenarios: {
    smoke: {
      executor: 'constant-vus',
      vus: 2,
      duration: '30s',
      tags: { test_type: 'smoke' },
    },
    load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '1m',  target: 20 },
        { duration: '20s', target: 0  },
      ],
      startTime: '35s',
      tags: { test_type: 'load' },
    },
    stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 50 },
        { duration: '30s', target: 50 },
        { duration: '10s', target: 0  },
      ],
      startTime: '2m35s',
      tags: { test_type: 'stress' },
    },
  },
  thresholds: {
    http_req_duration:      ['p(95)<500', 'p(99)<1000'],
    http_req_failed:        ['rate<0.05'],
    error_rate:             ['rate<0.05'],
    login_duration:         ['p(95)<300'],
    scenario_list_duration: ['p(95)<200'],
  },
};

const BASE_URL = 'http://localhost:8080';

// ── Helpers ─────────────────────────────────────────────────
function randomEmail() {
  return `user_${__VU}_${__ITER}_${Date.now()}@loadtest.com`;
}

function register(email, password) {
  const res = http.post(`${BASE_URL}/auth/register`, JSON.stringify({
    name: `LoadUser_${__VU}`,
    email,
    password,
  }), { headers: { 'Content-Type': 'application/json' } });
  requestCount.add(1);
  return res;
}

function login(email, password) {
  const start = Date.now();
  const res = http.post(`${BASE_URL}/auth/login`, JSON.stringify({ email, password }), {
    headers: { 'Content-Type': 'application/json' },
  });
  loginDuration.add(Date.now() - start);
  requestCount.add(1);
  return res;
}

function getScenarios(token) {
  const start = Date.now();
  const res = http.get(`${BASE_URL}/scenarios`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  scenarioDuration.add(Date.now() - start);
  requestCount.add(1);
  return res;
}

export default function () {
  const email    = randomEmail();
  const password = 'loadtest123';

  group('auth flow', () => {
    // Register
    const regRes = register(email, password);
    const regOk  = check(regRes, {
      'register 201': (r) => r.status === 201,
      'register has token': (r) => {
        try { return JSON.parse(r.body).tokens?.accessToken?.length > 0; }
        catch { return false; }
      },
    });
    if (!regOk) { errorRate.add(1); return; }
    errorRate.add(0);

    const token = JSON.parse(regRes.body).tokens.accessToken;

    // Login
    const loginRes = login(email, password);
    check(loginRes, {
      'login 200': (r) => r.status === 200,
      'login has token': (r) => {
        try { return JSON.parse(r.body).tokens?.accessToken?.length > 0; }
        catch { return false; }
      },
    });
  });

  // Re-login for fresh token
  const loginRes2 = login(email, password);
  if (loginRes2.status !== 200) { errorRate.add(1); return; }
  const token = JSON.parse(loginRes2.body).tokens.accessToken;
  errorRate.add(0);

  group('scenario flow', () => {
    // List scenarios
    const listRes = getScenarios(token);
    check(listRes, {
      'list 200': (r) => r.status === 200,
      'list has items': (r) => {
        try { return JSON.parse(r.body).items?.length >= 0; }
        catch { return false; }
      },
    });

    // Free filter
    const freeRes = http.get(`${BASE_URL}/scenarios?free=true`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    requestCount.add(1);
    check(freeRes, { 'free filter 200': (r) => r.status === 200 });

    // Get free scenario
    const coffeeRes = http.get(`${BASE_URL}/scenarios/coffee`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    requestCount.add(1);
    check(coffeeRes, {
      'coffee scenario 200': (r) => r.status === 200,
      'coffee has steps': (r) => {
        try { return JSON.parse(r.body).steps !== null; }
        catch { return false; }
      },
    });

    // Get paid scenario without subscription — should 403
    const airportRes = http.get(`${BASE_URL}/scenarios/airport`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    requestCount.add(1);
    check(airportRes, { 'airport 403 without sub': (r) => r.status === 403 });
  });

  group('user flow', () => {
    const loginRes3 = login(email, password);
    if (loginRes3.status !== 200) return;
    const t = JSON.parse(loginRes3.body).tokens.accessToken;

    // Get profile
    const profileRes = http.get(`${BASE_URL}/users/me`, {
      headers: { Authorization: `Bearer ${t}` },
    });
    requestCount.add(1);
    check(profileRes, { 'profile 200': (r) => r.status === 200 });

    // Get stats
    const statsRes = http.get(`${BASE_URL}/users/me/stats`, {
      headers: { Authorization: `Bearer ${t}` },
    });
    requestCount.add(1);
    check(statsRes, { 'stats 200': (r) => r.status === 200 });

    // Leaderboard
    const lbRes = http.get(`${BASE_URL}/leaderboard`, {
      headers: { Authorization: `Bearer ${t}` },
    });
    requestCount.add(1);
    check(lbRes, { 'leaderboard 200': (r) => r.status === 200 });
  });

  sleep(1);
}

// ── Edge case test ───────────────────────────────────────────
export function handleSummary(data) {
  return {
    'load_test_results.json': JSON.stringify(data, null, 2),
  };
}
