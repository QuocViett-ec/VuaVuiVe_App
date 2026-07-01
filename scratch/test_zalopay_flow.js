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

function get(url, token = null) {
    return new Promise((resolve, reject) => {
        const u = new URL(url);
        const options = {
            hostname: u.hostname,
            port: u.port || 80,
            path: u.pathname + u.search,
            method: 'GET',
            headers: {}
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
        req.end();
    });
}

async function run() {
    console.log("=== BẮT ĐẦU KIỂM TRA ZALOPAY PAYMENT FLOW ===");

    // 1. Admin Đăng nhập để tạo sản phẩm test
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
        name: 'Sản phẩm Test ZaloPay',
        description: 'Sản phẩm thử nghiệm thanh toán ZaloPay',
        originalPrice: 20000,
        sellingPrice: 20000,
        stockQuantity: 10,
        unit: 'Cái',
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

    // 4. Customer đặt hàng sản phẩm vừa tạo với phương thức ZALOPAY
    console.log("\n4. Customer đặt hàng (Checkout với ZALOPAY)...");
    const orderRequest = {
        items: [
            {
                productId: productId,
                quantity: 1,
                price: 20000
            }
        ],
        delivery: {
            name: 'Khách hàng ZaloPay',
            phone: '0912345678',
            address: 'Địa chỉ nhận hàng Test'
        },
        payment: {
            method: 'ZALOPAY'
        },
        note: 'Giao hàng nhanh',
        shippingFee: 15000,
        discount: 0
    };

    const checkout = await post('http://localhost:3000/api/orders', orderRequest, customerToken);
    if (checkout.statusCode !== 201 || !checkout.data.success || !checkout.data.data) {
        console.error("Đặt hàng thất bại:", checkout);
        return;
    }
    const orderId = checkout.data.data.id || checkout.data.data.orderId;
    const finalAmount = checkout.data.data.finalAmount;
    console.log(`Đặt hàng thành công! Order ID: ${orderId}, Số tiền: ${finalAmount}`);

    // 5. Tạo yêu cầu thanh toán ZaloPay
    console.log("\n5. Tạo yêu cầu thanh toán ZaloPay...");
    const paymentReq = {
        orderId: orderId,
        amount: finalAmount,
        description: "Thanh toan don hang Vua Vui Ve: " + orderId
    };

    const paymentResponse = await post('http://localhost:3000/api/payments/zalopay', paymentReq, customerToken);
    if (paymentResponse.statusCode !== 200 || !paymentResponse.data.success || !paymentResponse.data.data) {
        console.error("Tạo yêu cầu thanh toán ZaloPay thất bại:", paymentResponse);
        return;
    }
    const paymentData = paymentResponse.data.data;
    console.log("Tạo yêu cầu thanh toán ZaloPay thành công!");
    console.log("Order URL:", paymentData.orderUrl);
    console.log("App Trans ID:", paymentData.appTransId);

    // 6. Kiểm tra trạng thái thanh toán ban đầu (phải là PENDING)
    console.log("\n6. Kiểm tra trạng thái thanh toán ban đầu...");
    let statusResponse = await get(`http://localhost:3000/api/payments/${orderId}/status`, customerToken);
    console.log("Trạng thái ban đầu:", JSON.stringify(statusResponse.data.data, null, 2));
    if (statusResponse.data.data.paymentStatus !== "PENDING") {
        console.error("Trạng thái thanh toán không đúng (kỳ vọng PENDING)");
        return;
    }

    // 7. Mock thanh toán thành công
    console.log("\n7. Mock thanh toán thành công...");
    const mockSuccessResponse = await post(`http://localhost:3000/api/payments/zalopay/mock-success/${orderId}`, {}, customerToken);
    if (mockSuccessResponse.statusCode !== 200 || !mockSuccessResponse.data.success) {
        console.error("Mock thanh toán thành công thất bại:", mockSuccessResponse);
        return;
    }
    console.log("Mock thanh toán thành công thành công!");

    // 8. Kiểm tra lại trạng thái thanh toán sau khi mock (phải là PAID và PENDING_APPROVAL)
    console.log("\n8. Kiểm tra trạng thái thanh toán sau khi mock...");
    statusResponse = await get(`http://localhost:3000/api/payments/${orderId}/status`, customerToken);
    console.log("Trạng thái sau khi mock:", JSON.stringify(statusResponse.data.data, null, 2));
    if (statusResponse.data.data.paymentStatus === "PAID" && statusResponse.data.data.orderStatus === "PENDING_APPROVAL") {
        console.log("\n>>> THÀNH CÔNG: Flow thanh toán ZaloPay hoạt động hoàn hảo 100%!");
    } else {
        console.error("\n>>> THẤT BẠI: Trạng thái đơn hàng hoặc thanh toán không cập nhật đúng!");
    }
}

run();
