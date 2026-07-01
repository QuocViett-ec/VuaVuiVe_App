const { initializeApp, cert } = require('firebase-admin/app');
const { getDatabase } = require('firebase-admin/database');
const serviceAccount = require('../serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount),
  databaseURL: 'https://vua-vui-ve-default-rtdb.firebaseio.com'
});

const db = getDatabase();
const ref = db.ref('products');

ref.once('value', (snapshot) => {
  const products = snapshot.val();
  if (!products) {
    console.log("No products found.");
    process.exit(0);
  }
  
  console.log("Total products fetched:", Object.keys(products).length);
  for (const key in products) {
    const p = products[key];
    console.log(`Product Key: ${key} | Name: ${p.name} | Price: ${p.price} | SellingPrice: ${p.sellingPrice} | selling_price: ${p.selling_price}`);
  }
  process.exit(0);
}).catch((err) => {
  console.error("Error reading database:", err);
  process.exit(1);
});
