const http = require('http');

function request(url, method, data = null, token = null) {
    return new Promise((resolve, reject) => {
        const u = new URL(url);
        const options = {
            hostname: u.hostname,
            port: u.port || 80,
            path: u.pathname + u.search,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };
        let bodyData = null;
        if (data) {
            bodyData = JSON.stringify(data);
            options.headers['Content-Length'] = Buffer.byteLength(bodyData);
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
        if (bodyData) {
            req.write(bodyData);
        }
        req.end();
    });
}

async function run() {
    console.log("=== BẮT ĐẦU KIỂM TRA LUỒNG GÁN SHIPPER ===");

    // 1. Đăng nhập Admin
    console.log("\n1. Đăng nhập tài khoản Admin...");
    const adminLogin = await request('http://localhost:3000/api/auth/admin/login', 'POST', {
        identifier: 'admin@vuavuive.vn',
        password: 'Admin@123'
    });

    if (adminLogin.statusCode !== 200 || !adminLogin.data.accessToken) {
        console.error("Đăng nhập Admin thất bại:", adminLogin);
        return;
    }
    const adminToken = adminLogin.data.accessToken;
    console.log("Đăng nhập Admin thành công!");

    // 2. Lấy danh sách Shippers
    console.log("\n2. Lấy danh sách tài xế (Shippers)...");
    const getShippers = await request('http://localhost:3000/api/shippers', 'GET', null, adminToken);
    if (getShippers.statusCode !== 200) {
        console.error("Lấy danh sách Shippers thất bại:", getShippers);
        return;
    }
    // Response bọc trong ApiResponse wrapper: { success, message, data: [...] }
    const shippers = shippersResp.d?.data || (Array.isArray(shippersResp.d) ? shippersResp.d : []);
    console.log(`Tìm thấy ${shippers.length} tài xế.`);

    let targetShipper = null;
    if (shippers.length === 0) {
        console.log("Không có tài xế nào, tiến hành tạo mới 1 tài xế thử nghiệm...");
        const newPhone = '098' + Math.floor(1000000 + Math.random() * 9000000);
        const createShipper = await request('http://localhost:3000/api/shippers', 'POST', {
            fullName: 'Shipper Thu Nghiem',
            phone: newPhone,
            vehicleNumber: '59-X3 12345'
        }, adminToken);
        if (createShipper.statusCode !== 201) {
            console.error("Tạo tài xế thất bại:", createShipper);
            return;
        }
        targetShipper = createShipper.data;
        console.log("Tạo tài xế thành công:", targetShipper);
    } else {
        targetShipper = shippers[0];
        console.log("Sử dụng tài xế đầu tiên:", JSON.stringify(targetShipper, null, 2));
    }

    // 3. Lấy danh sách đơn hàng (admin endpoint)
    console.log("\n3. Lấy danh sách đơn hàng PENDING từ Admin API...");
    const getOrders = await request('http://localhost:3000/api/admin/orders?status=PENDING', 'GET', null, adminToken);
    if (getOrders.statusCode !== 200) {
        console.error("Lấy danh sách đơn hàng thất bại:", getOrders);
        return;
    }
    const ordersList = getOrders.data.content || [];
    console.log(`Tìm thấy ${ordersList.length} đơn hàng PENDING.`);

    let targetOrder = null;
    if (ordersList.length === 0) {
        console.log("Không có đơn hàng PENDING nào. Thử lấy tất cả đơn...");
        const getAllOrders = await request('http://localhost:3000/api/admin/orders?status=CONFIRMED', 'GET', null, adminToken);
        const allOrders = (getAllOrders.data.content || []);
        console.log(`Tổng CONFIRMED: ${allOrders.length}`);
        if (allOrders.length === 0) {
            console.log("Không có đơn hàng nào để gán. Hãy tạo đơn bằng test_crud_checkout.js trước.");
            return;
        }
        targetOrder = allOrders[0];
        console.log("Sử dụng đơn hàng CONFIRMED đầu tiên:", targetOrder.orderId);
    } else {
        targetOrder = ordersList[0];
        console.log("Sử dụng đơn hàng PENDING đầu tiên:", targetOrder.orderId);
    }

    // 4. Admin xác nhận đơn hàng (Chuyển sang CONFIRMED)
    console.log(`\n4. Admin xác nhận đơn hàng ${targetOrder.orderId} sang CONFIRMED...`);
    const confirmOrder = await request(`http://localhost:3000/api/orders/${targetOrder.orderId}/status`, 'PATCH', {
        status: 'CONFIRMED',
        note: 'Xác nhận để gán shipper'
    }, adminToken);

    if (confirmOrder.statusCode !== 200) {
        console.error("Xác nhận đơn hàng thất bại:", confirmOrder);
        return;
    }
    console.log("Xác nhận đơn hàng thành công!");

    // 5. Gán Shipper cho đơn hàng
    console.log(`\n5. Admin gán tài xế ${targetShipper.fullName} (ID: ${targetShipper.id}) cho đơn hàng ${targetOrder.orderId}...`);
    const assign = await request(`http://localhost:3000/api/shippers/${targetShipper.id}/assign/${targetOrder.orderId}`, 'POST', null, adminToken);

    console.log(`Kết quả gán (Status: ${assign.statusCode}):`);
    console.log(JSON.stringify(assign.data, null, 2));

    if (assign.statusCode === 200) {
        // 6. Kiểm tra lại đơn hàng
        console.log("\n6. Kiểm tra chi tiết đơn hàng sau khi gán...");
        const getOrderDetail = await request(`http://localhost:3000/api/orders/${targetOrder.orderId}`, 'GET', null, adminToken);
        console.log("Chi tiết đơn hàng sau gán:", JSON.stringify(getOrderDetail.data, null, 2));
        console.log("\n>>> THÀNH CÔNG: Flow gán shipper của admin hoạt động hoàn hảo 100%!");
    } else {
        console.error("\n>>> THẤT BẠI: Gán shipper thất bại!");
    }
}

run();
