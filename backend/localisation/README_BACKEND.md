# LocalisationLab PHP Backend

This folder contains the PHP/MySQL backend for the Android GPS localisation lab.

## XAMPP Setup

Copy `backend/localisation` to:

```text
C:\xampp\htdocs\localisation
```

The PHP files should then be reachable from:

```text
http://PC_IP/localisation/createPosition.php
http://PC_IP/localisation/showPositions.php
```

## Database Setup

1. Open phpMyAdmin.
2. Import `database.sql`.
3. Confirm that the database is named `localisation`.
4. Confirm that the table is named `position`.

The table structure is:

```sql
id int primary key auto_increment
latitude double not null
longitude double not null
date datetime not null
imei varchar(20) not null
```

## Test URLs

Use POST requests:

```text
POST http://PC_IP/localisation/createPosition.php
POST http://PC_IP/localisation/showPositions.php
```

Example `createPosition.php` POST fields:

```text
latitude=33.5
longitude=-7.6
date=2026-05-16 20:30:00
imei=device_id
```

`showPositions.php` returns:

```json
{
  "positions": []
}
```

## Android Networking Note

In `MainActivity.java` and `MapsActivity.java`, replace `192.168.43.228` with the real IPv4 address of the PC running Apache.

Do not use `localhost` in Android code. On a phone or emulator, `localhost` means the Android device itself, not the PC.

The phone and the PC must be connected to the same Wi-Fi network or the same hotspot.
