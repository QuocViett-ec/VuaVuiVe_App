const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/products.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
        try {
            const products = JSON.parse(data);
            const keys = Object.keys(products);
            if (keys.length > 0) {
                console.log("Sample Product Structure:", JSON.stringify({ key: keys[0], data: products[keys[0]] }, null, 2));
            } else {
                console.log("No products found.");
            }
        } catch(e) {
            console.log("Err parsing products:", e.message);
        }
    });
});

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/orders.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
        try {
            const orders = JSON.parse(data);
            const keys = Object.keys(orders);
            if (keys.length > 0) {
                console.log("Sample Order Structure:", JSON.stringify({ key: keys[0], data: orders[keys[0]] }, null, 2));
            } else {
                console.log("No orders found.");
            }
        } catch(e) {
            console.log("Err parsing orders:", e.message);
        }
    });
});
