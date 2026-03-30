# Documento de Requisitos

## Introdução

Esta feature substitui o fluxo de checkout externo (redirecionamento para o site do Mercado Pago) pelo **Payment Brick** do Mercado Pago, mantendo o usuário no site durante todo o processo de pagamento. O pedido já é criado antes do pagamento e possui status `AGUARDANDO_PAGAMENTO`. O frontend obtém a `preferenceId` do backend e inicializa o Brick diretamente na página, sem redirecionamento.

## Glossário

- **Payment_Brick**: Componente JavaScript do SDK `@mercadopago/sdk-js` que renderiza o formulário de pagamento embutido na página.
- **Preference**: Objeto criado no Mercado Pago que contém os dados do pedido e retorna `preferenceId` e `initPoint`.
- **Checkout_Page**: Página React (`/checkout`) que exibe o resumo do pedido e renderiza o Payment Brick.
- **Return_Page**: Página React (`/pagamento/retorno`) que exibe o resultado do pagamento após aprovação ou falha.
- **Payment_Store**: Estado Zustand responsável por armazenar `preferenceId`, `pedidoId` e status do pagamento em andamento.
- **Cart_Store**: Estado Zustand do carrinho de compras.
- **MP_SDK**: SDK JavaScript do Mercado Pago (`@mercadopago/sdk-js`).
- **Backend**: API Spring Boot 3 disponível em `/api`.
- **Webhook**: Endpoint `POST /api/payments/webhook` que recebe notificações assíncronas do Mercado Pago.

---

## Requisitos

### Requisito 1: Instalação e Inicialização do SDK do Mercado Pago

**User Story:** Como desenvolvedor, quero instalar e configurar o SDK do Mercado Pago no frontend, para que o Payment Brick possa ser renderizado corretamente.

#### Critérios de Aceitação

1. THE Frontend SHALL incluir o pacote `@mercadopago/sdk-js` como dependência no `package.json`.
2. WHEN o Checkout_Page é carregado, THE MP_SDK SHALL ser inicializado com a Public Key configurada via variável de ambiente `VITE_MP_PUBLIC_KEY`.
3. IF a variável de ambiente `VITE_MP_PUBLIC_KEY` não estiver definida, THEN THE Checkout_Page SHALL exibir uma mensagem de erro informando que a configuração de pagamento está indisponível.
4. THE MP_SDK SHALL ser inicializado com `locale: "pt-BR"`.

---

### Requisito 2: Criação da Preferência de Pagamento

**User Story:** Como cliente, quero que o sistema crie automaticamente uma preferência de pagamento ao acessar o checkout, para que o Payment Brick tenha os dados necessários para processar meu pagamento.

#### Critérios de Aceitação

1. WHEN o Checkout_Page é carregado com um `pedidoId` válido, THE Checkout_Page SHALL chamar `POST /api/payments` com `{ pedidoId, metodo: "MERCADO_PAGO" }` para obter a `preferenceId`.
2. WHEN o backend retorna `{ initPoint, preferenceId }`, THE Payment_Store SHALL armazenar a `preferenceId` e o `pedidoId`.
3. IF o backend retornar erro ao criar a preferência, THEN THE Checkout_Page SHALL exibir uma mensagem de erro descritiva e um botão para tentar novamente.
4. WHILE a preferência está sendo criada, THE Checkout_Page SHALL exibir um indicador de carregamento no lugar do Payment Brick.
5. THE Checkout_Page SHALL chamar `POST /api/payments` apenas uma vez por sessão de checkout para o mesmo `pedidoId`.

---

### Requisito 3: Renderização do Payment Brick

**User Story:** Como cliente, quero ver o formulário de pagamento diretamente na página de checkout, para que eu possa pagar sem ser redirecionado para outro site.

#### Critérios de Aceitação

1. WHEN a `preferenceId` está disponível no Payment_Store, THE Checkout_Page SHALL renderizar o Payment Brick dentro de um contêiner com `id="paymentBrick_container"`.
2. THE Payment_Brick SHALL ser inicializado com `initialization: { amount: valorTotal, preferenceId }` e `locale: "pt-BR"`.
3. THE Payment_Brick SHALL exibir as opções de pagamento disponíveis para o valor do pedido (cartão de crédito, boleto, Pix).
4. WHEN o Checkout_Page é desmontado, THE Payment_Brick SHALL ser destruído para liberar recursos.
5. IF o Payment Brick falhar ao renderizar, THEN THE Checkout_Page SHALL exibir uma mensagem de erro e um link para suporte.

