const { initializeApp, cert } = require('firebase-admin/app');
const { getDatabase } = require('firebase-admin/database');
const serviceAccount = require('../serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount),
  databaseURL: 'https://vua-vui-ve-default-rtdb.firebaseio.com'
});

const db = getDatabase();
db.ref('products').once('value', (snapshot) => {
  const productsVal = snapshot.val();
  if (!productsVal) {
    console.log("No products");
    process.exit(0);
  }
  
  let products = [];
  for (const key in productsVal) {
    const s = productsVal[key];
    
    const p = {};
    p.id = s.id;
    p.name = s.name;
    p.slug = s.slug;
    
    let sellingPrice = s.selling_price;
    if (sellingPrice === undefined || sellingPrice === null) {
      sellingPrice = s.sellingPrice;
    }
    p.price = sellingPrice !== undefined && sellingPrice !== null ? Number(sellingPrice) : 0.0;
    
    let active = s.is_active;
    if (active === undefined || active === null) {
      active = s.isActive;
    }
    p.active = active !== undefined && active !== null ? active : false;
    
    let createdAt = s.created_at;
    if (createdAt === undefined || createdAt === null) {
      createdAt = s.createdAt;
    }
    p.createdAt = createdAt || "";
    
    if (p.active) {
      products.push(p);
    }
  }
  
  // Sort by newest descending
  products.sort((p1, p2) => {
    return p2.createdAt.localeCompare(p1.createdAt);
  });
  
  const limit = 20;
  const page = 1;
  const startIndex = (page - 1) * limit;
  const pageProducts = products.slice(startIndex, startIndex + limit);
  
  console.log("=== FIRST 20 PRODUCTS WITH IDS ===");
  pageProducts.forEach((p, index) => {
    console.log(`${index + 1}. Name: ${p.name} | ID: ${p.id} | CreatedAt: ${p.createdAt} | Price: ${p.price}`);
  });
  
  process.exit(0);
}).catch(err => {
  console.error(err);
  process.exit(1);
});
