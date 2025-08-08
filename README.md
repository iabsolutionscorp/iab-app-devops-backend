# 🚀 IAB DevOps API

API para **gerenciar arquivos IaC** (Infrastructure as Code): upload, consulta, geração por IA e deploy.

---

## 🛠 Como rodar localmente

**Pré-requisitos**
- Docker instalado
- AWS CLI instalado
- Porta `8080` livre
- Pasta `local/` com `docker-compose.yml`, `createS3Buckets.bat` e exemplos
- Spring Boot com *profile* `local`

### 3 passos essenciais
**1) Entrar na pasta de execução**
```bash
cd local
```
*Por quê?* Aqui ficam o `docker-compose.yml`, o script de criação de buckets e o exemplo `iac-file-example`.

**2) Subir a infraestrutura base**
```bash
docker-compose up -d
```
*O que faz?* Sobe o **LocalStack** (e demais serviços) em contêineres. Aguarde até os serviços ficarem `healthy`.

**3) Preparar os buckets S3**
```bash
./createS3Buckets.bat   # Windows
```
*O que faz?* Cria os **buckets S3** necessários para a aplicação funcionar em local.

**4) Configurar o AWS CLI para LocalStack**
```bash
aws configure
AWS Access Key ID: localstack
AWS Secret Access Key: localstack
Default region: us-east-1
Default output format: json
```

**5) Ativar o profile do Spring**
- Variável de ambiente: `SPRING_PROFILES_ACTIVE=local`
- Ou configure na sua IDE (Run Configurations)

**Acessar o Swagger**
```
http://localhost:8080/swagger-ui.html
```

> 💡 **Arquivo de exemplo**: já existe um **`iac-file-example`** na pasta `local/`. Use-o se não quiser escrever um Terraform do zero — ou gere um novo via IA com o endpoint **POST `/v1/iac/generate`**.

> 🎯 **Destino de deploy**: o `application.yaml` está configurado para **LocalStack** por padrão.  
> Para apontar para a sua **conta AWS real**, ajuste as variáveis de ambiente da sessão **AWS** (ex.: `AWS_ACCESS_KEY_ID`, 
> `AWS_SECRET_ACCESS_KEY`, `AWS_DEFAULT_REGION`), remova/atualize endpoints específicos do LocalStack (ex.: `AWS_S3_ENDPOINT`),
> e inicie o `SPRING_PROFILES_ACTIVE` sem estar como local.

---

## 🌐 Base URL
```
http://localhost:8080
```

---

## 📦 Schemas

**IACRequest**
```json
{
  "prompt": "string",
  "type": "TERRAFORM"
}
```

**IACFileDto**
```json
{
  "id": 0,
  "name": "string",
  "type": "TERRAFORM",
  "url": "https://..."
}
```

---

## 🔌 Endpoints

### POST `/v1/iac` — Upload de arquivo IaC
Cria um novo arquivo de IaC no sistema.
- **Query params**: `fileName` (string), `type` (TERRAFORM)
- **Body**: `file` (multipart/form-data)
```bash
curl -X POST "http://localhost:8080/v1/iac?fileName=main.tf&type=TERRAFORM"      -F "file=@./main.tf"
```

---

### GET `/v1/iac/{id}` — Buscar arquivo por ID
Retorna informações do arquivo (nome, tipo e URL).
- **Path**: `id` (int64)
```bash
curl -X GET "http://localhost:8080/v1/iac/123"
```

---

### PUT `/v1/iac/{id}` — Atualizar arquivo IaC
Substitui o conteúdo de um arquivo existente.
- **Path**: `id` (int64)
- **Body**: `file` (multipart/form-data)
```bash
curl -X PUT "http://localhost:8080/v1/iac/123"      -F "file=@./main.tf"
```

---

### POST `/v1/iac/{id}/deploy` — Executar deploy
Dispara o processo de deploy do arquivo especificado.
- **Path**: `id` (int64)
```bash
curl -X POST "http://localhost:8080/v1/iac/123/deploy"
```

---

### POST `/v1/iac/generate` — Gerar código IaC via IA
Gera automaticamente um código IaC com base em um *prompt*.
- **Body (JSON)**: `prompt` (string), `type` (TERRAFORM)
```bash
curl -X POST "http://localhost:8080/v1/iac/generate"      -H "Content-Type: application/json"      -d '{"prompt":"Crie um bucket S3 com versionamento","type":"TERRAFORM"}'
```

---

## ✅ Status codes
- `200 OK` — Sucesso
- `400` — Erro de validação
- `401/403` — Acesso negado
- `404` — Não encontrado
- `500` — Erro interno

---

## 💡 Dicas de Uso
- **Tipos suportados**: No momento, apenas `TERRAFORM`.
- **Uploads**: Use `multipart/form-data` com o campo `file`.
- **Geração por IA**: Prompts claros geram código mais próximo do desejado.
- **Deploy**: Verifique os logs para detalhes do processo.
- **Versionamento**: Guarde o `id` ou `url` retornado pelo `GET /v1/iac/{id}`.
