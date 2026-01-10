import subprocess
import os
import glob

# ================= CONFIGURAÇÕES =================

# Nome completo da classe (pacote + nome)
MAIN_CLASS = "org.fog.test.perfeval.testes.cluster.GeradorTopologiaCluster"

# Configuração automática de pastas
# O script assume que está na raiz do projeto
BASE_DIR = os.getcwd()

# Tenta localizar a pasta de compilados (bin no Eclipse/VSCode puro, target/classes no Maven)
if os.path.exists(os.path.join(BASE_DIR, "bin")):
    PASTA_BINARIOS = os.path.join(BASE_DIR, "bin")
elif os.path.exists(os.path.join(BASE_DIR, "target", "classes")):
    PASTA_BINARIOS = os.path.join(BASE_DIR, "target", "classes")
else:
    print("ERRO: Não encontrei a pasta 'bin' ou 'target/classes'.")
    print("Certifique-se de que o projeto Java foi compilado na IDE.")
    exit(1)

# Localiza todos os JARs na pasta lib (se existir)
PASTA_LIB = os.path.join(BASE_DIR, "lib")
jars = glob.glob(os.path.join(PASTA_LIB, "*.jar"))

# Monta o Classpath (Binários + Jars)
separador = ";" if os.name == 'nt' else ":"
classpath_list = [PASTA_BINARIOS] + jars
classpath = separador.join(classpath_list)

# ================= EXECUÇÃO =================

def rodar_simulacoes(quantidade):
    print(f"--- Configuração ---")
    print(f"Diretório Base: {BASE_DIR}")
    print(f"Pasta Binários: {PASTA_BINARIOS}")
    print(f"Jars encontrados: {len(jars)}")
    print(f"--------------------")

    for i in range(1, quantidade + 1):
        print(f"Rodando simulação {i} de {quantidade}...", end=" ")

        comando = ["java", "-cp", classpath, MAIN_CLASS]
        
        # Executa o Java
        try:
            resultado = subprocess.run(comando, capture_output=True, text=True)
            
            if resultado.returncode == 0:
                print("OK! (Salvo no CSV)")
            else:
                print("FALHA!")
                print("--- Erro Java ---")
                print(resultado.stderr)
                # Se der erro de classe não encontrada, pare para não spamar
                if "Could not find or load main class" in resultado.stderr:
                    break
        except Exception as e:
            print(f"Erro ao chamar o Java: {e}")
            break

    print("\nProcesso finalizado.")

if __name__ == "__main__":
    rodar_simulacoes(30)