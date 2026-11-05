# Portfolio with an owner-only dashboard

Java 17, Spring Boot, Thymeleaf, Spring Security, Tailwind CSS. Everyone can see the site.
Only you can log in at `/admin` to add, edit and delete projects (link, photo, tech, GitHub).

## Run on your Mac
```bash
brew install openjdk@17 maven      # once
mvn spring-boot:run
```
Open http://localhost:8080. Owner login: http://localhost:8080/admin
Default ID `admin`, password `change-me-now`. Change them before you deploy.

## Make it yours
Edit the `portfolio.*` lines in `src/main/resources/application.properties`
(name, typing roles, intro, email, phone, GitHub, LinkedIn, skills ticker).
The summary, skill groups and education are plain text in `templates/index.html`.
Set your real GitHub and LinkedIn links there too, they are still placeholders.
Delete the two sample projects from the dashboard after you log in.

## Deploy (GitHub + Neon database + Render)
Free Render databases expire after 30 days, so use Neon for the database (its free plan has no time limit).

1. Push this folder to a new GitHub repository.
2. Neon (neon.com): create a free project. From its connection details note the host, database, user and password.
3. Render (render.com): New > Web Service > pick your repo > Language: Docker > Instance type: Free.
4. Add these environment variables on the Render service:
   - `ADMIN_USER` = your login ID
   - `ADMIN_PASS` = a long password
   - `DB_URL` = `jdbc:postgresql://<neon-host>/<database>?sslmode=require`
   - `DB_USER` = Neon user
   - `DB_PASSWORD` = Neon password
   - `JAVA_TOOL_OPTIONS` = `-Xmx300m` (the free instance has only 512 MB of RAM)
5. Deploy. Open `https://<your-service>.onrender.com/admin`, log in, delete the two sample projects, add yours.

Notes:
- Free Render services sleep after 15 minutes without visitors and take about a minute to wake up.
- Free Render disks are wiped on every deploy and restart, so uploaded photos disappear.
  In the dashboard, paste a photo link instead (GitHub raw link, Cloudinary, etc.).
- Never use the default `H2` database on Render, its data is lost too. Always set `DB_URL`.
# portfolio-moblap
