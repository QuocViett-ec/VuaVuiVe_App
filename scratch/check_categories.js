const { initializeApp, cert } = require('firebase-admin/app');
const { getDatabase } = require('firebase-admin/database');
const serviceAccount = require('../serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount),
  databaseURL: 'https://vua-vui-ve-default-rtdb.firebaseio.com'
});

const db = getDatabase();
db.ref('categories').once('value', (snapshot) => {
  const cats = snapshot.val();
  console.log("=== CATEGORIES ===");
  console.log(JSON.stringify(cats, null, 2));
  process.exit(0);
}).catch((err) => {
  console.error("Error:", err);
  process.exit(1);
});
