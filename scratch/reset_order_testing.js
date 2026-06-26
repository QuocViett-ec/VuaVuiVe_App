const https = require('https');

function makeRequest(url, method = 'GET', postData = null) {
    return new Promise((resolve, reject) => {
        const parsedUrl = new URL(url);
        const options = {
            hostname: parsedUrl.hostname,
            path: parsedUrl.pathname + parsedUrl.search,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        const req = https.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                try {
                    resolve(JSON.parse(data));
                } catch (e) {
                    resolve(data);
                }
            });
        });

        req.on('error', (e) => reject(e));

        if (postData) {
            req.write(JSON.stringify(postData));
        }
        req.end();
    });
}

async function run() {
    try {
        console.log("Fetching shippers...");
        const shippers = await makeRequest('https://vua-vui-ve-default-rtdb.firebaseio.com/shippers.json');
        
        let hung = null;
        let bao = null;
        for (const id in shippers) {
            const s = shippers[id];
            if (s.email === 'shipper1@gmail.com') hung = { id, ...s };
            if (s.email === 'shipper2@gmail.com') bao = { id, ...s };
        }

        console.log("Shipper 1 (Hùng):", hung ? hung.id : "Not found");
        console.log("Shipper 2 (Bảo):", bao ? bao.id : "Not found");

        if (!hung || !bao) {
            console.log("Failed to find shippers!");
            return;
        }

        console.log("\nFetching current ORD-SEED-PENDING-2...");
        const order2 = await makeRequest('https://vua-vui-ve-default-rtdb.firebaseio.com/orders/ORD-SEED-PENDING-2.json');
        console.log("Current order 2:", JSON.stringify(order2, null, 2));

        console.log("\nResetting ORD-SEED-PENDING-2 (Assigning to Bảo, status = IN_TRANSIT, unpaid)...");
        const updateData2 = {
            shipperId: bao.id,
            shipperName: bao.full_name || bao.name || "Trần Quốc Bảo",
            status: "IN_TRANSIT",
            paymentStatus: "UNPAID",
            payment_status: "UNPAID",
            payment: { status: "UNPAID" },
            stock_restored: false
        };
        const res2 = await makeRequest('https://vua-vui-ve-default-rtdb.firebaseio.com/orders/ORD-SEED-PENDING-2.json', 'PATCH', updateData2);
        console.log("Update ORD-SEED-PENDING-2 response:", res2);

        console.log("\nResetting ORD-SEED-PENDING-1 (Assigning to Hùng, status = IN_TRANSIT, unpaid)...");
        const updateData1 = {
            shipperId: hung.id,
            shipperName: hung.full_name || hung.name || "Nguyễn Văn Hùng",
            status: "IN_TRANSIT",
            paymentStatus: "UNPAID",
            payment_status: "UNPAID",
            payment: { status: "UNPAID" },
            stock_restored: false
        };
        const res1 = await makeRequest('https://vua-vui-ve-default-rtdb.firebaseio.com/orders/ORD-SEED-PENDING-1.json', 'PATCH', updateData1);
        console.log("Update ORD-SEED-PENDING-1 response:", res1);

        console.log("\nDone resetting!");
    } catch (e) {
        console.error("Error:", e);
    }
}

run();
