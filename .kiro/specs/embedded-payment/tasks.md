# Plano de Implementação: Pagamento Embutido (Embedded Payment)

## Visão Geral

Substituir o redirecionamento externo para o Mercado Pago pelo Payment Brick embutido na página de checkout, implementar validação de assinatura no webhook, adicionar páginas de perfil e histórico de pedidos, e cobrir as propriedades de corretude com testes PBT.

## Tarefas

- [ ] 1. Instalar dependência e configurar variáveis de ambiente
  - Instalar `@mercadopago/sdk-js` via npm no projeto frontend
  - Adicionar `VITE_MP_PUBLIC_KEY=TEST-sua-public-key-aqui` ao `.env.exemplo`
  - Adicionar `VITE_MP_PUBLIC_KEY` ao `.env` com a chave de sandbox real
  - _Requirements: 1.1, 7.1, 7.3_

- [ ] 2. Criar `paymentStore.ts` (Zustand)
  - [ ] 2.1 Criar `src/stores/paymentStore.ts` com interface `PaymentState`
    - Campos: `preferenceId: string | null`, `pedidoId: string | null`
    - Ações: `setPreferenceId(id)`, `setPedidoId(id)`, `reset()`
    - _Requirements: 2.2_

  - [ ]* 2.2 Escrever property test para `paymentStore` (P2)
    - **Property 2: Payment_Store armazena dados da preferência**
    - **Validates: Requirements 2.2**
    - Usar `fc.string()` para gerar `preferenceId` e `pedidoId` arbitrários
    - Verificar que após `setPreferenceId` + `setPedidoId`, o store contém os valores exatos
    - Anotar: `// Feature: embedded-payment, Property 2`

- [ ] 3. Criar componente `PaymentBrick.tsx`
  - [ ] 3.1 Criar `src/components/PaymentBrick.tsx` com a interface `PaymentBrickProps`
    - Props: `preferenceId`, `amount`, `pedidoId`, `onApproved`, `onPending`, `onError`
    - Renderizar `<div id="paymentBrick_container" />`
    - Inicializar `MercadoPago` com `VITE_MP_PUBLIC_KEY` e `locale: "pt-BR"` dentro de `useEffect`
    - Chamar `mp.bricks().create("payment", "paymentBrick_container", { initialization: { amount, preferenceId }, callbacks: { onSubmit, onError } })`
    - Armazenar `brickController` em `useRef<BrickController | null>`
    - Usar flag `cancelled` para evitar race condition no StrictMode
    - Cleanup do `useEffect`: chamar `brickController.current?.unmount()` e setar `null`
    - No `onSubmit`: se `status === "approved"` → chamar `onApproved()`; se `status === "pending"` → chamar `onPending()`
    - No `onError`: chamar `onError(error.message)`
    - _Requirements: 1.2, 1.4, 3.1, 3.2, 3.4_

  - [ ]* 3.2 Escrever property test para inicialização do Brick com parâmetros corretos (P5)
    - **Property 5: Brick inicializado com parâmetros corretos no container correto**
    - **Validates: Requirements 3.1, 3.2**
    - Usar `fc.string()` e `fc.float({ min: 0.01 })` para gerar `preferenceId` e `amount`
    - Mockar `MercadoPago` e verificar que `bricks().create` recebe `initialization: { amount, preferenceId }` e container `"paymentBrick_container"`
    - Anotar: `// Feature: embedded-payment, Property 5`

  - [ ]* 3.3 Escrever property test para destruição do Brick ao desmontar (P6)
    - **Property 6: Brick destruído exatamente uma vez ao desmontar**
    - **Validates: Requirements 3.4**
    - Usar `fc.string()` e `fc.float({ min: 0.01 })` para gerar inputs arbitrários
    - Montar e desmontar o componente; verificar que `unmount` foi chamado exatamente 1 vez
    - Anotar: `// Feature: embedded-payment, Property 6`

