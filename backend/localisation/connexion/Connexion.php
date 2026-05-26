<?php
// Connexion centralizes the PDO connection used by the backend services.
class Connexion {
    // Stores the shared PDO instance so every service can reuse one connection.
    private static $connexion = null;

    // Returns a PDO connection to the localisation database.
    public static function getConnexion() {
        // Creates the PDO object only the first time this method is called.
        if (self::$connexion === null) {
            // Defines the MySQL server host used by XAMPP or a local PHP server.
            $host = "localhost";
            // Defines the database name required by the lab.
            $dbname = "localisation";
            // Defines the default MySQL login used by XAMPP.
            $login = "root";
            // Defines the default empty MySQL password used by XAMPP.
            $password = "";
            // Builds a UTF-8 PDO DSN for the localisation database.
            $dsn = "mysql:host=$host;dbname=$dbname;charset=utf8mb4";
            // Opens the PDO connection to MySQL.
            self::$connexion = new PDO($dsn, $login, $password);
            // Enables exceptions so database errors are easy to detect.
            self::$connexion->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            // Returns SELECT rows as associative arrays by default.
            self::$connexion->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
        }

        // Returns the ready PDO connection.
        return self::$connexion;
    }
}
?>
