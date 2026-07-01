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
  
  let printed = 0;
  for (const key in products) {
    const p = products[key];
    if (key.startsWith('90000000') && printed < 5) {
      console.log(`Key: ${key}`);
      console.log(JSON.stringify(p, null, 2));
      console.log(`Type of is_active: ${typeof p.is_active}`);
      printed++;
    }
  }
  process.exit(0);
}).catch((err) => {
  console.error("Error reading database:", err);
  process.exit(1);
});
