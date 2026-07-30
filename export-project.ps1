param(
    [string]$ProjectRoot = ".",
    [string]$OutputFile = "project_dump.txt"
)

if (Test-Path $OutputFile) {
    Remove-Item $OutputFile
}

$extensions = @(
    "*.java",
    "*.kt",
    "*.xml",
    "*.properties",
    "*.yml",
    "*.yaml",
    "*.json",
    "*.sql",
    "*.md",
    "*.txt"
)

$excludeDirs = @(
    "\target\",
    "\build\",
    "\out\",
    "\.git\",
    "\.idea\",
    "\node_modules\",
    "\.mvn\wrapper\",
    "\logs\"
)

$total = 0

function Write-Line($text){
    Add-Content $OutputFile $text
}

Write-Line "=========================================================="
Write-Line "PROJECT DUMP"
Write-Line "Generated : $(Get-Date)"
Write-Line "Root : $(Resolve-Path $ProjectRoot)"
Write-Line "=========================================================="
Write-Line ""

##########################################################
# PROJECT TREE
##########################################################

Write-Line "################ PROJECT TREE ################"
Write-Line ""

tree $ProjectRoot /F | Out-File tree.tmp

Get-Content tree.tmp | Add-Content $OutputFile

Remove-Item tree.tmp

Write-Line ""
Write-Line "##########################################################"
Write-Line ""

##########################################################
# MODULES
##########################################################

Write-Line "Modules:"
Write-Line ""

Get-ChildItem $ProjectRoot -Directory | ForEach-Object{
    Write-Line " - $($_.Name)"
}

Write-Line ""
Write-Line "##########################################################"
Write-Line ""

##########################################################
# FILES
##########################################################

Get-ChildItem $ProjectRoot -Recurse -File | ForEach-Object {

    $full = $_.FullName

    $skip = $false

    foreach($dir in $excludeDirs){
        if($full.Contains($dir)){
            $skip = $true
            break
        }
    }

    if($skip){
        return
    }

    $include = $false

    if($_.Name -eq "pom.xml"){
        $include = $true
    }

    foreach($ext in $extensions){
        if($_.Name -like $ext){
            $include = $true
            break
        }
    }

    if(!$include){
        return
    }

    $total++

    Write-Line ""
    Write-Line "=========================================================="
    Write-Line "FILE : $full"
    Write-Line "=========================================================="
    Write-Line ""

    try{
        Get-Content $full | Add-Content $OutputFile
    }
    catch{
        Write-Line "<<Unable to read file>>"
    }

    Write-Line ""
}

Write-Line ""
Write-Line "=========================================================="
Write-Line "TOTAL FILES : $total"
Write-Line "=========================================================="

Write-Host ""
Write-Host "Done."
Write-Host "Files exported : $total"
Write-Host "Output : $OutputFile"
