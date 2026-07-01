const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/products.json', (res) => {
    let data = '';
    res.on('data', (chunk) => { data += chunk; });
    res.on('end', () => {
        try {
            const products = JSON.parse(data);
            console.log("Total products fetched:", Object.keys(products).length);
            let count = 0;
            for (const key in products) {
                if (count++ < 10) {
                    console.log("Product:", key, JSON.stringify(products[key], null, 2));
                }
            }
        } catch (e) {
            console.error("Error parsing JSON:", e.message);
        }
    });
}).on('error', (err) => {
    console.error("Error fetching data:", err.message);
});
