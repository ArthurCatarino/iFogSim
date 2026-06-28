# Configurações
$sensores = @(100,200,300)
$porcentagens = @("0.2", "0.4")
$execucoes = 1
$argFile = '@C:\Users\arthu\AppData\Local\Temp\cp_ap5hqkh8akcq0cyqyg9mo9ee4.argfile'
# Teste de Sanidade: Verifica se o arquivo do VS Code ainda existe
if (-not (Test-Path ($argFile.TrimStart('@')))) {
    Write-Host "ERRO: O arquivo .argfile expirou ou mudou! Rode a simulacao uma vez no VS Code e pegue o novo nome." -ForegroundColor Red
    exit
}

c:; cd 'c:\Users\arthu\iFogSim'

foreach ($n in $sensores) {
    foreach ($p in $porcentagens) {
        Write-Host "--- Cenário: $n sensores / $p nuvem ---" -ForegroundColor Yellow
        for ($i = 1; $i -le $execucoes; $i++) {
            Write-Host "Execução $i de 30..."
            
            # Executa o comando exato que você forneceu
            & 'C:\Program Files\Java\jdk-24\bin\java.exe' $argFile 'org.fog.test.perfeval.testes.Simulacao' $n $p
        }
    }
}