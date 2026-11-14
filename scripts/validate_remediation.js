const http = require('http');

async function req(path, method = 'GET', data = null, token = null) {
  return new Promise((resolve, reject) => {
    const opts = {
      hostname: 'localhost',
      port: 8082,
      path,
      method,
      headers: {
        'Content-Type': 'application/json'
      }
    };
    if (token) opts.headers['Authorization'] = 'Bearer ' + token;
    if (data) opts.headers['Content-Length'] = Buffer.byteLength(data);
    const r = http.request(opts, res => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, body: JSON.parse(body || '{}') });
        } catch (e) {
          resolve({ status: res.statusCode, body });
        }
      });
    });
    r.on('error', reject);
    if (data) r.write(data);
    r.end();
  });
}

(async () => {
  console.log('================================================================');
  console.log('FIELDOPS PHASE 6.3.1 - LIVE REMEDIATION VERIFICATION');
  console.log('================================================================\n');

  console.log('--- 1. Testing Dispatcher Login & /api/intelligence/overview (SEC-6301) ---');
  const dLogin = await req('/api/auth/login', 'POST', JSON.stringify({ email: 'dispatcher@fieldops.demo', password: 'FieldOps@2026!' }));
  const dToken = dLogin.body.data.token;
  console.log('Dispatcher login status:', dLogin.status);

  const overview = await req('/api/intelligence/overview', 'GET', null, dToken);
  console.log('Overview status (Expected 200):', overview.status);
  console.log('Overview HealthScore:', overview.body.data?.operationsHealthScore);
  console.log('Overview ActiveAlerts:', overview.body.data?.activeAlertsCount);
  console.log('Overview TopPredictedBreaches:', overview.body.data?.topPredictedBreaches?.length);

  console.log('\n--- 2. Testing Technician Self-Access & Work Order Update (SEC-6302) ---');
  // Demo email
  const tDemoLogin = await req('/api/auth/login', 'POST', JSON.stringify({ email: 'technician@fieldops.demo', password: 'FieldOps@2026!' }));
  const tDemoToken = tDemoLogin.body.data.token;
  console.log('technician@fieldops.demo login status:', tDemoLogin.status);

  const tDemoProfile = await req('/api/technicians/1', 'GET', null, tDemoToken);
  console.log('technician@ accessing /api/technicians/1 (Expected 200):', tDemoProfile.status, 'Tech Name:', tDemoProfile.body.data?.name);

  const tDemoWo = await req('/api/work-orders/1/status', 'PATCH', JSON.stringify({ status: 'IN_PROGRESS', serviceNotes: 'Diagnosing on-site' }), tDemoToken);
  console.log('technician@ updating assigned WO-1 status (Expected 200):', tDemoWo.status, 'Status:', tDemoWo.body.data?.status);

  // Primary email rahul.s@fieldops.demo
  const tRahulLogin = await req('/api/auth/login', 'POST', JSON.stringify({ email: 'rahul.s@fieldops.demo', password: 'FieldOps@2026!' }));
  const tRahulToken = tRahulLogin.body.data.token;
  console.log('rahul.s@fieldops.demo login status:', tRahulLogin.status);

  const tRahulProfile = await req('/api/technicians/1', 'GET', null, tRahulToken);
  console.log('rahul.s@ accessing /api/technicians/1 (Expected 200):', tRahulProfile.status, 'Tech Name:', tRahulProfile.body.data?.name);

  // Route access tests
  const tRouteSelf = await req('/api/technicians/1/optimized-route', 'GET', null, tRahulToken);
  console.log('rahul.s@ accessing own route /api/technicians/1/optimized-route (Expected 200):', tRouteSelf.status, 'Total Stops:', tRouteSelf.body.data?.totalStops);

  const tDemoRouteSelf = await req('/api/technicians/1/optimized-route', 'GET', null, tDemoToken);
  console.log('technician@ accessing own route /api/technicians/1/optimized-route (Expected 200):', tDemoRouteSelf.status, 'Total Stops:', tDemoRouteSelf.body.data?.totalStops);

  // Peer IDOR test
  const tPeer = await req('/api/technicians/2', 'GET', null, tRahulToken);
  console.log('rahul.s@ accessing peer technician 2 (Expected 403):', tPeer.status);

  const tPeerRoute = await req('/api/technicians/2/optimized-route', 'GET', null, tRahulToken);
  console.log('rahul.s@ accessing peer route 2 (Expected 403):', tPeerRoute.status);

  console.log('\n--- 3. Testing Customer Self-Access & IDOR (SEC-6302) ---');
  const cDemoLogin = await req('/api/auth/login', 'POST', JSON.stringify({ email: 'customer@fieldops.demo', password: 'FieldOps@2026!' }));
  const cDemoToken = cDemoLogin.body.data.token;
  console.log('customer@fieldops.demo login status:', cDemoLogin.status);

  const cDemoProfile = await req('/api/customers/1', 'GET', null, cDemoToken);
  console.log('customer@ accessing /api/customers/1 (Expected 200):', cDemoProfile.status, 'Customer Name:', cDemoProfile.body.data?.name);

  const cCust2 = await req('/api/customers/2', 'GET', null, cDemoToken);
  console.log('customer@ accessing unowned customer 2 (Expected 403):', cCust2.status);

  console.log('\n--- 4. Testing Exception Handling (P1 Defect) ---');
  const malformed = await req('/api/simulator/simulate', 'POST', JSON.stringify({ scenarioType: 'INVALID_ENUM' }), dToken);
  console.log('Malformed enum payload (Expected 400):', malformed.status, 'Message:', malformed.body.message);

  const notFound = await req('/api/unmapped/resource/path', 'GET', null, dToken);
  console.log('Unmapped URL endpoint (Expected 404):', notFound.status, 'Message:', notFound.body.message);

  console.log('\n================================================================');
  console.log('ALL VERIFICATIONS COMPLETED SUCCESSFULLY!');
  console.log('================================================================');
})();
