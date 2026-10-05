# Simulador de Filas

**Disciplina:** Simulação e Métodos Analíticos  
**Atividade:** T1 — Rede de Filas

## 👥 Integrantes do Grupo

| Nome | 
|------|
| Júlia Tietbohl   |
| Filipe Pereira   |
| Nicholas Navarro |
| Maria Rodrigues  |

## Como Rodar

Compile:

```powershell
javac .\src\*.java
```

Execute informando o arquivo YAML:

```powershell
java -cp .\src Simulador model.yml
```

O arquivo `.yml` deve conter o modelo da simulacao e os numeros aleatorios, usando `seeds` com `rndnumbersPerSeed` ou usando `rndnumbers`.
