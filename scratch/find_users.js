const { initializeApp, cert } = require('firebase-admin/app');
const { getDatabase } = require('firebase-admin/database');
const serviceAccount = require('../serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount),
  databaseURL: 'https://vua-vui-ve-default-rtdb.firebaseio.com'
});

const db = getDatabase();
db.ref('users').once('value', (snapshot) => {
  const users = snapshot.val();
  console.log("=== USERS ===");
  for (const key in users) {
    const u = users[key];
    console.log(`Phone: ${u.phoneNumber || u.phone} | Email: ${u.email} | Name: ${u.fullName || u.name} | Role: ${u.role}`);
  }
  process.exit(0);
}).catch(err => {
  console.error(err);
  process.exit(1);
});
