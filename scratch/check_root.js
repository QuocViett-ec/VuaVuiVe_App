const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/.json?shallow=true', (res) => {
    let data = '';
    res.on('data', (chunk) => {
        data += chunk;
    });
    res.on('end', () => {
        try {
            const root = JSON.parse(data);
            console.log("--- FIREBASE RTDB ROOT KEYS ---");
            console.log(JSON.stringify(root, null, 2));
        } catch (e) {
            console.error("Error parsing JSON:", e.message);
        }
    });
}).on('error', (err) => {
    console.error("Error fetching data:", err.message);
});
