param(
    [string]$ProjectRoot = ".",
    [string]$OutputFile = "project_dump.txt",
    [string[]]$Modules = @()
)

$ErrorActionPreference = "Stop"

# ================================================================
# RESOLVE ROOT / OUTPUT
# ================================================================

$resolvedRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path

if ([System.IO.Path]::IsPathRooted($OutputFile)) {
    $resolvedOutput = [System.IO.Path]::GetFullPath($OutputFile)
}
else {
    $resolvedOutput = [System.IO.Path]::GetFullPath(
            (Join-Path $resolvedRoot $OutputFile)
    )
}

# ================================================================
# CONFIGURATION
# ================================================================

$extensions = @(
    ".java",
    ".kt",
    ".xml",
    ".properties",
    ".yml",
    ".yaml",
    ".json",
    ".sql",
    ".md",
    ".txt"
)

$excludeDirNames = @(
    "target",
    "build",
    "out",
    ".git",
    ".idea",
    "node_modules",
    "logs"
)

# ================================================================
# NORMALIZE MODULE ARGUMENT
# ================================================================

$Modules = @(
$Modules |
        ForEach-Object {
            if ($null -ne $_) {
                $_.ToString().Trim()
            }
        } |
        Where-Object {
            $_ -ne ""
        } |
        Select-Object -Unique
)

$moduleFilterEnabled = ($Modules.Count -gt 0)

# ================================================================
# FUNCTIONS
# ================================================================

function Test-ExcludedPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FullPath
    )

    foreach ($excludedName in $excludeDirNames) {

        $pattern = '[\\/]' + [regex]::Escape($excludedName) + '([\\/]|$)'

        if ($FullPath -match $pattern) {
            return $true
        }
    }

    # Exclude only .mvn\wrapper
    if ($FullPath -match '[\\/]\.mvn[\\/]wrapper([\\/]|$)') {
        return $true
    }

    return $false
}

function Test-IsIncludedModulePath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FullPath
    )

    if (-not $moduleFilterEnabled) {
        return $true
    }

    foreach ($module in $Modules) {

        $modulePath = [System.IO.Path]::GetFullPath(
                (Join-Path $resolvedRoot $module)
        )

        $modulePath = $modulePath.TrimEnd(
                [System.IO.Path]::DirectorySeparatorChar,
                [System.IO.Path]::AltDirectorySeparatorChar
        )

        $candidate = [System.IO.Path]::GetFullPath($FullPath)

        if (
        $candidate.Equals(
                $modulePath,
                [System.StringComparison]::OrdinalIgnoreCase
        )
        ) {
            return $true
        }

        $prefix = $modulePath +
                [System.IO.Path]::DirectorySeparatorChar

        if (
        $candidate.StartsWith(
                $prefix,
                [System.StringComparison]::OrdinalIgnoreCase
        )
        ) {
            return $true
        }
    }

    return $false
}

