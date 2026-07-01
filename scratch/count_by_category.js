const { initializeApp, cert } = require('firebase-admin/app');
const { getDatabase } = require('firebase-admin/database');
const serviceAccount = require('../serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount),
  databaseURL: 'https://vua-vui-ve-default-rtdb.firebaseio.com'
});

const db = getDatabase();
db.ref('products').once('value', (snapshot) => {
  const products = snapshot.val();
  const catCounts = {};
  for (const key in products) {
    const p = products[key];
    const cat = p.category_id || p.categoryId;
    const active = p.is_active !== undefined ? p.is_active : p.isActive;
    if (active) {
      catCounts[cat] = (catCounts[cat] || 0) + 1;
    }
  }
  console.log("=== ACTIVE PRODUCTS PER CATEGORY ===");
  console.log(catCounts);
  process.exit(0);
}).catch(err => {
  console.error(err);
  process.exit(1);
});
