const https = require('https');

const API_KEY = 'AIzaSyDTkZoVvvS45QhsQgH6EphLAF6CGq0-794';
const DB_URL = 'https://vua-vui-ve-default-rtdb.firebaseio.com';

// Helper function to make HTTP POST request
function postJson(url, body) {
    return new Promise((resolve, reject) => {
        const parsedUrl = new URL(url);
        const postData = JSON.stringify(body);
        const options = {
            hostname: parsedUrl.hostname,
            path: parsedUrl.pathname + parsedUrl.search,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(postData)
            }
        };

        const req = https.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                try {
                    resolve(JSON.parse(data));
                } catch(e) {
                    reject(new Error("Failed to parse JSON response: " + e.message));
                }
            });
        });

        req.on('error', (e) => reject(e));
        req.write(postData);
        req.end();
    });
}

// Helper function to make HTTP PUT request to Firebase RTDB
function putJson(url, body) {
    return new Promise((resolve, reject) => {
        const parsedUrl = new URL(url);
        const putData = JSON.stringify(body);
        const options = {
            hostname: parsedUrl.hostname,
            path: parsedUrl.pathname + parsedUrl.search,
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(putData)
            }
        };

        const req = https.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                try {
                    resolve(JSON.parse(data));
                } catch(e) {
                    reject(new Error("Failed to parse JSON response: " + e.message));
                }
            });
        });

        req.on('error', (e) => reject(e));
        req.write(putData);
        req.end();
    });
}