- [ ] 4. Atualizar `Checkout.tsx`
  - [ ] 4.1 Adicionar estado `checkoutState: CheckoutState` e `useRef<boolean>` para evitar double-invoke
    - Definir tipo `CheckoutState` com os valores: `"idle" | "criando-pedido" | "criando-preferencia" | "exibindo-brick" | "processando" | "concluido" | "erro"`
    - Adicionar ref `hasCalledPaymentAPI` inicializado como `false`
    - _Requirements: 2.4, 2.5_

  - [ ] 4.2 Substituir redirecionamento para `initPoint` pelo fluxo de estados
    - Ao montar: se `!hasCalledPaymentAPI.current`, chamar `POST /api/pedidos` → `POST /api/payments`
    - Setar `hasCalledPaymentAPI.current = true` antes da chamada
    - Salvar `preferenceId` e `pedidoId` no `paymentStore`
    - Transicionar `checkoutState` conforme o fluxo: `idle → criando-pedido → criando-preferencia → exibindo-brick`
    - Em caso de erro: `checkoutState → "erro"`, exibir mensagem + botão "Tentar novamente"
    - Botão retry: resetar `hasCalledPaymentAPI.current = false` e `checkoutState = "idle"`
    - Remover `window.location.href = initPoint`
    - _Requirements: 2.1, 2.3, 2.4, 2.5, 1.3_

  - [ ] 4.3 Renderizar `<PaymentBrick>` quando `checkoutState === "exibindo-brick"`
    - Exibir `<Skeleton>` / spinner durante `"criando-pedido"` e `"criando-preferencia"`
    - Passar `preferenceId`, `amount` (valorTotal do pedido), `pedidoId` para o Brick
    - Implementar `onApproved`: chamar `cartStore.clearCart()` → `navigate("/pagamento/retorno?status=approved&pedidoId=" + pedidoId)`
    - Implementar `onPending`: `navigate("/pagamento/retorno?status=pending&pedidoId=" + pedidoId)`
    - Implementar `onError`: exibir toast de erro inline (Brick permanece visível)
    - _Requirements: 3.1, 3.3, 4.1, 4.2, 4.3, 4.4, 4.5_

  - [ ]* 4.4 Escrever property test para chamada única à API de preferência (P3)
    - **Property 3: Chamada à API de preferência é feita exatamente uma vez**
    - **Validates: Requirements 2.5**
    - Usar `fc.integer({ min: 1, max: 20 })` para simular N re-renders
    - Mockar `paymentService.createPreference` e verificar que foi chamado exatamente 1 vez
    - Anotar: `// Feature: embedded-payment, Property 3`

  - [ ]* 4.5 Escrever property test para aprovação: clearCart antes de navigate com URL correta (P7)
    - **Property 7: Aprovação limpa carrinho e redireciona com parâmetros corretos**
    - **Validates: Requirements 4.1, 4.5**
    - Usar `fc.uuid()` para gerar `pedidoId` arbitrário
    - Simular callback `onSubmit` com `status: "approved"` e verificar que `clearCart` foi chamado antes de `navigate`
    - Verificar que a URL contém `status=approved` e `pedidoId={pedidoId}`
    - Anotar: `// Feature: embedded-payment, Property 7`

  - [ ]* 4.6 Escrever property test para mapeamento de status para URL de retorno (P8)
    - **Property 8: Status de pagamento mapeia para URL de retorno correta**
    - **Validates: Requirements 4.1, 4.3**
    - Usar `fc.constantFrom("approved", "pending")` para gerar status arbitrário
    - Verificar que a URL de navegação contém `status={valor}` e `pedidoId={pedidoId}`
    - Anotar: `// Feature: embedded-payment, Property 8`

- [ ] 5. Atualizar `PaymentReturn.tsx`
  - [ ] 5.1 Adicionar leitura do parâmetro `?status=` com prioridade sobre `?collection_status=`
    - Ler `status` e `collection_status` da query string via `useSearchParams`
    - Prioridade: `status` > `collection_status`
    - Se nenhum parâmetro presente: `navigate("/")`
    - Renderizar mensagem de sucesso para `approved`, análise para `pending`, falha para `failure` ou valor desconhecido
    - Incluir link para `/meus-pedidos` nos casos `approved` e `pending`
    - Incluir link para `/carrinho` no caso de falha
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [ ]* 5.2 Escrever property test para mapeamento de status na Return Page (P10)
    - **Property 10: Return_Page mapeia status para mensagem correta**
    - **Validates: Requirements 5.1, 5.2, 5.3, 5.4**
    - Usar `fc.constantFrom("approved", "pending", "failure", "unknown_value", "")` para cobrir todos os casos
    - Verificar que cada status renderiza o texto/componente correspondente
    - Anotar: `// Feature: embedded-payment, Property 10`

