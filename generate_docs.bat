call npm install -g docco
SET DIR=%~dp0
FOR %%a IN (%DIR%app\controllers\*.java) DO docco --output %DIR%public\docs %%a