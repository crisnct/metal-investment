param(
    [string]$BuildDir = "build",
    [string]$StaticDir = "src\main\resources\static"
)

function Format-IndexHtml {
    param([string]$FilePath)

    if (-not (Test-Path $FilePath)) {
        return
    }

    $raw = Get-Content -Raw -Path $FilePath
    if ([string]::IsNullOrWhiteSpace($raw)) {
        return
    }

    $withBreaks = $raw -replace '><', ">" + [Environment]::NewLine + "<"
    $lines = $withBreaks -split [Environment]::NewLine

    $indent = 0
    $formatted = foreach ($line in $lines) {
        $trimmed = $line.Trim()
        if ($trimmed.Length -eq 0) {
            continue
        }

        $isClosing = $trimmed -match '^</'
        $isSelfClosing = $trimmed -match '/>$'
        $isDoctype = $trimmed -match '^<!'
        $isSingleLine = $trimmed -match '^<[^>]+>.*</[^>]+>$'

        if ($isClosing -and !$isSingleLine) {
            $indent = [Math]::Max($indent - 1, 0)
        }

        $indentation = "  " * $indent
        $formattedLine = "$indentation$trimmed"

        if (-not $isClosing -and -not $isSelfClosing -and -not $isDoctype -and -not $isSingleLine) {
            $indent += 1
        }

        $formattedLine
    }

    [System.IO.File]::WriteAllLines($FilePath, $formatted, [System.Text.Encoding]::UTF8)
}

function Copy-BuildToStatic {
    param([string]$Source, [string]$Destination)

    if (-not (Test-Path $Source)) {
        Write-Error "Build directory '$Source' not found. Run 'npm run build' first."
        exit 1
    }

    if (Test-Path $Destination) {
        Remove-Item -Path (Join-Path $Destination '*') -Recurse -Force -ErrorAction Stop
    } else {
        New-Item -ItemType Directory -Path $Destination | Out-Null
    }

    Copy-Item -Path (Join-Path $Source '*') -Destination $Destination -Recurse -Force
}

Copy-BuildToStatic -Source $BuildDir -Destination $StaticDir
Format-IndexHtml -FilePath (Join-Path $StaticDir 'index.html')
