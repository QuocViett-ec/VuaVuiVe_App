const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/products.json', (res) => {
    let data = '';
    res.on('data', (chunk) => {
        data += chunk;
    });
    res.on('end', () => {
        try {
            const products = JSON.parse(data);
            if (!products) {
                console.log("No products found.");
                return;
            }
            console.log("--- ANALYSIS OF PRODUCTS STOCK ---");
            let noStockField = [];
            let zeroStock = [];
            let validStockCount = 0;

            Object.keys(products).forEach((key) => {
                const p = products[key];
                if (!p) return;
                
                const sq = p.stock_quantity;
                const s = p.stock;

                if (sq === undefined && s === undefined) {
                    noStockField.push({ id: key, name: p.name });
                } else {
                    const stockVal = sq !== undefined ? sq : s;
                    if (stockVal <= 0) {
                        zeroStock.push({ id: key, name: p.name, stock: stockVal });
                    } else {
                        validStockCount++;
                    }
                }
            });

            console.log(`Valid products count: ${validStockCount}`);
            console.log(`Products with zero/negative stock count: ${zeroStock.length}`);
            zeroStock.forEach(p => console.log(`  - ${p.name} (${p.id}): stock = ${p.stock}`));
            
            console.log(`Products missing stock fields completely count: ${noStockField.length}`);
            noStockField.forEach(p => console.log(`  - ${p.name} (${p.id})`));
        } catch (e) {
            console.error("Error parsing JSON:", e.message);
        }
    });
}).on('error', (err) => {
    console.error("Error fetching data:", err.message);
});