---

### Requisito 4: Processamento do Resultado do Pagamento

**User Story:** Como cliente, quero ser informado imediatamente sobre o resultado do meu pagamento, para que eu saiba se meu pedido foi confirmado ou se preciso tentar novamente.

#### Critérios de Aceitação

1. WHEN o Mercado Pago retorna `status: "approved"` no callback `onSubmit` do Payment Brick, THE Checkout_Page SHALL limpar o Cart_Store e redirecionar para `/pagamento/retorno?status=approved&pedidoId={pedidoId}`.
2. WHEN o Mercado Pago retorna `status: "rejected"` ou `status: "error"` no callback `onError` do Payment Brick, THE Checkout_Page SHALL exibir uma mensagem de erro descritiva sem redirecionar o usuário.
3. WHEN o Mercado Pago retorna `status: "pending"` no callback `onSubmit`, THE Checkout_Page SHALL redirecionar para `/pagamento/retorno?status=pending&pedidoId={pedidoId}`.
4. IF o pagamento for rejeitado, THEN THE Checkout_Page SHALL manter o Payment Brick visível para que o usuário possa tentar novamente.
5. WHEN o pagamento é aprovado, THE Cart_Store SHALL ter todos os itens removidos antes do redirecionamento.

---

### Requisito 5: Página de Retorno do Pagamento

**User Story:** Como cliente, quero ver uma página de confirmação após o pagamento, para que eu tenha clareza sobre o status do meu pedido.

#### Critérios de Aceitação

1. THE Return_Page SHALL ler o parâmetro `status` da query string da URL.
2. WHEN `status=approved`, THE Return_Page SHALL exibir uma mensagem de sucesso com o número do pedido e um link para `/meus-pedidos`.
3. WHEN `status=pending`, THE Return_Page SHALL exibir uma mensagem informando que o pagamento está em análise e um link para `/meus-pedidos`.
4. WHEN `status=failure` ou qualquer valor não reconhecido, THE Return_Page SHALL exibir uma mensagem de falha e um link para retornar ao carrinho.
5. IF o parâmetro `status` não estiver presente na URL, THEN THE Return_Page SHALL redirecionar para `/`.

---

### Requisito 6: Persistência de Pedidos sem Pagamento

**User Story:** Como administrador, quero que pedidos criados mas não pagos permaneçam no banco de dados com status identificável, para que eu possa monitorar e gerenciar pedidos abandonados.

#### Critérios de Aceitação

1. WHEN um pedido é criado via `POST /api/pedidos`, THE Backend SHALL persistir o pedido com `status: AGUARDANDO_PAGAMENTO`.
2. WHILE o pagamento não for confirmado pelo Webhook, THE Backend SHALL manter o pedido com `status: AGUARDANDO_PAGAMENTO`.
3. WHEN o Webhook recebe notificação com `status: approved`, THE Backend SHALL atualizar o pedido para `status: PAGO`.
4. IF o Webhook receber uma notificação com `type` diferente de `"payment"`, THEN THE Webhook SHALL ignorar a notificação e retornar HTTP 200.
5. THE Backend SHALL processar o Webhook de forma idempotente: chamadas repetidas com o mesmo `mpPaymentId` SHALL resultar no mesmo estado final do pedido.

---

### Requisito 7: Segurança e Configuração

**User Story:** Como desenvolvedor, quero que as chaves do Mercado Pago sejam gerenciadas de forma segura, para que credenciais não sejam expostas no código-fonte.

#### Critérios de Aceitação

1. THE Frontend SHALL ler a Public Key do Mercado Pago exclusivamente da variável de ambiente `VITE_MP_PUBLIC_KEY`.
2. THE Backend SHALL ler o Access Token do Mercado Pago exclusivamente de variável de ambiente ou configuração segura, nunca de código-fonte.
3. THE Frontend SHALL incluir `VITE_MP_PUBLIC_KEY` no arquivo `.env.exemplo` com um valor de placeholder.
4. THE Backend SHALL validar a assinatura do Webhook do Mercado Pago antes de processar qualquer notificação.
5. IF uma requisição ao Webhook não puder ser validada, THEN THE Webhook SHALL retornar HTTP 400 e registrar o evento em log.