async function getOrCreateUser(email, password) {
    console.log(`[+] Đang tạo/lấy tài khoản Auth cho email: ${email}...`);
    try {
        // Thử đăng ký trước
        const urlSignUp = `https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=${API_KEY}`;
        const res = await postJson(urlSignUp, {
            email: email,
            password: password,
            returnSecureToken: true
        });
        if (res.localId) {
            console.log(`[✓] Đã tạo mới tài khoản thành công! UID: ${res.localId}`);
            return res.localId;
        }
        if (res.error && res.error.message === 'EMAIL_EXISTS') {
            // Đã tồn tại, thử đăng nhập để lấy UID
            const urlSignIn = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`;
            const resSign = await postJson(urlSignIn, {
                email: email,
                password: password,
                returnSecureToken: true
            });
            if (resSign.localId) {
                console.log(`[✓] Tài khoản đã tồn tại. Lấy thành công UID: ${resSign.localId}`);
                return resSign.localId;
            }
        }
        throw new Error(JSON.stringify(res.error || res));
    } catch(e) {
        console.error(`[x] Lỗi xử lý tài khoản ${email}:`, e.message);
        throw e;
    }
}

async function runSeed() {
    try {
        console.log("=== BẮT ĐẦU SEED TÀI KHOẢN SHIPPER & ĐƠN HÀNG ===");
        
        // 1. Tạo/Lấy UID cho 2 shipper
        const uid1 = await getOrCreateUser("shipper1@gmail.com", "Shipper@123");
        const uid2 = await getOrCreateUser("shipper2@gmail.com", "Shipper@123");
        
        // 2. Ghi thông tin shipper 1 vào RTDB
        const shipper1Data = {
            id: uid1,
            name: "Nguyễn Văn Hùng",
            full_name: "Nguyễn Văn Hùng",
            email: "shipper1@gmail.com",
            phone: "0911111111",
            role: "SHIPPER",
            onlineStatus: "AVAILABLE",
            online_status: "AVAILABLE",
            points: 0,
            is_active: true,
            createdAt: new Date().toISOString(),
            created_at: new Date().toISOString(),
            updated_at: new Date().toISOString()
        };
        await putJson(`${DB_URL}/users/${uid1}.json`, shipper1Data);
        await putJson(`${DB_URL}/shippers/${uid1}.json`, shipper1Data);
        console.log("[✓] Đã ghi shipper 1 (Nguyễn Văn Hùng) lên Database.");

        // Ghi thông tin shipper 2 vào RTDB
        const shipper2Data = {
            id: uid2,
            name: "Trần Quốc Bảo",
            full_name: "Trần Quốc Bảo",
            email: "shipper2@gmail.com",
            phone: "0922222222",
            role: "SHIPPER",
            onlineStatus: "AVAILABLE",
            online_status: "AVAILABLE",
            points: 0,
            is_active: true,
            createdAt: new Date().toISOString(),
            created_at: new Date().toISOString(),
            updated_at: new Date().toISOString()
        };
        await putJson(`${DB_URL}/users/${uid2}.json`, shipper2Data);
        await putJson(`${DB_URL}/shippers/${uid2}.json`, shipper2Data);
        console.log("[✓] Đã ghi shipper 2 (Trần Quốc Bảo) lên Database.");

        // 3. Tạo các đơn hàng mẫu
        const now = new Date().toISOString();
        const sampleItems = {
            "90000000-0000-0000-0000-000000000100": {
                created_at: now,
                id: "f9af5bd2-7d13-4f7a-91aa-f7107466f34f",
                image_url: "https://res.cloudinary.com/ddj1f931a/image/upload/v1781347041/vua-vui-ve/products/100-rau-muong-500g.jpg",
                product_id: "90000000-0000-0000-0000-000000000100",
                product_name: "Rau muống (500g)",
                quantity: 2,
                subtotal: 72000,
                unit: "gói",
                unit_price: 36000,
                updated_at: now
            }
        };

        const orders = {
            // Đơn hàng Chờ duyệt (Pending) chưa gán ai
            "ORD-SEED-PENDING-1": {
                id: "ORD-SEED-PENDING-1",
                created_at: now,
                updated_at: now,
                delivery_name: "Lê Minh Triết",
                delivery_phone: "0933333333",
                delivery_address: "15 Lê Lợi, Quận 1, TP. HCM",
                total_amount: 72000,
                final_amount: 102000, // +30k ship
                payment_method: "COD",
                payment_status: "UNPAID",
                status: "PENDING",
                user_id: "8e95e2c8-d95c-4a71-a8cd-c71b9dcc31f6",
                user_name: "Nguyen Chi Duc",
                user_phone: "0906760495",
                items: sampleItems,
                status_logs: {
                    "log-1": {
                        created_at: now,
                        id: "log-1",
                        note: "Đơn hàng vừa được tạo bởi seed",
                        order_id: "ORD-SEED-PENDING-1",
                        status: "PENDING",
                        updated_by_name: "Seed System",
                        updated_by_role: "SYSTEM"
                    }
                }
            },
            "ORD-SEED-PENDING-2": {
                id: "ORD-SEED-PENDING-2",
                created_at: now,
                updated_at: now,
                delivery_name: "Hoàng Ngọc Lan",
                delivery_phone: "0944444444",
                delivery_address: "88 Nguyễn Huệ, Quận 1, TP. HCM",
                total_amount: 72000,
                final_amount: 102000,
                payment_method: "COD",
                payment_status: "UNPAID",
                status: "PENDING",
                user_id: "8e95e2c8-d95c-4a71-a8cd-c71b9dcc31f6",
                user_name: "Nguyen Chi Duc",
                user_phone: "0906760495",
                items: sampleItems,
                status_logs: {
                    "log-1": {
                        created_at: now,
                        id: "log-1",
                        note: "Đơn hàng vừa được tạo bởi seed",
                        order_id: "ORD-SEED-PENDING-2",
                        status: "PENDING",
                        updated_by_name: "Seed System",
                        updated_by_role: "SYSTEM"
                    }
                }
            },

            // Đơn hàng đã gán cho Shipper 1 (Nguyễn Văn Hùng) - Confirmed
            "ORD-SEED-HUN-CONF-1": {
                id: "ORD-SEED-HUN-CONF-1",
                created_at: now,
                updated_at: now,
                delivery_name: "Vũ Hoàng Nam",
                delivery_phone: "0955555555",
                delivery_address: "102 Trần Hưng Đạo, Quận 5, TP. HCM",
                total_amount: 72000,
                final_amount: 102000,
                payment_method: "COD",
                payment_status: "UNPAID",
                status: "CONFIRMED",
                user_id: "8e95e2c8-d95c-4a71-a8cd-c71b9dcc31f6",
                user_name: "Nguyen Chi Duc",
                user_phone: "0906760495",
                shipperId: uid1,
                shipperName: "Nguyễn Văn Hùng",
                items: sampleItems,
                status_logs: {
                    "log-1": {
                        created_at: now,
                        id: "log-1",
                        note: "Đơn hàng vừa được tạo bởi seed",
                        order_id: "ORD-SEED-HUN-CONF-1",
                        status: "PENDING",
                        updated_by_name: "Seed System",
                        updated_by_role: "SYSTEM"
                    },
                    "log-2": {
                        created_at: now,
                        id: "log-2",
                        note: "Đã gán cho shipper Nguyễn Văn Hùng",
                        order_id: "ORD-SEED-HUN-CONF-1",
                        status: "CONFIRMED",
                        updated_by_name: "Admin",
                        updated_by_role: "ADMIN"
                    }
                }
            },
            "ORD-SEED-HUN-SHI-2": {
                id: "ORD-SEED-HUN-SHI-2",
                created_at: now,
                updated_at: now,
                delivery_name: "Phan Thanh Sơn",
                delivery_phone: "0966666666",
                delivery_address: "246 Điện Biên Phủ, Bình Thạnh, TP. HCM",
                total_amount: 72000,
                final_amount: 102000,
                payment_method: "COD",
                payment_status: "UNPAID",
                status: "SHIPPING",
                user_id: "8e95e2c8-d95c-4a71-a8cd-c71b9dcc31f6",
                user_name: "Nguyen Chi Duc",
                user_phone: "0906760495",
                shipperId: uid1,
                shipperName: "Nguyễn Văn Hùng",
                items: sampleItems,
                status_logs: {
                    "log-1": {
                        created_at: now,
                        id: "log-1",
                        note: "Đơn hàng vừa được tạo bởi seed",
                        order_id: "ORD-SEED-HUN-SHI-2",
                        status: "PENDING",
                        updated_by_name: "Seed System",
                        updated_by_role: "SYSTEM"
                    },
                    "log-2": {
                        created_at: now,
                        id: "log-2",
                        note: "Đã gán cho shipper Nguyễn Văn Hùng",
                        order_id: "ORD-SEED-HUN-SHI-2",
                        status: "CONFIRMED",
                        updated_by_name: "Admin",
                        updated_by_role: "ADMIN"
                    },
                    "log-3": {
                        created_at: now,
                        id: "log-3",
                        note: "Shipper bắt đầu giao hàng",
                        order_id: "ORD-SEED-HUN-SHI-2",
                        status: "SHIPPING",
                        updated_by_name: "Nguyễn Văn Hùng",
                        updated_by_role: "SHIPPER"
                    }
                }
            },

            // Đơn hàng đã gán cho Shipper 2 (Trần Quốc Bảo) - Confirmed & Delivered
            "ORD-SEED-BAO-CONF-1": {
                id: "ORD-SEED-BAO-CONF-1",
                created_at: now,
                updated_at: now,
                delivery_name: "Đặng Thu Thảo",
                delivery_phone: "0977777777",
                delivery_address: "357 Cách Mạng Tháng 8, Quận 10, TP. HCM",
                total_amount: 72000,
                final_amount: 102000,
                payment_method: "COD",
                payment_status: "UNPAID",
                status: "CONFIRMED",
                user_id: "8e95e2c8-d95c-4a71-a8cd-c71b9dcc31f6",
                user_name: "Nguyen Chi Duc",
                user_phone: "0906760495",
                shipperId: uid2,
                shipperName: "Trần Quốc Bảo",
                items: sampleItems,
                status_logs: {
                    "log-1": {
                        created_at: now,
                        id: "log-1",
                        note: "Đơn hàng vừa được tạo bởi seed",
                        order_id: "ORD-SEED-BAO-CONF-1",
                        status: "PENDING",
                        updated_by_name: "Seed System",
                        updated_by_role: "SYSTEM"
                    },
                    "log-2": {
                        created_at: now,
                        id: "log-2",
                        note: "Đã gán cho shipper Trần Quốc Bảo",
                        order_id: "ORD-SEED-BAO-CONF-1",
                        status: "CONFIRMED",
                        updated_by_name: "Admin",
                        updated_by_role: "ADMIN"
                    }
                }
            },
            "ORD-SEED-BAO-DELI-2": {
                id: "ORD-SEED-BAO-DELI-2",
                created_at: now,
                updated_at: now,
                delivery_name: "Trần Anh Tuấn",
                delivery_phone: "0988888888",
                delivery_address: "12 Song Hành, TP. Thủ Đức, TP. HCM",
                total_amount: 72000,
                final_amount: 102000,
                payment_method: "COD",
                payment_status: "PAID",
                status: "DELIVERED",
                user_id: "8e95e2c8-d95c-4a71-a8cd-c71b9dcc31f6",
                user_name: "Nguyen Chi Duc",
                user_phone: "0906760495",
                shipperId: uid2,
                shipperName: "Trần Quốc Bảo",
                items: sampleItems,
                status_logs: {
                    "log-1": {
                        created_at: now,
                        id: "log-1",
                        note: "Đơn hàng vừa được tạo bởi seed",
                        order_id: "ORD-SEED-BAO-DELI-2",
                        status: "PENDING",
                        updated_by_name: "Seed System",
                        updated_by_role: "SYSTEM"
                    },
                    "log-2": {
                        created_at: now,
                        id: "log-2",
                        note: "Đã gán cho shipper Trần Quốc Bảo",
                        order_id: "ORD-SEED-BAO-DELI-2",
                        status: "CONFIRMED",
                        updated_by_name: "Admin",
                        updated_by_role: "ADMIN"
                    },
                    "log-3": {
                        created_at: now,
                        id: "log-3",
                        note: "Shipper bắt đầu giao hàng",
                        order_id: "ORD-SEED-BAO-DELI-2",
                        status: "SHIPPING",
                        updated_by_name: "Trần Quốc Bảo",
                        updated_by_role: "SHIPPER"
                    },
                    "log-4": {
                        created_at: now,
                        id: "log-4",
                        note: "Giao hàng thành công",
                        order_id: "ORD-SEED-BAO-DELI-2",
                        status: "DELIVERED",
                        updated_by_name: "Trần Quốc Bảo",
                        updated_by_role: "SHIPPER"
                    }
                }
            }
        };

        for (const orderId of Object.keys(orders)) {
            await putJson(`${DB_URL}/orders/${orderId}.json`, orders[orderId]);
            console.log(`[✓] Đã ghi đơn hàng ${orderId} lên Database.`);
        }

        console.log("=== SEED THÀNH CÔNG RỰC RỠ! ===");
        console.log("Thông tin tài khoản để đăng nhập test:");
        console.log(`- Shipper 1: shipper1@gmail.com / Shipper@123 (Tên: Nguyễn Văn Hùng)`);
        console.log(`- Shipper 2: shipper2@gmail.com / Shipper@123 (Tên: Trần Quốc Bảo)`);

    } catch(e) {
        console.error("[x] Lỗi xảy ra khi chạy seed:", e.message);
    }
}

runSeed();
