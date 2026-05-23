# GitHub Setup Guide — AI Dev Academy Projects

Complete step-by-step guide to push all 20 projects to GitHub
so users can clone, download and run them.

---

## STEP 1 — Create GitHub Organization

1. Go to **github.com** → click your profile picture top-right
2. Click **"Your organizations"**
3. Click **"New organization"**
4. Choose **Free plan**
5. Organization name: `ai-dev-academy`
6. Contact email: your email
7. Click **"Create organization"**

✅ Your org URL will be: `https://github.com/ai-dev-academy`

---

## STEP 2 — Create the Monorepo

1. Inside your new org, click **"New repository"**
2. Fill in:
   - Repository name: `ai-dev-academy-projects`
   - Description: `Complete Spring Boot AI integration projects for every AI Dev Academy topic`
   - Visibility: **Public** ← important so users can clone for free
   - ✅ Add a README file
   - .gitignore: **None** (our projects have their own)
3. Click **"Create repository"**

✅ Your repo URL: `https://github.com/ai-dev-academy/ai-dev-academy-projects`

---

## STEP 3 — Push the Code (Git Bash on Windows)

Open **Git Bash** and run these commands:

```bash
# Navigate to where you extracted the zip
cd ~/Downloads
unzip ai-dev-academy-projects.zip
cd ai-dev-academy-projects

# Initialize git
git init
git add .
git commit -m "Add all 20 Spring Boot AI integration projects"

# Connect to GitHub (replace YOUR-TOKEN with your Personal Access Token)
git remote add origin https://github.com/ai-dev-academy/ai-dev-academy-projects.git
git branch -M main
git push -u origin main
```

**If push asks for username/password:**
- Username: your GitHub username
- Password: use a Personal Access Token (see Step 3a below)

---

## STEP 3a — Create a Personal Access Token (PAT)

GitHub no longer accepts passwords for git push. You need a PAT:

1. Go to **github.com** → Profile picture → **Settings**
2. Scroll down → **Developer settings** (bottom of left sidebar)
3. **Personal access tokens** → **Tokens (classic)**
4. Click **"Generate new token (classic)"**
5. Note: `AI Dev Academy push`
6. Expiration: **90 days**
7. Scopes: check ✅ **repo** (gives full repo access)
8. Click **"Generate token"**
9. **COPY THE TOKEN NOW** — you won't see it again

Use this token as your password when git push asks.

---

## STEP 4 — Verify It Worked

1. Go to `https://github.com/ai-dev-academy/ai-dev-academy-projects`
2. You should see all 20 topic folders
3. Click any folder (e.g. `01-intro-to-llm-apis`) — you should see the README

---

## STEP 5 — Update Web App GitHub Links

The web app already points to the correct repo URLs. After pushing, the buttons will work:

| Button | Links to |
|--------|----------|
| View on GitHub | `github.com/ai-dev-academy/ai-dev-academy-projects/tree/main/01-intro-to-llm-apis` |
| Download ZIP | `github.com/ai-dev-academy/ai-dev-academy-projects/archive/refs/heads/main.zip` |
| View README | `github.com/ai-dev-academy/ai-dev-academy-projects/blob/main/01-intro-to-llm-apis/README.md` |

---

## STEP 6 — Test Clone & Run (Verify Everything Works)

Open a new terminal and test the full user flow:

```bash
# 1. Clone (what your users will do)
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/01-intro-to-llm-apis

# 2. Set up API key
cp .env.example .env
# Edit .env → add your OPENAI_API_KEY

# 3. Run
./mvnw spring-boot:run

# 4. Test (open new terminal)
curl http://localhost:8080/ai/health
# Expected: "Topic 01 - Intro to LLM APIs is running!"

curl -X POST http://localhost:8080/ai/chat \
  -H "Content-Type: application/json" \
  -d '"What is Spring Boot?"'
# Expected: AI response about Spring Boot
```

---

## What Users See on GitHub

### Free Topics (01-05)
Full source code visible to everyone — no login required.

### Pro/Advanced Topics (06-20)  
Full source code also visible (same repo). Users purchase the topic on aidevacademy.dev to get the guided tutorial, flowchart, and step-by-step bootcamp. The code on GitHub is the final project they build toward.

---

## Keeping the Repo Updated

When you add new content or fix bugs:

```bash
cd ai-dev-academy-projects

# Make your changes, then:
git add .
git commit -m "Fix: improve topic 09 RAG service error handling"
git push origin main
```

GitHub automatically updates — users who already cloned can run:
```bash
git pull origin main
```

---

## Optional: Add GitHub Topics Tags

Makes your repo discoverable on GitHub search:

1. Go to your repo page
2. Click the ⚙️ gear icon next to "About"
3. Add topics: `spring-boot`, `openai`, `langchain4j`, `rag`, `spring-ai`, `java`, `llm`, `ai-integration`
4. Click **Save changes**

---

## Optional: Pin the Repo on Your Org Page

1. Go to `github.com/ai-dev-academy`
2. Click **"Customize your organization's profile"**
3. Pin `ai-dev-academy-projects`

This makes it the first thing visitors see.
