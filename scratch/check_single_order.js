const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/orders/f0a850ce-f5ec-400a-9cdf-99bb3291c89c.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
        try {
            console.log("Order Detail for f0a850ce-f5ec-400a-9cdf-99bb3291c89c:");
            console.log(JSON.stringify(JSON.parse(data), null, 2));
        } catch(e) {
            console.log("Err:", e.message);
        }
    });
});
