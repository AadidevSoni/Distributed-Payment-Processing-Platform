import http from 'k6/http';
import { check, sleep } from 'k6';

http.setResponseCallback(http.expectedStatuses(201, 403, 409, 429));

export const options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '30s', target: 10 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = 'http://localhost:8081';

const FROM_WALLET_ID = '22eb87be-9334-4d47-ab2e-2e6838549351';
const TO_WALLET_ID = 'REPLACE_WITH_REAL_WALLET_ID';

export default function () {
    const payload = JSON.stringify({
        fromWalletId: FROM_WALLET_ID,
        toWalletId: TO_WALLET_ID,
        amount: 0.01,
        idempotencyKey: `k6-${__VU}-${__ITER}-${Date.now()}`,
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const res = http.post(`${BASE_URL}/transactions/transfer`, payload, params);

    check(res, {
        'status is 201, 403, 409, or 429': (r) =>
            [201, 403, 409, 429].includes(r.status),
    });

    sleep(0.1);
}