const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/users.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
        try {
            const users = JSON.parse(data);
            const keys = Object.keys(users);
            if (keys.length > 0) {
                console.log("Sample User Structure:", JSON.stringify({ key: keys[0], data: users[keys[0]] }, null, 2));
            } else {
                console.log("No users found.");
            }
        } catch(e) {
            console.log("Err parsing users:", e.message);
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
