# Environment Variables Setup Guide

## Why Environment Variables?

Environment variables keep sensitive data (passwords, API keys) out of your code and Git repository.

## Setup Methods

### Method 1: Using .env File (Recommended for Local Development)

1. **Create `.env` file** (already done):
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` with your actual values**:
   ```bash
   # .env
   DB_USERNAME=userapp
   DB_PASSWORD=your_actual_password
   ```

3. **Run the application**:
   ```bash
   ./run-app.sh
   ```

   This script automatically loads variables from `.env` and starts the app.

### Method 2: Export in Terminal (Temporary - Current Session Only)

```bash
export DB_USERNAME=userapp
export DB_PASSWORD=password123

# Then run the app
./mvnw spring-boot:run
```

### Method 3: Add to Shell Profile (Permanent)

**For macOS/Linux (zsh):**

```bash
# Edit your shell profile
nano ~/.zshrc

# Add these lines at the end:
export DB_USERNAME=userapp
export DB_PASSWORD=password123

# Save and reload
source ~/.zshrc
```

**For macOS/Linux (bash):**

```bash
# Edit your shell profile
nano ~/.bash_profile

# Add the same export lines
# Save and reload
source ~/.bash_profile
```

### Method 4: IntelliJ IDEA / IDE

1. Open **Run/Debug Configurations**
2. Add **Environment Variables**:
   ```
   DB_USERNAME=userapp;DB_PASSWORD=password123
   ```

### Method 5: Docker / Production

**docker-compose.yml:**
```yaml
services:
  app:
    environment:
      - DB_USERNAME=userapp
      - DB_PASSWORD=${DB_PASSWORD}
```

**Kubernetes:**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
data:
  username: dXNlcmFwcA==  # base64 encoded
  password: cGFzc3dvcmQxMjM=  # base64 encoded
```

## How It Works

**application.yml** uses this syntax:
```yaml
username: ${DB_USERNAME:userapp}
password: ${DB_PASSWORD:password}
```

- `${DB_USERNAME:userapp}` means:
  - Use environment variable `DB_USERNAME` if it exists
  - Otherwise, use default value `userapp`

## Verify Environment Variables

```bash
# Check if variables are set
echo $DB_USERNAME
echo $DB_PASSWORD

# Or check all environment variables
env | grep DB_
```

## Security Best Practices

✅ **DO:**
- Use `.env` for local development
- Add `.env` to `.gitignore`
- Commit `.env.example` (without real passwords)
- Use secrets management in production (AWS Secrets Manager, HashiCorp Vault)

❌ **DON'T:**
- Commit `.env` file to Git
- Hardcode passwords in `application.yml`
- Share `.env` file publicly
- Use the same password in production as development

## Quick Start

```bash
# 1. Copy example file
cp .env.example .env

# 2. Edit with your values
nano .env

# 3. Run the app
./run-app.sh
```

## Troubleshooting

### App can't connect to database

**Check if variables are loaded:**
```bash
./mvnw spring-boot:run | grep "DB_USERNAME"
```

**Manually set and test:**
```bash
export DB_USERNAME=userapp
export DB_PASSWORD=password123
./mvnw spring-boot:run
```

### Variables not loading from .env

**Use the run script:**
```bash
./run-app.sh
```

**Or load manually:**
```bash
source .env
./mvnw spring-boot:run
```

## Files Overview

| File | Purpose | Commit to Git? |
|------|---------|----------------|
| `.env` | Your actual secrets | ❌ NO |
| `.env.example` | Template with placeholders | ✅ YES |
| `run-app.sh` | Startup script | ✅ YES |
| `application.yml` | Config with `${VAR}` syntax | ✅ YES |

## Summary

🔒 **Your secrets are now safe!**
- `.env` file is ignored by Git
- `application.yml` uses environment variables
- Easy to change passwords without touching code
- Production-ready configuration
