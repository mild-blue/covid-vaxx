# Covid Vaxx Backend

TBD

## Test data

To create test user:

```json
{
  "username": "mildblue",
  "password": "bluemild",
  "role": "ADMIN"
}
```

use following SQL insert

```sql
INSERT INTO users (id, password_hash, "role", username)
VALUES ('ee99ce15-ad00-4dbc-a31d-faf732272c77', '$s0$e0801$asDyD5znh458o/+vCMIaLw==$zydsv6Cw2fKxkIGqFNFMDWQ47pKdHIInLURYOeVlYuA=', 'ADMIN', 'mildblue')
```
