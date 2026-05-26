<?php
// Returns JSON responses to the Android Volley client.
header("Content-Type: application/json; charset=utf-8");

// Loads the Position model class.
require_once __DIR__ . "/classe/Position.php";
// Loads the service that inserts positions into MySQL.
require_once __DIR__ . "/service/PositionService.php";

// Stores the client IP address for debugging local network requests.
$clientIp = isset($_SERVER["REMOTE_ADDR"]) ? $_SERVER["REMOTE_ADDR"] : "unknown";

// Allows only POST requests for creating a position.
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    // Sends HTTP 405 when the method is not POST.
    http_response_code(405);
    // Returns an error JSON object to the client.
    echo json_encode(array("success" => false, "message" => "Method not allowed", "client_ip" => $clientIp));
    // Stops script execution after the error response.
    exit;
}

// Checks that all required POST parameters were sent by Android.
if (!isset($_POST["latitude"], $_POST["longitude"], $_POST["date"], $_POST["imei"])) {
    // Sends HTTP 400 when a required parameter is missing.
    http_response_code(400);
    // Returns a JSON error with the client IP for debugging.
    echo json_encode(array("success" => false, "message" => "Missing parameters", "client_ip" => $clientIp));
    // Stops script execution after the error response.
    exit;
}

// Reads latitude from POST data.
$latitude = $_POST["latitude"];
// Reads longitude from POST data.
$longitude = $_POST["longitude"];
// Reads datetime from POST data.
$date = $_POST["date"];
// Reads the Android device identifier from POST data.
$imei = $_POST["imei"];

try {
    // Creates a Position object without an id because MySQL generates it automatically.
    $position = new Position(null, $latitude, $longitude, $date, $imei);
    // Creates the service used to insert the position.
    $service = new PositionService();
    // Executes the database INSERT operation.
    $created = $service->create($position);

    // Checks whether the INSERT operation succeeded.
    if ($created) {
        // Returns a success JSON response to Android.
        echo json_encode(array("success" => true, "message" => "Position inserted", "client_ip" => $clientIp));
    } else {
        // Sends HTTP 500 when PDO reports an insertion failure.
        http_response_code(500);
        // Returns an insertion failure JSON response.
        echo json_encode(array("success" => false, "message" => "Insertion failed", "client_ip" => $clientIp));
    }
} catch (Exception $exception) {
    // Sends HTTP 500 when an exception occurs.
    http_response_code(500);
    // Returns the exception message for lab debugging.
    echo json_encode(array("success" => false, "message" => $exception->getMessage(), "client_ip" => $clientIp));
}
?>
