import jwt
import time

payload = {
    "sub": "test_user_id",
    "role": "ROLE_CUSTOMER",
    "iat": int(time.time()),
    "exp": int(time.time()) + 3600
}
token = jwt.encode(payload, "vuavuive-super-secret-key-for-jwt-please-change-in-production-min-256bit", algorithm="HS256")
print(token)
