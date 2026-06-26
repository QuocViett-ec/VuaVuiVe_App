const https = require('https');

https.get('https://vua-vui-ve-default-rtdb.firebaseio.com/users.json', (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
        try {
            const users = JSON.parse(data);
            if (!users) {
                console.log("No users found.");
                return;
            }
            console.log("--- USERS ROLE CHECK ---");
            for (const key of Object.keys(users)) {
                const u = users[key];
                if (u && (u.email === 'admin@vuavuive.vn' || u.role === 'admin' || u.role === 'audit')) {
                    console.log(`Key: ${key} | Name: ${u.name} | Email: ${u.email} | Role: ${u.role}`);
                }
            }
        } catch (e) {
            console.error("Error parsing JSON:", e.message);
        }
    });
}).on('error', (err) => {
    console.error("Error:", err.message);
});
