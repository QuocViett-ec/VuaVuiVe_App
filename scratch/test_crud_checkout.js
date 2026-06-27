const http = require('http');

function post(url, data, token = null) {
    return new Promise((resolve, reject) => {
        const u = new URL(url);
        const postData = JSON.stringify(data);
        const options = {
            hostname: u.hostname,
            port: u.port || 80,
            path: u.pathname + u.search,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(postData)
            }
        };
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
        req.write(postData);
        req.end();
    });
}

async function run() {
    console.log("=== BẮT ĐẦU KIỂM TRA FLOW CRUD & CHECKOUT ===");

    // 1. Admin Đăng nhập
    console.log("\n1. Đăng nhập tài khoản Admin...");
    let adminLogin;
    try {
        adminLogin = await post('http://localhost:3000/api/auth/admin/login', {
            identifier: 'admin@vuavuive.vn',
            password: 'Admin@123'
        });
    } catch(e) {
        console.error("Lỗi kết nối backend:", e.message);
        return;
    }

    if (adminLogin.statusCode !== 200 || !adminLogin.data.accessToken) {
        console.error("Đăng nhập Admin thất bại:", adminLogin);
        return;
    }
    const adminToken = adminLogin.data.accessToken;
    console.log("Đăng nhập Admin thành công!");

    // 2. Admin tạo sản phẩm mới
    console.log("\n2. Admin tạo sản phẩm mới...");
    const productRequest = {
        name: 'Quoc viet',
        description: 'Sản phẩm thử nghiệm chất lượng cao',
        originalPrice: 15000,
        sellingPrice: 15000,
        stockQuantity: 50,
        unit: 'KG',
        imageUrl: 'https://res.cloudinary.com/ddj1f931a/image/upload/v1781347041/vua-vui-ve/products/100-rau-muong-500g.jpg',
        categoryId: '80000000-0000-0000-0000-000000000001'
    };

    const createProduct = await post('http://localhost:3000/api/products', productRequest, adminToken);
    if (createProduct.statusCode !== 201 || !createProduct.data.data || !createProduct.data.data.id) {
        console.error("Tạo sản phẩm thất bại:", createProduct);
        return;
    }
    const productId = createProduct.data.data.id;
    console.log(`Tạo sản phẩm thành công! ID: ${productId}`);
    console.log("Chi tiết sản phẩm trả về:", JSON.stringify(createProduct.data, null, 2));

    // 3. Customer Đăng nhập
    console.log("\n3. Đăng nhập tài khoản Customer...");
    const customerLogin = await post('http://localhost:3000/api/auth/login', {
        identifier: 'customer@gmail.com',
        password: 'Customer@123'
    });

    if (customerLogin.statusCode !== 200 || !customerLogin.data.accessToken) {
        console.error("Đăng nhập Customer thất bại:", customerLogin);
        return;
    }
    const customerToken = customerLogin.data.accessToken;
    console.log("Đăng nhập Customer thành công!");

    // 4. Customer đặt hàng sản phẩm vừa tạo
    console.log("\n4. Customer đặt hàng (Checkout)...");
    const orderRequest = {
        items: [
            {
                productId: productId,
                quantity: 2,
                price: 15000
            }
        ],
        delivery: {
            name: 'quoc viet',
            phone: '0977654843',
            address: 'ben cat'
        },
        payment: {
            method: 'COD'
        },
        note: 'nhanh',
        shippingFee: 15000,
        discount: 0
    };

    const checkout = await post('http://localhost:3000/api/orders', orderRequest, customerToken);
    console.log(`Kết quả checkout (Status: ${checkout.statusCode}):`);
    console.log(JSON.stringify(checkout.data, null, 2));

    if (checkout.statusCode === 201 || (checkout.data && checkout.data.success)) {
        console.log("\n>>> THÀNH CÔNG: Flow CRUD Admin và Customer mua hàng hoạt động hoàn hảo 100%!");
    } else {
        console.error("\n>>> THẤT BẠI: Checkout thất bại!");
    }
}

run();
