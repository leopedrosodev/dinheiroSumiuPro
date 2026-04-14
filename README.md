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
- Base recorrente local com edição, ativação/inativação, remoção e geração automática para o mês.
- Aba de pendências dedicada (pendências não ficam mais destacadas no balanço/relatório).
- Filtro por múltiplos meses (todos os meses com movimentação).
- Ação para preencher valores iniciais da base recorrente a partir da planilha de referência.
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

## Base Recorrente

- A base recorrente é local e editável.
- Itens da base podem ser adicionados, removidos, pausados e reativados a qualquer momento.
- Gerar o mês usa apenas os itens ativos da base.
- A geração evita duplicar um item que já exista no mesmo mês com a mesma descrição, categoria e tipo.
- Alterar a base não muda meses anteriores.
- Existe um atalho para preencher valores iniciais sugeridos com base na planilha usada como referência.

Valores sugeridos aplicados automaticamente quando o item estiver sem valor:

- Aluguel: `R$ 700,00`
- Internet: `R$ 100,00`
- Agua: `R$ 180,00`
- Luz: `R$ 280,00`
- Celular: `R$ 60,00`
- Unitv: `R$ 24,00`
- Gasolina carro: `R$ 160,00`
- Oleo da moto: `R$ 64,00`
- Faculdade: `R$ 100,00`
- ChatGPT: `R$ 37,00`
- Motoclube: `R$ 75,00`

Observação:
- `Gasolina moto` permanece sem valor inicial sugerido e pode ser preenchido manualmente no app.

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
