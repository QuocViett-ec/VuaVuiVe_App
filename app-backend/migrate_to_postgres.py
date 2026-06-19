"""
Migration: SQLite (vuavuive_v2.db) -> PostgreSQL (vuavuive_app)
"""

import sqlite3
import sys
import os

# Force UTF-8 output
os.environ.setdefault('PYTHONIOENCODING', 'utf-8')

# ===== CONFIG =====
SQLITE_FILE   = "vuavuive_v2.db"
PG_HOST       = "localhost"
PG_PORT       = 5432
PG_DB         = "vuavuive_app"
PG_USER       = "postgres"
PG_PASS       = "Viet0609"
# ==================

def log(msg):
    sys.stdout.buffer.write((msg + "\n").encode("utf-8"))
    sys.stdout.buffer.flush()

# Auto-install psycopg2 if missing
try:
    import psycopg2
except ImportError:
    log("[INFO] Installing psycopg2-binary...")
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "psycopg2-binary", "-q"])
    import psycopg2

from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

# -------------------------------------------------------
# STEP 1: Create database
# -------------------------------------------------------
log(f"\n[1/4] Creating database '{PG_DB}'...")
conn_root = psycopg2.connect(
    host=PG_HOST, port=PG_PORT,
    dbname="postgres",
    user=PG_USER, password=PG_PASS
)
conn_root.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
cur_root = conn_root.cursor()

cur_root.execute("SELECT 1 FROM pg_database WHERE datname=%s", (PG_DB,))
if cur_root.fetchone():
    log(f"  Database '{PG_DB}' already exists - dropping and recreating...")
    cur_root.execute(f'DROP DATABASE IF EXISTS "{PG_DB}"')

cur_root.execute(f'CREATE DATABASE "{PG_DB}" ENCODING \'UTF8\'')
log(f"  [OK] Database '{PG_DB}' created")
cur_root.close()
conn_root.close()

# -------------------------------------------------------
# STEP 2: Create tables
# -------------------------------------------------------
log(f"\n[2/4] Creating tables in '{PG_DB}'...")
conn_pg = psycopg2.connect(
    host=PG_HOST, port=PG_PORT,
    dbname=PG_DB,
    user=PG_USER, password=PG_PASS
)
conn_pg.autocommit = False
cur_pg = conn_pg.cursor()

DDL_STATEMENTS = [
    """CREATE TABLE IF NOT EXISTS users (
        id            VARCHAR(36)  PRIMARY KEY,
        created_at    TIMESTAMP,
        updated_at    TIMESTAMP,
        avatar_url    VARCHAR(255),
        email         VARCHAR(255) UNIQUE,
        full_name     VARCHAR(255) NOT NULL,
        is_active     BOOLEAN      NOT NULL,
        password_hash VARCHAR(255),
        phone         VARCHAR(255) UNIQUE,
        role          VARCHAR(50)  NOT NULL CHECK (role IN ('CUSTOMER','STAFF','ADMIN','SHIPPER'))
    )""",
    """CREATE TABLE IF NOT EXISTS categories (
        id         VARCHAR(36)  PRIMARY KEY,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        image_url  VARCHAR(255),
        is_active  BOOLEAN      NOT NULL,
        name       VARCHAR(255) NOT NULL,
        slug       VARCHAR(255) UNIQUE,
        parent_id  VARCHAR(36)  REFERENCES categories(id)
    )""",
    """CREATE TABLE IF NOT EXISTS products (
        id             VARCHAR(36)    PRIMARY KEY,
        created_at     TIMESTAMP,
        updated_at     TIMESTAMP,
        description    TEXT,
        image_url      VARCHAR(255),
        is_active      BOOLEAN        NOT NULL,
        name           VARCHAR(255)   NOT NULL,
        original_price NUMERIC(12,2)  NOT NULL,
        selling_price  NUMERIC(12,2)  NOT NULL,
        stock_quantity INTEGER        NOT NULL,
        unit           VARCHAR(255)   NOT NULL,
        category_id    VARCHAR(36)    NOT NULL REFERENCES categories(id),
        slug           TEXT,
        sub_category   TEXT,
        tags           TEXT,
        external_id    TEXT
    )""",
    """CREATE TABLE IF NOT EXISTS shippers (
        id             VARCHAR(36)  PRIMARY KEY,
        created_at     TIMESTAMP,
        updated_at     TIMESTAMP,
        current_status VARCHAR(50)  NOT NULL CHECK (current_status IN ('AVAILABLE','DELIVERING','OFFLINE')),
        full_name      VARCHAR(255) NOT NULL,
        is_active      BOOLEAN      NOT NULL,
        phone          VARCHAR(255) NOT NULL UNIQUE,
        vehicle_number VARCHAR(255)
    )""",
    """CREATE TABLE IF NOT EXISTS orders (
        id               VARCHAR(36)   PRIMARY KEY,
        created_at       TIMESTAMP,
        updated_at       TIMESTAMP,
        delivery_address TEXT,
        delivery_name    VARCHAR(255),
        delivery_phone   VARCHAR(20),
        final_amount     NUMERIC(12,2) NOT NULL,
        note             VARCHAR(255),
        payment_method   VARCHAR(50)   NOT NULL,
        payment_status   VARCHAR(50)   NOT NULL CHECK (payment_status IN ('UNPAID','PENDING','PAID','FAILED','CANCELLED','REFUNDED')),
        status           VARCHAR(50)   NOT NULL CHECK (status IN ('PENDING','CONFIRMED','SHIPPING','PREPARING','READY_FOR_PICKUP','IN_TRANSIT','DELIVERED','FAILED','RETURNED','CANCELLED')),
        total_amount     NUMERIC(12,2) NOT NULL,
        shipper_id       VARCHAR(36)   REFERENCES shippers(id),
        user_id          VARCHAR(36)   NOT NULL REFERENCES users(id)
    )""",
    """CREATE TABLE IF NOT EXISTS order_items (
        id          VARCHAR(36)   PRIMARY KEY,
        created_at  TIMESTAMP,
        updated_at  TIMESTAMP,
        quantity    INTEGER       NOT NULL,
        subtotal    NUMERIC(12,2) NOT NULL,
        unit_price  NUMERIC(12,2) NOT NULL,
        order_id    VARCHAR(36)   NOT NULL REFERENCES orders(id),
        product_id  VARCHAR(36)   NOT NULL REFERENCES products(id)
    )""",
    """CREATE TABLE IF NOT EXISTS order_status_logs (
        id               VARCHAR(36)   PRIMARY KEY,
        created_at       TIMESTAMP,
        note             VARCHAR(255),
        status           VARCHAR(50)   NOT NULL CHECK (status IN ('PENDING','CONFIRMED','PREPARING','READY_FOR_PICKUP','IN_TRANSIT','DELIVERED','FAILED','RETURNED','CANCELLED')),
        updated_by_id    VARCHAR(36),
        updated_by_name  VARCHAR(255),
        updated_by_role  VARCHAR(255)  NOT NULL,
        order_id         VARCHAR(36)   NOT NULL REFERENCES orders(id)
    )""",
    """CREATE TABLE IF NOT EXISTS recipes (
        id          VARCHAR(255) PRIMARY KEY,
        category    VARCHAR(255),
        cook_time   VARCHAR(255),
        description TEXT,
        difficulty  VARCHAR(255),
        image       TEXT,
        ingredients TEXT,
        name        VARCHAR(255),
        prep_time   VARCHAR(255),
        steps       TEXT
    )""",
]

