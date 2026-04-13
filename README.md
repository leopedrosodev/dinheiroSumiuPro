# Dinheiro Sumiu Pro

App Android (Kotlin + Jetpack Compose + Room) para controle financeiro pessoal, com foco em uso offline.

Plano de evolução: [docs/PLANO_MELHORIAS.md](docs/PLANO_MELHORIAS.md)

## Status atual

- Balanço com comparativo de mês atual e mês passado.
- Descritivo no balanço com:
  - Total gasto
  - Salário
  - Despesa não essencial
  - Investimento
- Aba de gastos com criação, edição, remoção e mudança de status.
- Aba de pendências dedicada (pendências não ficam mais destacadas no balanço/relatório).
- Filtro por múltiplos meses (todos os meses com movimentação).
- Relatório mensal com:
  - receitas
  - gastos
  - saldo
  - gastos por categoria
  - top gastos
- Exportação de relatório em CSV e PDF.

## Regras de categoria (manual)

Os cálculos de descritivo usam categorias fixas (não dependem de palavra-chave).

- Receita:
  - Salário
  - Investimento
  - Outros
- Gasto:
  - Moradia
  - Contas fixas
  - Alimentação
  - Transporte
  - Saúde
  - Despesa não essencial
  - Investimento
  - Outros

## Stack

- Kotlin
- Jetpack Compose (Material 3)
- Room
- ViewModel + StateFlow
- KSP (Room compiler)

## Pacote do app

- `applicationId`: `com.dinheirosumiupro.app`
- `namespace`: `com.dinheirosumiupro.app`

## Como rodar

1. Abra no Android Studio.
2. Confira o SDK Android em `local.properties`:
   - Exemplo: `sdk.dir=/home/leonardoti03/Android/Sdk`
3. Build debug:

```bash
./gradlew assembleDebug
```

## APK para teste

`app/build/outputs/apk/debug/app-debug.apk`

Instalação via adb:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
