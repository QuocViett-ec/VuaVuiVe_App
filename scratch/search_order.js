const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/orders.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
        try {
            const orders = JSON.parse(data);
            console.log("Search results:");
            for (const key of Object.keys(orders)) {
                const o = orders[key];
                const orderStr = JSON.stringify(o);
                if (orderStr.includes("Hieu") || orderStr.includes("1782289694879") || orderStr.includes("17822")) {
                    console.log(`Key: ${key}`);
                    console.log(`ID: ${o.id}, OrderID: ${o.order_id}, Name: ${o.delivery_name}, Amount: ${o.final_amount}`);
                }
            }
        } catch(e) {
            console.log("Err:", e.message);
        }
    });
});
