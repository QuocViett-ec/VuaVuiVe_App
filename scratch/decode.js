const fs = require('fs');
for (const name of ['vvv_db', 'vvv_db-wal', 'vvv_db-shm']) {
    try {
        const path = 'scratch/' + name + '.b64';
        if (!fs.existsSync(path)) {
            console.log('Skipping non-existent', path);
            continue;
        }
        const b64 = fs.readFileSync(path, 'utf8').trim();
        const buf = Buffer.from(b64, 'base64');
        fs.writeFileSync('scratch/' + name, buf);
        console.log('Decoded', name);
    } catch (e) {
        console.error('Error decoding', name, e.message);
    }
}
