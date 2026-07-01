const http = require('http');

function request(url, method, data, token = null) {
    return new Promise((resolve, reject) => {
        const u = new URL(url);
        const postData = data ? JSON.stringify(data) : '';
        const options = {
            hostname: u.hostname,
            port: u.port || 80,
            path: u.pathname + u.search,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };
        if (postData) {
            options.headers['Content-Length'] = Buffer.byteLength(postData);
        }
        if (token) {
            options.headers['Authorization'] = 'Bearer ' + token;
        }

        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', (chunk) => body += chunk);
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(body);
                    resolve({ statusCode: res.statusCode, data: parsed });
                } catch(e) {
                    resolve({ statusCode: res.statusCode, raw: body });
                }
            });
        });

        req.on('error', (e) => reject(e));
        if (postData) {
            req.write(postData);
        }
        req.end();
    });
}

async function run() {
    console.log("1. Logging in as Admin...");
    const loginRes = await request('http://localhost:3000/api/auth/admin/login', 'POST', {
        identifier: 'admin@vuavuive.vn',
        password: 'Admin@123'
    });
    if (loginRes.statusCode !== 200) {
        console.error("Login failed:", loginRes);
        return;
    }
    const token = loginRes.data.accessToken;
    console.log("Token obtained successfully.");

    const productId = "50c5a84f-c596-415b-bd71-bc0d5b9a169c"; // Cải thảo tươi Đà Lạt

    console.log("\n2. Getting current product details from API...");
    // Let's get product by ID. Wait, product details endpoint is GET /api/products/{id}
    const getRes = await request(`http://localhost:3000/api/products/${productId}`, 'GET', null, token);
    console.log("Current details:", JSON.stringify(getRes.data, null, 2));

    console.log("\n3. Testing PUT update with active = false...");
    const updateBody = {
        name: 'Cải thảo tươi Đà Lạt',
        description: 'Cải thảo tươi ngon từ Đà Lạt, giàu vitamin C và chất xơ. Thu hoạch mỗi ngày, đảm bảo độ tươi ngon tối đa.',
        originalPrice: 20000,
        sellingPrice: 15000,
        stockQuantity: 99,
        unit: 'KG',
        imageUrl: 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400',
        categoryId: '80000000-0000-0000-0000-000000000001',
        isActive: false,
        active: false
    };

    const updateRes = await request(`http://localhost:3000/api/products/${productId}`, 'PUT', updateBody, token);
    console.log("Update response (isActive: false):", JSON.stringify(updateRes.data, null, 2));

    console.log("\n4. Fetching product details again to verify if isActive is false...");
    const verifyRes = await request(`http://localhost:3000/api/products/${productId}`, 'GET', null, token);
    console.log("Verified details:", JSON.stringify(verifyRes.data, null, 2));
}

run();
