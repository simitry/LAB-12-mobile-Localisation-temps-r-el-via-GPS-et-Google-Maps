<?php
// IDao defines the CRUD contract used by service classes in the lab backend.
interface IDao {
    // Creates a new object in the database.
    public function create($obj);
    // Updates an existing object in the database.
    public function update($obj);
    // Deletes an existing object from the database.
    public function delete($obj);
    // Returns one object by id.
    public function getById($obj);
    // Returns all objects from the database.
    public function getAll();
}
?>
