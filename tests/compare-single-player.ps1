param([string]$BaselineRef = '1dfe8bfb17cecffcb4ed8b99ded4179f63d26f41')

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $repoRoot
$report = Join-Path $repoRoot 'target/surefire-reports/TEST-rotp.multiplayer.SimulationBaselineTest.xml'
if (!(Test-Path -LiteralPath $report)) { throw 'Run Maven tests first to compile the probe and resolve dependencies.' }
$reportXml = [xml](Get-Content -Raw -LiteralPath $report)
$classpath = ($reportXml.testsuite.properties.property | Where-Object name -eq 'java.class.path').value
if (!$classpath) { throw 'No resolved test classpath in the Surefire report.' }
$baselineCommit = (& git rev-parse --verify "$BaselineRef^{commit}").Trim()
if ($LASTEXITCODE -ne 0) { throw 'Baseline revision is unavailable.' }
$runRoot = Join-Path $repoRoot ('target/single-player-comparison-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $runRoot | Out-Null
$sourceRoot = Join-Path $runRoot 'source'
$classesRoot = Join-Path $runRoot 'classes'
New-Item -ItemType Directory -Path $sourceRoot, $classesRoot | Out-Null

# Overlay every changed tracked Java class from the baseline. Unchanged classes
# and assets come from the Maven build; no working-tree or git-index mutation.
$changed = @(& git -c core.safecrlf=false diff --name-only $baselineCommit -- src | Where-Object { $_.EndsWith('.java') })
if ($LASTEXITCODE -ne 0 -or $changed.Count -eq 0) { throw 'Expected Java changes relative to the baseline.' }
$archive = Join-Path $runRoot 'baseline.zip'
& git archive --format=zip "--output=$archive" $baselineCommit -- @changed
if ($LASTEXITCODE -ne 0) { throw 'Could not export baseline sources.' }
Expand-Archive -LiteralPath $archive -DestinationPath $sourceRoot
$sources = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -Filter '*.java' | ForEach-Object FullName)
$argumentsFile = Join-Path $runRoot 'javac.args'
$arguments = @('--release', '17', '-encoding', 'UTF-8', '-classpath', ('"' + $classpath.Replace('\', '/') + '"'),
    '-d', ('"' + $classesRoot.Replace('\', '/') + '"'))
$arguments += $sources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }
[IO.File]::WriteAllLines($argumentsFile, $arguments, [Text.UTF8Encoding]::new($false))
& javac "@$argumentsFile" *> (Join-Path $runRoot 'compile.log')
if ($LASTEXITCODE -ne 0) { throw "Baseline compilation failed; see $runRoot/compile.log" }

foreach ($variant in @('baseline', 'current')) {
    $probeDir = Join-Path $runRoot $variant
    $probeClasspath = if ($variant -eq 'baseline') { $classesRoot + ';' + $classpath } else { $classpath }
    $logPath = Join-Path $runRoot ($variant + '.log')
    & java '-Xmx4g' '-cp' $probeClasspath 'rotp.multiplayer.SinglePlayerBaselineProbe' $probeDir *> $logPath
    if ($LASTEXITCODE -ne 0) { throw "$variant probe failed; see $logPath" }
    if ((Get-Content -Raw -LiteralPath $logPath) -match '(Exception|Error:)') {
        throw "$variant probe logged an error; see $logPath"
    }
}
$baselineState = Get-Content -Raw -LiteralPath (Join-Path $runRoot 'baseline/state.txt')
$currentState = Get-Content -Raw -LiteralPath (Join-Path $runRoot 'current/state.txt')
if ($baselineState -cne $currentState) { throw "Single-player gameplay differs. Compare the state.txt files under $runRoot" }
Write-Output "PASS: ordinary turns, research, colonization, automatic combat, diplomacy, military and Council victories, and subsequent RNG values match $baselineCommit"
Write-Output "Evidence: $runRoot"
