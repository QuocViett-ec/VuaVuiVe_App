const { initializeApp, cert } = require('firebase-admin/app');
const { getDatabase } = require('firebase-admin/database');
const serviceAccount = require('../serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount),
  databaseURL: 'https://vua-vui-ve-default-rtdb.firebaseio.com'
});

const db = getDatabase();
const ref = db.ref('products');

ref.once('value', async (snapshot) => {
  const products = snapshot.val();
  if (!products) {
    console.log("No products found.");
    process.exit(0);
  }
  
  let count = 0;
  for (const key in products) {
    const p = products[key];
    
    const hasNoPrice = 
      (p.selling_price === undefined || p.selling_price === null || p.selling_price === 0 || p.selling_price === "0" || p.selling_price === 0.0) &&
      (p.sellingPrice === undefined || p.sellingPrice === null || p.sellingPrice === 0 || p.sellingPrice === "0" || p.sellingPrice === 0.0) &&
      (p.price === undefined || p.price === null || p.price === 0 || p.price === "0" || p.price === 0.0);

    if (hasNoPrice) {
      console.log(`Deleting product with zero or missing price: ${p.name || 'Unnamed'} (Key: ${key})`);
      await ref.child(key).remove();
      count++;
    }
  }
  console.log(`Deletion complete. Total products deleted: ${count}`);
  process.exit(0);
}).catch((err) => {
  console.error("Error reading database:", err);
  process.exit(1);
});