for ddl in DDL_STATEMENTS:
    cur_pg.execute(ddl)
conn_pg.commit()
log("  [OK] All tables created")

# -------------------------------------------------------
# STEP 3: Migrate data from SQLite
# -------------------------------------------------------
log(f"\n[3/4] Migrating data from SQLite...")

conn_sq = sqlite3.connect(SQLITE_FILE)
conn_sq.row_factory = sqlite3.Row
cur_sq = conn_sq.cursor()

def blob_to_uuid(v):
    import uuid
    if isinstance(v, bytes):
        try:
            return str(uuid.UUID(bytes=v))
        except Exception:
            return v.hex()
    return v

def migrate_table(table_name):
    try:
        cur_sq.execute(f'SELECT * FROM "{table_name}"')
    except sqlite3.OperationalError as e:
        if "no such table" in str(e):
            log(f"  - {table_name}: not found in SQLite (skip)")
            return 0
        raise e
    rows = cur_sq.fetchall()
    if not rows:
        log(f"  - {table_name}: 0 rows (skip)")
        return 0

    cols = list(rows[0].keys())
    col_str = ", ".join(f'"{c}"' for c in cols)
    placeholders = ", ".join(["%s"] * len(cols))
    sql = f'INSERT INTO "{table_name}" ({col_str}) VALUES ({placeholders}) ON CONFLICT DO NOTHING'

    data = []
    for row in rows:
        vals = []
        for c in cols:
            v = row[c]
            if isinstance(v, bytes):
                v = blob_to_uuid(v)
            elif isinstance(v, int) and c == 'is_active':
                v = bool(v)
            vals.append(v)
        data.append(tuple(vals))

    cur_pg.executemany(sql, data)
    log(f"  [OK] {table_name}: {len(data)} rows migrated")
    return len(data)

MIGRATION_ORDER = [
    "users", "categories", "products", "shippers",
    "orders", "order_items", "order_status_logs", "recipes"
]

total = 0
for table in MIGRATION_ORDER:
    total += migrate_table(table)

conn_sq.close()
conn_pg.commit()
log(f"\n  Total: {total} rows migrated")

# -------------------------------------------------------
# STEP 4: Verify
# -------------------------------------------------------
log(f"\n[4/4] Verify row counts in PostgreSQL:")
for table in MIGRATION_ORDER:
    cur_pg.execute(f'SELECT COUNT(*) FROM "{table}"')
    cnt = cur_pg.fetchone()[0]
    log(f"  {table}: {cnt} rows")

cur_pg.close()
conn_pg.close()

log("\n========================================")
log("[DONE] Migration complete!")
log(f"  Host: {PG_HOST}:{PG_PORT}")
log(f"  DB  : {PG_DB}")
log("========================================")
