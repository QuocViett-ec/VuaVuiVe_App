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
            console.log("=== FIREBASE USERS ===");
            for (const key of Object.keys(users)) {
                const u = users[key];
                console.log(`UID: ${key}`);
                console.log(`  Name: ${u.fullName || u.name || 'N/A'}`);
                console.log(`  Email: ${u.email || 'N/A'}`);
                console.log(`  Phone: ${u.phone || 'N/A'}`);
                console.log(`  Role: ${u.role || 'N/A'}`);
                console.log(`  Active: ${u.isActive !== undefined ? u.isActive : 'N/A'}`);
                console.log(`  PasswordHash: ${u.passwordHash || u.password || 'N/A'}`);
                console.log("------------------------");
            }
        } catch (e) {
            console.error("Error parsing JSON:", e.message);
        }
    });
}).on('error', (err) => {
    console.error("Error:", err.message);
});
