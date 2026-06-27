const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/orders.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
        try {
            const orders = JSON.parse(data);
            console.log("All order IDs and statuses:");
            for (const key of Object.keys(orders)) {
                console.log(`- ${key}: status = ${orders[key].status}, shipper = ${orders[key].shipperName || 'none'}`);
            }
        } catch(e) {
            console.log("Err:", e.message);
        }
    });
});
