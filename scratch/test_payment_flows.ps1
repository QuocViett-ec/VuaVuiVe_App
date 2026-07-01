$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$BaseUrl = "http://localhost:3000"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token,
        $Body
    )

    $headers = @{}
    if ($Token) {
        $headers.Authorization = "Bearer $Token"
    }

    $params = @{
        Method = $Method
        Uri = "$BaseUrl$Path"
        Headers = $headers
    }

    if ($null -ne $Body) {
        $params.ContentType = "application/json"
        $params.Body = ($Body | ConvertTo-Json -Depth 20 -Compress)
    }

    try {
        Invoke-RestMethod @params
    } catch {
        $resp = $_.Exception.Response
        if ($resp) {
            $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
            $text = $reader.ReadToEnd()
            throw "HTTP $($resp.StatusCode.value__) $Method $Path`n$text"
        }
        throw
    }
}

function Add-Check {
    param(
        [System.Collections.Generic.List[object]]$Checks,
        [string]$Label,
        [bool]$Pass,
        [string]$Details
    )
    $Checks.Add([pscustomobject]@{
        label = $Label
        pass = $Pass
        details = $Details
    }) | Out-Null
}

function Expect {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) {
        throw $Message
    }
}

function Get-OrderId($obj) {
    if ($obj.orderId) { return $obj.orderId }
    if ($obj.id) { return $obj.id }
    if ($obj.data) { return Get-OrderId $obj.data }
    throw "orderId not found"
}

function Get-StatusValue($obj) {
    if ($obj.status) { return [string]$obj.status }
    if ($obj.data) { return Get-StatusValue $obj.data }
    return $null
}

function Get-PaymentStatusValue($obj) {
    if ($obj.paymentStatus) { return [string]$obj.paymentStatus }
    if ($obj.payment_status) { return [string]$obj.payment_status }
    if ($obj.data) { return Get-PaymentStatusValue $obj.data }
    return $null
}

function Get-PaymentUrlValue($obj) {
    if ($obj.paymentUrl) { return [string]$obj.paymentUrl }
    if ($obj.payment_url) { return [string]$obj.payment_url }
    if ($obj.data) { return Get-PaymentUrlValue $obj.data }
    return $null
}

function Get-CustomerPoints($token) {
    $me = Invoke-Api -Method GET -Path "/api/auth/me" -Token $token -Body $null
    [int]$me.data.points
}

function Get-Product($token, $productId) {
    (Invoke-Api -Method GET -Path "/api/products/$productId" -Token $token -Body $null).data
}

function New-OrderBody($productId, $method, $name, $phone, $address, $note) {
    @{
        items = @(
            @{
                productId = $productId
                quantity = 1
            }
        )
        delivery = @{
            name = $name
            phone = $phone
            address = $address
        }
        payment = @{
            method = $method
        }
        note = $note
    }
}

function Get-ShipperOrdersList($response) {
    $list = @()
    if ($response.content) { $list += @($response.content) }
    if ($response.data) { $list += @($response.data) }
    if ($response.items) { $list += @($response.items) }
    return $list
}

