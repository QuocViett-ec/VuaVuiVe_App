const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/orders.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
        try {
            const orders = JSON.parse(data);
            console.log("=== SEARCHING SPECIFIC ORDER ON FIREBASE ===");
            for (const key in orders) {
                const order = orders[key];
                
                const addressStr = order.delivery_address || order.deliveryAddress || order.recipientAddress || "";
                const nameStr = order.delivery_name || order.deliveryName || order.recipientName || order.recipient_name || "";
                const phoneStr = order.delivery_phone || order.deliveryPhone || order.recipientPhone || order.recipient_phone || "";

                const matchPhone = phoneStr.includes('1131231') || addressStr.includes('1131231');
                const matchName = nameStr.toLowerCase().includes('quoc viet');

                if (matchPhone || matchName) {
                    console.log(`FOUND MATCH! Key/ID: ${key}`);
                    console.log(JSON.stringify(order, null, 2));
                }
            }
        } catch(e) {
            console.log("Err:", e.message);
        }
    });
});
