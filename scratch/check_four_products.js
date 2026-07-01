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
  
  const targetKeys = [
    'e60e6b03-180b-44d1-afde-2877b9355aa8',
    'fa177751-8761-4007-a4b5-574fc1a7c1d9',
    'prod-a635367e-2d37-4942-a81a-230f507c8f3a',
    'prod-5770e6b1-e5e2-4a5c-93c7-8eeeb9c0f42b'
  ];

  for (const key of targetKeys) {
    console.log(`=== Product: ${key} ===`);
    console.log(JSON.stringify(products[key], null, 2));
  }
  process.exit(0);
}).catch((err) => {
  console.error("Error reading database:", err);
  process.exit(1);
});