- [ ] 6. Checkpoint — Testes do frontend passando
  - Garantir que todos os testes do frontend passam. Perguntar ao usuário se houver dúvidas.

- [x] 7. Implementar validação de assinatura do Webhook (backend)
  - [x] 7.1 Criar `WebhookSignatureValidator.java` em `src/main/java/.../services/`
    - Ler `x-signature` e `x-request-id` dos headers da requisição
    - Ler `data.id` da query string
    - Construir o template: `id:{data.id};request-id:{x-request-id};ts:{ts};`
    - Calcular HMAC-SHA256 com o `MERCADOPAGO_WEBHOOK_SECRET` lido de `application.properties`
    - Retornar `boolean` indicando se a assinatura é válida
    - _Requirements: 7.4_

  - [x] 7.2 Atualizar `PaymentController.processWebhookPayment` para validar assinatura
    - Injetar `WebhookSignatureValidator`
    - Receber `@RequestHeader Map<String, String> headers` e `@RequestParam Map<String, String> params`
    - Chamar `validator.isValid(headers, params)` antes de processar
    - Se inválido: logar o evento e retornar `ResponseEntity.badRequest().build()` (HTTP 400)
    - Se válido: delegar para `paymentService.processWebhookPayment(data)`
    - _Requirements: 7.4, 7.5_

  - [x] 7.3 Adicionar `mercadopago.webhook.secret` ao `application.properties` e `.env.exemplo` do backend
    - _Requirements: 7.2, 7.4_

  - [ ]* 7.4 Escrever property test jqwik para assinatura inválida retorna HTTP 400 (P15)
    - **Property 15: Webhook com assinatura inválida retorna HTTP 400**
    - **Validates: Requirements 7.4, 7.5**
    - Usar `@ForAll String invalidSignature` para gerar assinaturas arbitrárias inválidas
    - Verificar que o endpoint retorna HTTP 400 para qualquer assinatura que não passe na validação
    - Anotar: `// Feature: embedded-payment, Property 15`

- [x] 8. Garantir idempotência e corretude do Webhook (backend)
  - [x] 8.1 Verificar que `PedidoService.criarPedido()` persiste com `status: AGUARDANDO_PAGAMENTO`
    - Confirmar que o campo `status` é setado corretamente na criação
    - Se necessário, ajustar o método para garantir o valor padrão
    - _Requirements: 6.1, 6.2_

  - [x] 8.2 Verificar que `PaymentService.processWebhookPayment()` é idempotente
    - Confirmar que processar o mesmo `mpPaymentId` N vezes resulta no mesmo estado final
    - Se necessário, adicionar verificação de estado atual antes de atualizar (`if status != PAGO then update`)
    - _Requirements: 6.3, 6.5_

  - [x] 8.3 Verificar que webhook com `type != "payment"` retorna HTTP 200 sem alterar estado
    - Confirmar que o `PaymentController` ou `PaymentService` ignora tipos desconhecidos
    - _Requirements: 6.4_

  - [ ]* 8.4 Escrever property test jqwik para status inicial AGUARDANDO_PAGAMENTO (P11)
    - **Property 11: Pedido criado com status AGUARDANDO_PAGAMENTO**
    - **Validates: Requirements 6.1, 6.2**
    - Usar `@ForAll @From("validPedidoRequests")` para gerar pedidos arbitrários
    - Verificar que o status persistido é sempre `AGUARDANDO_PAGAMENTO`
    - Anotar: `// Feature: embedded-payment, Property 11`

  - [ ]* 8.5 Escrever property test jqwik para idempotência do webhook (P12)
    - **Property 12: Webhook idempotente**
    - **Validates: Requirements 6.5**
    - Usar `@ForAll String mpPaymentId` e `@ForAll @IntRange(min=1, max=10) int n` para simular N chamadas
    - Verificar que o estado final do pedido é o mesmo independentemente de N
    - Anotar: `// Feature: embedded-payment, Property 12`

  - [ ]* 8.6 Escrever property test jqwik para webhook approved atualiza para PAGO (P13)
    - **Property 13: Webhook approved atualiza pedido para PAGO**
    - **Validates: Requirements 6.3**
    - Usar `@ForAll @From("approvedWebhookPayloads")` para gerar notificações aprovadas
    - Verificar que o pedido correspondente tem status `PAGO` após o processamento
    - Anotar: `// Feature: embedded-payment, Property 13`

  - [ ]* 8.7 Escrever property test jqwik para webhook com type != "payment" não altera estado (P14)
    - **Property 14: Webhook com type != "payment" não altera estado do pedido**
    - **Validates: Requirements 6.4**
    - Usar `fc.string().filter(t => t !== "payment")` / `@ForAll @StringLength(min=1) @NotContains("payment") String type`
    - Verificar que o status do pedido não muda e o endpoint retorna HTTP 200
    - Anotar: `// Feature: embedded-payment, Property 14`

