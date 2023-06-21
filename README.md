# SisGes

### App that tracks products in a warehouse.

For authentication it uses Firebase Authentication with Email/Password.

For databases it uses Firebase Firestore Database.

For uploading photos it uses Firebase Storage.

## Collections structure
<img src="https://github.com/mihai301196/SisGesViewBinding/blob/master/assets/database-structure.png">

administration: type (integer), productCode (string), productName (string), quantity (integer), timestamp (long)

products: code (string), name (string)

storage: productCode (string), positionCode (string), quantity (integer)

storage_positions: code (string)

users: id (string), email (string), firstName (string), lastName (string), photoUri (string)

## Database additional information:
Collections "products" and "storage_positions" must be filled before using the app.
<p>Field "code" of "products" collection must be a string of 10 digits.</p>
<p>The format of the "code" field of the "storage_positions" collection is "ZxRxRx", where Z=zone, (first)R=region, (second)R=row and x is a number from 0 to 9. (e.g., Z1R1R1, Z1R1R2, etc).</p>
