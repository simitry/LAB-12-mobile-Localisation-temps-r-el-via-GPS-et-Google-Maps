<?php
// Returns JSON responses to the Android Volley client.
header("Content-Type: application/json; charset=utf-8");

// Loads the service that reads saved positions from MySQL.
require_once __DIR__ . "/service/PositionService.php";

// Allows only POST requests for reading positions.
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    // Sends HTTP 405 when the method is not POST.
    http_response_code(405);
    // Returns an error JSON object to the client.
    echo json_encode(array("positions" => array(), "success" => false, "message" => "Method not allowed"));
    // Stops script execution after the error response.
    exit;
}

try {
    // Creates the service used to read positions.
    $service = new PositionService();
    // Loads all saved positions as associative arrays.
    $positions = $service->getAll();
    // Returns the JSON format expected by MapsActivity.
    echo json_encode(array("positions" => $positions));
} catch (Exception $exception) {
    // Sends HTTP 500 when an exception occurs.
    http_response_code(500);
    // Returns an empty positions array plus the error message for lab debugging.
    echo json_encode(array("positions" => array(), "success" => false, "message" => $exception->getMessage()));
}
?>