- [x] 9. Checkpoint — Testes do backend passando
  - Garantir que todos os testes do backend passam. Perguntar ao usuário se houver dúvidas.

- [ ] 10. Criar página de Perfil do Usuário
  - [ ] 10.1 Criar `src/pages/Perfil.tsx`
    - Ler `user` do `authStore` (nome, email, role)
    - Exibir avatar/foto (usar inicial do nome como fallback com `shadcn/ui Avatar`)
    - Exibir nome, email e role do usuário
    - _Requirements: (feature adicional — perfil do usuário)_

  - [ ] 10.2 Adicionar rota `/perfil` no `App.tsx`
    - Proteger a rota com verificação de `isAuthenticated` do `authStore`
    - Redirecionar para `/login` se não autenticado

- [ ] 11. Criar página de Meus Pedidos
  - [ ] 11.1 Criar `src/services/pedidoService.ts` (ou atualizar `orderService.ts`)
    - Adicionar função `getByUsuario(usuarioId: string)` que chama `GET /api/pedidos/usuario/{usuarioId}`
    - Adicionar função `getPaymentByPedido(pedidoId: string)` que chama `GET /api/payments/pedido/{pedidoId}`
    - _Requirements: (feature adicional — meus pedidos)_

  - [ ] 11.2 Criar `src/pages/MeusPedidos.tsx`
    - Ler `user.id` do `authStore`
    - Usar `useQuery` (React Query) para buscar pedidos via `getByUsuario(user.id)`
    - Exibir lista de pedidos com: número do pedido, data, valor total, status do pedido e status do pagamento
    - Exibir estado de loading com `Skeleton` e estado de erro com mensagem descritiva
    - Exibir mensagem "Nenhum pedido encontrado" quando a lista estiver vazia
    - _Requirements: (feature adicional — meus pedidos)_

  - [ ] 11.3 Adicionar rota `/meus-pedidos` no `App.tsx`
    - Proteger a rota com verificação de `isAuthenticated`
    - Redirecionar para `/login` se não autenticado
    - Atualizar links em `PaymentReturn.tsx` para apontar para `/meus-pedidos`

- [ ] 12. Checkpoint final — Integração completa
  - Garantir que todos os testes (frontend e backend) passam.
  - Verificar que o fluxo completo funciona: checkout → brick → retorno → meus pedidos.
  - Perguntar ao usuário se houver dúvidas antes de concluir.

## Notas

- Tarefas marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido
- Testes PBT do frontend usam `fast-check` (já instalado como devDependency)
- Testes PBT do backend usam `jqwik` (adicionar dependência ao `pom.xml` se necessário)
- Cada property test deve ser anotado com `// Feature: embedded-payment, Property {N}`
- O `paymentBrick_container` deve ser um `div` com esse `id` exato para o SDK do MP funcionar
- A flag `hasCalledPaymentAPI` (useRef) é essencial para evitar double-invoke no React StrictMode
