param(
    [string]$ProjectRoot = ".",
    [string]$OutputFile = "project_dump.txt"
)

$ErrorActionPreference = "Stop"

# ----------------------------------------------------------------
# Resolve paths up front so we can reliably compare/exclude files
# ----------------------------------------------------------------
$resolvedRoot = (Resolve-Path $ProjectRoot).Path

if (-not [System.IO.Path]::IsPathRooted($OutputFile)) {
    $resolvedOutput = Join-Path $resolvedRoot $OutputFile
} else {
    $resolvedOutput = $OutputFile
}
$resolvedOutput = [System.IO.Path]::GetFullPath($resolvedOutput)

if (Test-Path $resolvedOutput) {
    Remove-Item $resolvedOutput -Force
}

$extensions = @(
    ".java", ".kt", ".xml", ".properties", ".yml", ".yaml",
    ".json", ".sql", ".md", ".txt"
)

# Folder NAMES to exclude (matched as whole path segments, not substrings)
$excludeDirNames = @(
    "target", "build", "out", ".git", ".idea", "node_modules", "logs"
)

# Special-case: only ".mvn\wrapper", not the whole ".mvn" folder
function Test-ExcludedPath {
    param([string]$FullPath)

    $segments = $FullPath.Split([System.IO.Path]::DirectorySeparatorChar, [System.IO.Path]::AltDirectorySeparatorChar)

    for ($i = 0; $i -lt $segments.Length; $i++) {
        if ($excludeDirNames -contains $segments[$i]) {
            return $true
        }
        if ($segments[$i] -eq ".mvn" -and $i + 1 -lt $segments.Length -and $segments[$i + 1] -eq "wrapper") {
            return $true
        }
    }
    return $false
}

# ----------------------------------------------------------------
# Single writer for the whole run: plain UTF-8, no BOM.
# This is the most broadly compatible text encoding (Claude, most
# editors/tools, diff, grep, etc. all handle it cleanly), and using
# one open handle for the entire run avoids the file-lock error.
# ----------------------------------------------------------------
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$writer = New-Object System.IO.StreamWriter($resolvedOutput, $false, $utf8NoBom)

function Write-Line([string]$text = "") {
    $writer.WriteLine($text)
}

# ----------------------------------------------------------------
# Native tree renderer (replaces external `tree` command, which
# outputs OEM/CP437 on Windows and caused the garbled characters)
# ----------------------------------------------------------------
function Write-Tree {
    param(
        [string]$Path,
        [string]$Prefix = ""
    )

    $items = Get-ChildItem -LiteralPath $Path | Where-Object {
        -not (Test-ExcludedPath $_.FullName)
    } | Sort-Object @{Expression = { -not $_.PSIsContainer } }, Name

    $count = $items.Count
    for ($i = 0; $i -lt $count; $i++) {
        $item = $items[$i]
        $isLast = ($i -eq $count - 1)
        $connector = if ($isLast) { "+-- " } else { "|-- " }

        Write-Line "$Prefix$connector$($item.Name)"

        if ($item.PSIsContainer) {
            $childPrefix = if ($isLast) { "$Prefix    " } else { "$Prefix|   " }
            Write-Tree -Path $item.FullName -Prefix $childPrefix
        }
    }
}

try {
    Write-Line "=========================================================="
    Write-Line "PROJECT DUMP"
    Write-Line "Generated : $(Get-Date)"
    Write-Line "Root      : $resolvedRoot"
    Write-Line "=========================================================="
    Write-Line ""

    ##########################################################
    # PROJECT TREE
    ##########################################################
    Write-Line "################ PROJECT TREE ################"
    Write-Line ""
    Write-Line (Split-Path $resolvedRoot -Leaf)
    Write-Tree -Path $resolvedRoot
    Write-Line ""
    Write-Line "##########################################################"
    Write-Line ""

    ##########################################################
    # MODULES
    ##########################################################
    Write-Line "Modules:"
    Write-Line ""

    Get-ChildItem $resolvedRoot -Directory | Where-Object {
        -not (Test-ExcludedPath $_.FullName)
    } | ForEach-Object {
        Write-Line " - $($_.Name)"
    }

    Write-Line ""
    Write-Line "##########################################################"
    Write-Line ""

    ##########################################################
    # FILES
    ##########################################################
    $total = 0

    Get-ChildItem $resolvedRoot -Recurse -File | ForEach-Object {

        $full = $_.FullName

        # Never read the file we are currently writing to
        if ($full -eq $resolvedOutput) { return }

        if (Test-ExcludedPath $full) { return }

        $include = $false

        if ($_.Name -eq "pom.xml") {
            $include = $true
        } elseif ($extensions -contains $_.Extension.ToLower()) {
            $include = $true
        }

        if (-not $include) { return }

        $total++

        Write-Line ""
        Write-Line "=========================================================="
        Write-Line "FILE : $full"
        Write-Line "=========================================================="
        Write-Line ""

        try {
            $content = [System.IO.File]::ReadAllText($full)
            $writer.Write($content)
            if (-not $content.EndsWith("`n")) { $writer.WriteLine() }
        }
        catch {
            Write-Line "<<Unable to read file: $($_.Exception.Message)>>"
        }

        Write-Line ""
    }

    Write-Line ""
    Write-Line "=========================================================="
    Write-Line "TOTAL FILES : $total"
    Write-Line "=========================================================="
}
finally {
    $writer.Flush()
    $writer.Close()
}

Write-Host ""
Write-Host "Done."
Write-Host "Files exported : $total"
Write-Host "Output : $resolvedOutput"