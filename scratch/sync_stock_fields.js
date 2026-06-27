const https = require('https');

// Helper to make a PATCH request
function patchFirebase(path, data) {
    return new Promise((resolve, reject) => {
        const payload = JSON.stringify(data);
        const options = {
            hostname: 'vua-vui-ve-default-rtdb.firebaseio.com',
            path: path,
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(payload)
            }
        };

        const req = https.request(options, (res) => {
            let body = '';
            res.on('data', (chunk) => body += chunk);
            res.on('end', () => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    resolve(JSON.parse(body));
                } else {
                    reject(new Error(`Status: ${res.statusCode}, Body: ${body}`));
                }
            });
        });

        req.on('error', (err) => reject(err));
        req.write(payload);
        req.end();
    });
}

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/products.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', async () => {
        try {
            const products = JSON.parse(data);
            if (!products) {
                console.log("No products found.");
                return;
            }

            console.log("Starting stock fields synchronization...");
            let updateCount = 0;

            for (const key of Object.keys(products)) {
                const p = products[key];
                if (!p) continue;

                const sq = p.stock_quantity;
                const s = p.stock;

                let targetStock = undefined;
                if (sq !== undefined) {
                    targetStock = sq;
                } else if (s !== undefined) {
                    targetStock = s;
                }

                if (targetStock !== undefined) {
                    // Check if either field is missing or they are not equal
                    if (sq !== targetStock || s !== targetStock) {
                        console.log(`Syncing product: ${p.name} (${key}) -> stock = ${targetStock}, stock_quantity = ${targetStock}`);
                        await patchFirebase(`/products/${key}.json`, {
                            stock: targetStock,
                            stock_quantity: targetStock
                        });
                        updateCount++;
                    }
                }
            }

            console.log(`Synchronization finished. Updated ${updateCount} products.`);
        } catch (e) {
            console.error("Error during sync:", e.message);
        }
    });
}).on('error', (err) => {
    console.error("Error fetching products:", err.message);
});
