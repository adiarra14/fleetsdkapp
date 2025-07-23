# PowerShell script to test TCP server connection and send test balise data

# TCP server details
$tcpServer = "localhost"
$tcpPort = 6060  # Port mapped for the TCP server (balise transmission port)

# Simulated balise data (JSON format expected by server)
$testMessage = '{"device_id":"TEST_BALISE_01","event_type":"GPS","latitude":12.3456,"longitude":45.6789,"battery_level":80}'

Write-Host "Testing connection to TCP server at $tcpServer`:$tcpPort..."

try {
    # Create TCP client
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    
    # Try to connect
    Write-Host "Connecting to TCP server..."
    $tcpClient.Connect($tcpServer, $tcpPort)
    
    if ($tcpClient.Connected) {
        Write-Host "Connected successfully!" -ForegroundColor Green
        
        # Get stream
        $stream = $tcpClient.GetStream()
        
        # Convert message to bytes
        $data = [System.Text.Encoding]::ASCII.GetBytes($testMessage)
        
        # Send data
        Write-Host "Sending test balise data: $testMessage"
        $stream.Write($data, 0, $data.Length)
        
        # Wait for response (optional)
        Start-Sleep -Seconds 1
        
        # Check if there's a response
        if ($stream.DataAvailable) {
            $responseBuffer = New-Object Byte[] 1024
            $bytesRead = $stream.Read($responseBuffer, 0, $responseBuffer.Length)
            $response = [System.Text.Encoding]::ASCII.GetString($responseBuffer, 0, $bytesRead)
            Write-Host "Received response: $response" -ForegroundColor Cyan
        } else {
            Write-Host "No response received (this may be normal)" -ForegroundColor Yellow
        }
        
        # Close connection
        $stream.Close()
        $tcpClient.Close()
        
        Write-Host "Test message sent successfully."
        Write-Host "Now check the database to see if the data was received and stored."
    } else {
        Write-Host "Failed to connect to TCP server!" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
} finally {
    if ($tcpClient -ne $null) {
        $tcpClient.Close()
    }
}

# Now check the database to see if the data was inserted
Write-Host "`nChecking database for test data..."
docker exec balise-postgres psql -U adminbdb -d balisedb -c "SELECT * FROM balises WHERE device_id = 'TEST_BALISE_01';"
docker exec balise-postgres psql -U adminbdb -d balisedb -c "SELECT * FROM balise_events WHERE device_id = 'TEST_BALISE_01';"
