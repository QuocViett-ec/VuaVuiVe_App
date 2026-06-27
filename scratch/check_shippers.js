const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/shippers.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
        try {
            const shippers = JSON.parse(data);
            if (shippers) {
                const keys = Object.keys(shippers);
                if (keys.length > 0) {
                    console.log("Sample Shipper Structure in /shippers:", JSON.stringify({ key: keys[0], data: shippers[keys[0]] }, null, 2));
                } else {
                    console.log("No shippers node content found.");
                }
            } else {
                console.log("No shippers node found.");
            }
        } catch(e) {
            console.log("Err parsing shippers:", e.message);
        }
    });
});
