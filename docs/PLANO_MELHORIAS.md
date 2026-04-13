# Plano de Melhorias - Dinheiro Sumiu Pro

## Objetivo
Evoluir o app de controle financeiro pessoal para uma versão pronta para distribuição, com foco em confiabilidade, organização dos dados e potencial de monetização.

## Fase 1 - Base de Produto (curto prazo)
1. Refinar UX de lançamentos
- Máscara monetária melhor no campo de valor.
- Validação de formulário com mensagens mais específicas.
- Confirmação antes de excluir item.

2. Melhorar relatórios
- PDF multipágina (hoje está em 1 página).
- Exportação por período customizado.
- Resumo por categoria com percentuais.

3. Qualidade mínima de release
- Testes unitários de cálculo (saldo, pendências, categorias).
- Smoke test de fluxo principal (criar/editar/excluir/exportar).
- Ícone, nome e tema revisados para publicação.

## Fase 2 - Offline First + Sync (médio prazo)
1. Conta de usuário opcional
- Login com Google (Firebase Auth).
- Conta local continua funcionando sem login.

2. Sincronização em nuvem
- Room local como fonte principal.
- Sincronização com Firestore em segundo plano.
- Estratégia de conflito: último update vence + histórico simples.

3. Backup e recuperação
- Backup manual e automático para nuvem.
- Restauração de dados ao trocar de aparelho.

## Fase 3 - Monetização e Escala (médio/longo prazo)
1. Recursos Premium
- Categorias/etiquetas ilimitadas.
- Relatórios avançados e comparativos anuais.
- Exportações avançadas com branding.

2. Assinatura
- Google Play Billing com plano mensal/anual.
- Modo gratuito com limite de funcionalidades.

3. Distribuição
- Publicar versão estável na Play Store.
- Implementar Firebase Crashlytics e Analytics.
- Coletar feedback dos usuários para roadmap contínuo.

## Checklist Técnico Prioritário
- Migrar para arquitetura por módulos conforme crescimento.
- Introduzir camada de logs e tratamento de erro centralizado.
- Configurar CI (build + testes) em pull requests.
- Definir versionamento semântico e changelog.