function Run-CodFlow {
    param($customerToken, $adminToken, $shipperToken, $productId, $shipperId, $checks)

    $pointsBefore = Get-CustomerPoints $customerToken

    $create = Invoke-Api -Method POST -Path "/api/orders" -Token $customerToken -Body (New-OrderBody $productId "COD" "Nguyen Van A" "0901234567" "123 Test St" "Test COD")
    $orderId = Get-OrderId $create
    Add-Check $checks "Create order: PENDING_APPROVAL + UNPAID" (($create.data.status -eq "PENDING_APPROVAL") -and ((Get-PaymentStatusValue $create) -eq "UNPAID") -and [string]::IsNullOrWhiteSpace((Get-PaymentUrlValue $create))) "status=$($create.data.status), paymentStatus=$(Get-PaymentStatusValue $create)"

    $confirmed = Invoke-Api -Method PATCH -Path "/api/orders/$orderId/status" -Token $adminToken -Body @{ status = "confirmed"; updatedBy = "Admin Test" }
    Add-Check $checks "Admin confirm: CONFIRMED" ((Get-StatusValue $confirmed) -eq "CONFIRMED") "status=$(Get-StatusValue $confirmed)"

    $assigned = Invoke-Api -Method PATCH -Path "/api/admin/orders/$orderId/assign-shipper" -Token $adminToken -Body @{ shipperId = $shipperId }
    $assignOk = ($assigned.data.shipperId -eq $shipperId) -and (-not [string]::IsNullOrWhiteSpace([string]$assigned.data.shipperName))
    Add-Check $checks "Assign shipper: shipperId + shipperName" $assignOk "shipperId=$($assigned.data.shipperId), shipperName=$($assigned.data.shipperName)"

    $shipperOrders = Invoke-Api -Method GET -Path "/api/orders/shipper" -Token $shipperToken -Body $null
    $found = @(Get-ShipperOrdersList $shipperOrders | Where-Object { $_ -and (Get-OrderId $_) -eq $orderId })
    Add-Check $checks "Shipper sees order" ($found.Count -gt 0) "orderId=$orderId"

    $inTransit = Invoke-Api -Method PATCH -Path "/api/orders/$orderId/status" -Token $adminToken -Body @{ status = "in_transit"; updatedBy = "Shipper Test" }
    Add-Check $checks "IN_TRANSIT works" ((Get-StatusValue $inTransit) -eq "IN_TRANSIT") "status=$(Get-StatusValue $inTransit)"

    $delivered = Invoke-Api -Method PATCH -Path "/api/orders/$orderId/status" -Token $adminToken -Body @{ status = "delivered"; updatedBy = "Shipper Test" }
    $pointsAfter = Get-CustomerPoints $customerToken
    $deliveredOk = ((Get-StatusValue $delivered) -eq "DELIVERED") -and ((Get-PaymentStatusValue $delivered) -eq "PAID") -and ($pointsAfter -gt $pointsBefore)
    Add-Check $checks "DELIVERED: PAID + points added" $deliveredOk "status=$(Get-StatusValue $delivered), paymentStatus=$(Get-PaymentStatusValue $delivered), pointsBefore=$pointsBefore, pointsAfter=$pointsAfter"
}

function Run-OnlineFlow {
    param($method, $prefix, $customerToken, $adminToken, $shipperToken, $productId, $shipperId, $checks)

    $pointsBefore = Get-CustomerPoints $customerToken
    $stockBefore = [int](Get-Product $customerToken $productId).stockQuantity

    $create = Invoke-Api -Method POST -Path "/api/orders" -Token $customerToken -Body (New-OrderBody $productId $method "Nguyen Van B" "0901234568" "456 Test St" "Test $method")
    $orderId = Get-OrderId $create
    $createOk = ($create.data.status -eq "PENDING_PAYMENT") -and ((Get-PaymentStatusValue $create) -eq "PENDING") -and (-not [string]::IsNullOrWhiteSpace((Get-PaymentUrlValue $create)))
    Add-Check $checks "Create order: PENDING_PAYMENT + PENDING + paymentUrl" $createOk "status=$($create.data.status), paymentStatus=$(Get-PaymentStatusValue $create), paymentUrl=$(Get-PaymentUrlValue $create)"

    $success = Invoke-Api -Method POST -Path "/api/payments/$prefix/mock-success/$orderId" -Token $adminToken -Body $null
    $successOk = ((Get-StatusValue $success) -eq "CONFIRMED") -and ((Get-PaymentStatusValue $success) -eq "PAID")
    Add-Check $checks "mock-success: CONFIRMED + PAID" $successOk "status=$(Get-StatusValue $success), paymentStatus=$(Get-PaymentStatusValue $success)"

    $assigned = Invoke-Api -Method PATCH -Path "/api/admin/orders/$orderId/assign-shipper" -Token $adminToken -Body @{ shipperId = $shipperId }
    $assignOk = ($assigned.data.shipperId -eq $shipperId) -and (-not [string]::IsNullOrWhiteSpace([string]$assigned.data.shipperName))
    Add-Check $checks "Assign shipper" $assignOk "shipperId=$($assigned.data.shipperId), shipperName=$($assigned.data.shipperName)"

    $inTransit = Invoke-Api -Method PATCH -Path "/api/orders/$orderId/status" -Token $adminToken -Body @{ status = "in_transit"; updatedBy = "Shipper Test" }
    Expect ((Get-StatusValue $inTransit) -eq "IN_TRANSIT") "$method failed to move into IN_TRANSIT"

    $delivered = Invoke-Api -Method PATCH -Path "/api/orders/$orderId/status" -Token $adminToken -Body @{ status = "delivered"; updatedBy = "Shipper Test" }
    $pointsAfter = Get-CustomerPoints $customerToken
    $stockAfterSuccess = [int](Get-Product $customerToken $productId).stockQuantity
    $deliveredOk = ((Get-StatusValue $delivered) -eq "DELIVERED") -and ((Get-PaymentStatusValue $delivered) -eq "PAID") -and ($pointsAfter -gt $pointsBefore)
    Add-Check $checks "IN_TRANSIT -> DELIVERED" $deliveredOk "status=$(Get-StatusValue $delivered), paymentStatus=$(Get-PaymentStatusValue $delivered), pointsBefore=$pointsBefore, pointsAfter=$pointsAfter, stockBefore=$stockBefore, stockAfterSuccess=$stockAfterSuccess"

    $failCreate = Invoke-Api -Method POST -Path "/api/orders" -Token $customerToken -Body (New-OrderBody $productId $method "Nguyen Van C" "0901234569" "789 Test St" "Test $method fail")
    $failOrderId = Get-OrderId $failCreate
    $fail = Invoke-Api -Method POST -Path "/api/payments/$prefix/mock-fail/$failOrderId" -Token $adminToken -Body $null
    $stockAfterFail = [int](Get-Product $customerToken $productId).stockQuantity
    $failOk = ((Get-StatusValue $fail) -eq "CANCELLED") -and ($stockAfterFail -eq $stockAfterSuccess)
    Add-Check $checks "mock-fail: CANCELLED + stock restored" $failOk "status=$(Get-StatusValue $fail), stockAfterSuccess=$stockAfterSuccess, stockAfterFail=$stockAfterFail"
}

