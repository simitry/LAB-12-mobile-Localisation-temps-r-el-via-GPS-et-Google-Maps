<?php
// Loads the DAO interface contract.
require_once __DIR__ . "/../dao/IDao.php";
// Loads the Position model class.
require_once __DIR__ . "/../classe/Position.php";
// Loads the PDO connection helper.
require_once __DIR__ . "/../connexion/Connexion.php";

// PositionService implements database operations for the position table.
class PositionService implements IDao {
    // Stores the PDO connection used by this service.
    private $connexion;

    // Initializes the service with a PDO connection.
    public function __construct() {
        // Gets the shared PDO connection from Connexion.
        $this->connexion = Connexion::getConnexion();
    }

    // Inserts a new Position into the database.
    public function create($position) {
        // Prepares an INSERT statement to avoid SQL injection.
        $query = "INSERT INTO position (latitude, longitude, date, imei) VALUES (:latitude, :longitude, :date, :imei)";
        // Creates the prepared statement object.
        $statement = $this->connexion->prepare($query);
        // Binds the latitude value from the Position object.
        $statement->bindValue(":latitude", $position->getLatitude());
        // Binds the longitude value from the Position object.
        $statement->bindValue(":longitude", $position->getLongitude());
        // Binds the datetime value from the Position object.
        $statement->bindValue(":date", $position->getDate());
        // Binds the device identifier value from the Position object.
        $statement->bindValue(":imei", $position->getImei());
        // Executes the INSERT query and returns true or false.
        return $statement->execute();
    }

    // Updates an existing Position in the database.
    public function update($position) {
        // Prepares an UPDATE statement for the matching id.
        $query = "UPDATE position SET latitude = :latitude, longitude = :longitude, date = :date, imei = :imei WHERE id = :id";
        // Creates the prepared statement object.
        $statement = $this->connexion->prepare($query);
        // Binds the id used in the WHERE clause.
        $statement->bindValue(":id", $position->getId());
        // Binds the updated latitude.
        $statement->bindValue(":latitude", $position->getLatitude());
        // Binds the updated longitude.
        $statement->bindValue(":longitude", $position->getLongitude());
        // Binds the updated datetime.
        $statement->bindValue(":date", $position->getDate());
        // Binds the updated device identifier.
        $statement->bindValue(":imei", $position->getImei());
        // Executes the UPDATE query and returns true or false.
        return $statement->execute();
    }

    // Deletes a Position from the database by id.
    public function delete($position) {
        // Prepares a DELETE statement for the matching id.
        $query = "DELETE FROM position WHERE id = :id";
        // Creates the prepared statement object.
        $statement = $this->connexion->prepare($query);
        // Binds the id of the position to delete.
        $statement->bindValue(":id", $position->getId());
        // Executes the DELETE query and returns true or false.
        return $statement->execute();
    }

    // Returns one position row by id.
    public function getById($id) {
        // Prepares a SELECT query for one id.
        $query = "SELECT * FROM position WHERE id = :id";
        // Creates the prepared statement object.
        $statement = $this->connexion->prepare($query);
        // Binds the requested id.
        $statement->bindValue(":id", $id);
        // Executes the SELECT query.
        $statement->execute();
        // Returns one associative array or false when not found.
        return $statement->fetch(PDO::FETCH_ASSOC);
    }

    // Returns all saved positions from the database.
    public function getAll() {
        // Prepares a SELECT query for every position row.
        $query = "SELECT * FROM position";
        // Creates the prepared statement object.
        $statement = $this->connexion->prepare($query);
        // Executes the SELECT query.
        $statement->execute();
        // Returns all rows as associative arrays.
        return $statement->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>
