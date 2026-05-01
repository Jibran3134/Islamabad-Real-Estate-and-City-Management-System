$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = "C:\Users\Jibran\.jdks\openjdk-26"
}

& "$PSScriptRoot\mvnw.cmd" clean package