$customerLogin = Invoke-Api -Method POST -Path "/api/auth/login" -Token $null -Body @{ identifier = "customer@gmail.com"; password = "Customer@123" }
$adminLogin = Invoke-Api -Method POST -Path "/api/auth/admin/login" -Token $null -Body @{ identifier = "admin@vuavuive.vn"; password = "Admin@123" }
$shipperLogin = Invoke-Api -Method POST -Path "/api/auth/shipper/login" -Token $null -Body @{ identifier = "shipper@gmail.com"; password = "Shipper@123" }

$customerToken = $customerLogin.accessToken
$adminToken = $adminLogin.accessToken
$shipperToken = $shipperLogin.accessToken

$products = (Invoke-Api -Method GET -Path '/api/products?page=1&limit=20' -Token $customerToken -Body $null).data
$product = $products | Where-Object { $_.isActive -eq $true -and [decimal]$_.sellingPrice -ge 10000 -and [int]$_.stockQuantity -ge 5 } | Select-Object -First 1
if (-not $product) {
    throw "No valid test product found"
}

$shippers = (Invoke-Api -Method GET -Path "/api/admin/shippers" -Token $adminToken -Body $null).data
$shipper = $shippers | Where-Object { $_.phone -eq "0987654321" } | Select-Object -First 1
if (-not $shipper) {
    throw "Test shipper 0987654321 not found"
}

$result = [ordered]@{
    timestamp = (Get-Date).ToString("s")
    baseUrl = $BaseUrl
    productId = $product.id
    shipperId = $shipper.id
    COD = New-Object 'System.Collections.Generic.List[object]'
    MOMO = New-Object 'System.Collections.Generic.List[object]'
    ZALOPAY = New-Object 'System.Collections.Generic.List[object]'
}

Run-CodFlow -customerToken $customerToken -adminToken $adminToken -shipperToken $shipperToken -productId $product.id -shipperId $shipper.id -checks $result.COD
Run-OnlineFlow -method "MOMO" -prefix "momo" -customerToken $customerToken -adminToken $adminToken -shipperToken $shipperToken -productId $product.id -shipperId $shipper.id -checks $result.MOMO
Run-OnlineFlow -method "ZALOPAY" -prefix "zalopay" -customerToken $customerToken -adminToken $adminToken -shipperToken $shipperToken -productId $product.id -shipperId $shipper.id -checks $result.ZALOPAY

$result | ConvertTo-Json -Depth 10