function Write-Tree {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [string]$Prefix = "",

        [bool]$RootLevel = $false
    )

    $items = @(
    Get-ChildItem -LiteralPath $Path -Force |
            Where-Object {

                if (Test-ExcludedPath $_.FullName) {
                    return $false
                }

                # When module filtering is active, at the project root
                # show only selected modules plus root files.
                if (
                $RootLevel -and
                        $_.PSIsContainer -and
                        $moduleFilterEnabled
                ) {
                    return ($Modules -contains $_.Name)
                }

                return $true
            } |
            Sort-Object @{
                Expression = { -not $_.PSIsContainer }
            }, Name
    )

    for ($i = 0; $i -lt $items.Count; $i++) {

        $item = $items[$i]

        $isLast = ($i -eq ($items.Count - 1))

        if ($isLast) {
            $connector = "+-- "
        }
        else {
            $connector = "|-- "
        }

        Write-DumpLine "$Prefix$connector$($item.Name)"

        if ($item.PSIsContainer) {

            if ($isLast) {
                $childPrefix = "$Prefix    "
            }
            else {
                $childPrefix = "$Prefix|   "
            }

            Write-Tree `
                -Path $item.FullName `
                -Prefix $childPrefix `
                -RootLevel:$false
        }
    }
}

function Write-DumpLine {
    param(
        [string]$Text = ""
    )

    $script:writer.WriteLine($Text)
}

# ================================================================
# VALIDATE MODULES
# ================================================================

if ($moduleFilterEnabled) {

    Write-Host ""
    Write-Host "Checking requested modules..." -ForegroundColor Cyan

    $availableModules = @(
    Get-ChildItem `
            -LiteralPath $resolvedRoot `
            -Directory |
            Where-Object {
                -not (Test-ExcludedPath $_.FullName)
            } |
            Select-Object -ExpandProperty Name
    )

    $missingModules = @(
    $Modules | Where-Object {
        $_ -notin $availableModules
    }
    )

    if ($missingModules.Count -gt 0) {

        Write-Host ""
        Write-Host "ERROR: Module(s) not found:" -ForegroundColor Red

        foreach ($module in $missingModules) {
            Write-Host "  $module" -ForegroundColor Red
        }

        Write-Host ""
        Write-Host "Available top-level directories:" -ForegroundColor Yellow

        foreach ($module in $availableModules) {
            Write-Host "  $module"
        }

        Write-Host ""

        exit 1
    }

    Write-Host "Selected modules:" -ForegroundColor Green

    foreach ($module in $Modules) {
        Write-Host "  $module"
    }
}
else {
    Write-Host ""
    Write-Host "Scope: ENTIRE PROJECT" -ForegroundColor Cyan
}

# ================================================================
# REMOVE EXISTING OUTPUT
# ================================================================

if (Test-Path -LiteralPath $resolvedOutput) {
    Remove-Item -LiteralPath $resolvedOutput -Force
}

# ================================================================
# CREATE WRITER
# ================================================================

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

$script:writer = New-Object System.IO.StreamWriter(
$resolvedOutput,
$false,
$utf8NoBom
)

$total = 0

try {

    # ============================================================
    # HEADER
    # ============================================================

    Write-DumpLine "=========================================================="
    Write-DumpLine "PROJECT DUMP"
    Write-DumpLine "Generated : $(Get-Date)"
    Write-DumpLine "Root      : $resolvedRoot"

    if ($moduleFilterEnabled) {
        Write-DumpLine "Scope     : SELECTED MODULES"
        Write-DumpLine "Modules   : $($Modules -join ', ')"
    }
    else {
        Write-DumpLine "Scope     : ENTIRE PROJECT"
    }

    Write-DumpLine "=========================================================="
    Write-DumpLine ""

    # ============================================================
    # PROJECT TREE
    # ============================================================

    Write-DumpLine "################ PROJECT TREE ################"
    Write-DumpLine ""

    $rootName = Split-Path -Leaf $resolvedRoot

    if ([string]::IsNullOrWhiteSpace($rootName)) {
        $rootName = $resolvedRoot
    }

    Write-DumpLine $rootName

    Write-Tree `
        -Path $resolvedRoot `
        -RootLevel:$true

    Write-DumpLine ""
    Write-DumpLine "##########################################################"
    Write-DumpLine ""

    # ============================================================
    # MODULES
    # ============================================================

    Write-DumpLine "Modules:"
    Write-DumpLine ""

    if ($moduleFilterEnabled) {

        foreach ($module in $Modules) {
            Write-DumpLine " - $module"
        }

    }
    else {

        Get-ChildItem `
            -LiteralPath $resolvedRoot `
            -Directory |
                Where-Object {
                    -not (Test-ExcludedPath $_.FullName)
                } |
                Sort-Object Name |
                ForEach-Object {
                    Write-DumpLine " - $($_.Name)"
                }
    }

    Write-DumpLine ""
    Write-DumpLine "##########################################################"
    Write-DumpLine ""

    # ============================================================
    # ROOT POM
    #
    # Always include parent pom.xml.
    # This is particularly important for module-only dumps.
    # ============================================================

    $rootPom = Join-Path $resolvedRoot "pom.xml"

    if (Test-Path -LiteralPath $rootPom -PathType Leaf) {

        Write-DumpLine ""
        Write-DumpLine "=========================================================="
        Write-DumpLine "FILE : $rootPom"
        Write-DumpLine "=========================================================="
        Write-DumpLine ""

        try {

            $content = [System.IO.File]::ReadAllText($rootPom)

            $script:writer.Write($content)

            if (-not $content.EndsWith("`n")) {
                $script:writer.WriteLine()
            }

            $total++

        }
        catch {

            Write-DumpLine "<<Unable to read file: $($_.Exception.Message)>>"
        }

        Write-DumpLine ""
    }

    # ============================================================
    # GET PROJECT FILES
    # ============================================================

    $files = @(
    Get-ChildItem `
            -LiteralPath $resolvedRoot `
            -Recurse `
            -File |
            Where-Object {

                # Never include output file
                if (
                $_.FullName.Equals(
                        $resolvedOutput,
                        [System.StringComparison]::OrdinalIgnoreCase
                )
                ) {
                    return $false
                }

                # Excluded directory
                if (Test-ExcludedPath $_.FullName) {
                    return $false
                }

                # Root pom already handled above
                if (
                $_.FullName.Equals(
                        $rootPom,
                        [System.StringComparison]::OrdinalIgnoreCase
                )
                ) {
                    return $false
                }

                # Module filtering
                if ($moduleFilterEnabled) {

                    if (-not (Test-IsIncludedModulePath $_.FullName)) {
                        return $false
                    }
                }

                # Include pom.xml regardless of extension
                if ($_.Name -eq "pom.xml") {
                    return $true
                }

                # Include configured extensions
                return (
                $extensions -contains $_.Extension.ToLowerInvariant()
                )
            } |
            Sort-Object FullName
    )

    # ============================================================
    # WRITE FILE CONTENTS
    # ============================================================

    foreach ($file in $files) {

        $total++

        Write-DumpLine ""
        Write-DumpLine "=========================================================="
        Write-DumpLine "FILE : $($file.FullName)"
        Write-DumpLine "=========================================================="
        Write-DumpLine ""

        try {

            $content = [System.IO.File]::ReadAllText(
                    $file.FullName
            )

            $script:writer.Write($content)

            if (-not $content.EndsWith("`n")) {
                $script:writer.WriteLine()
            }

        }
        catch {

            Write-DumpLine "<<Unable to read file: $($_.Exception.Message)>>"
        }

        Write-DumpLine ""
    }

    # ============================================================
    # FOOTER
    # ============================================================

    Write-DumpLine ""
    Write-DumpLine "=========================================================="
    Write-DumpLine "TOTAL FILES : $total"
    Write-DumpLine "=========================================================="

}
finally {

    if ($null -ne $script:writer) {
        $script:writer.Flush()
        $script:writer.Close()
        $script:writer.Dispose()
    }
}

# ================================================================
# RESULT
# ================================================================

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "DONE" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "Root   : $resolvedRoot"

if ($moduleFilterEnabled) {
    Write-Host "Modules: $($Modules -join ', ')"
}
else {
    Write-Host "Modules: ALL"
}

Write-Host "Files  : $total"
Write-Host "Output : $resolvedOutput"
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""