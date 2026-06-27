const sqlite3 = require('sqlite3').verbose();
const db = new sqlite3.Database('app-backend/vuavuive_v2.db');
db.all("SELECT id, name, stock_quantity FROM products WHERE name LIKE '%Quoc%' OR name LIKE '%Viet%'", [], (err, rows) => {
  if (err) {
    console.error(err);
    return;
  }
  rows.forEach(r => console.log(JSON.stringify(r)));
  db.close();
});
