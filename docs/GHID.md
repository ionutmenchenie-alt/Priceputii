# Ghid pas cu pas

Ai nevoie de contul tău Google (pentru Firebase) și de contul GitHub. Durează cam 30 de minute prima dată.

## 1. Proiectul Firebase

1. Intră pe <https://console.firebase.google.com> → **Add project** (Creează proiect) → nume `Priceputii`.
   Google Analytics îl poți dezactiva.
2. În pagina proiectului apasă iconița **Android** și completează:
   - **Android package name:** `ro.priceputii.app`
   - **App nickname:** Priceputii
   - **Debug signing certificate SHA-1:** `B0:FD:DC:3C:FA:96:E6:0C:A9:43:4E:E8:DA:93:76:D2:09:26:43:ED`
3. Apasă **Register app**, apoi **Download google-services.json**. Păstrează fișierul.
   Nu-l urca în repository (repository-ul e public); mai jos îți spun unde îl pui.
   Restul pașilor din pagină („Add Firebase SDK”) îi sari, sunt deja în cod.
4. În **Project settings** (rotița din stânga sus) → tabul **General** → la aplicația Android → **Add fingerprint**
   și adaugă și SHA-256:
   `1B:BA:31:01:57:2D:F2:C3:A6:CD:49:55:78:5A:4C:6F:9C:59:86:B9:9B:FD:49:68:76:AE:E2:D8:F0:07:97:D1`

## 2. Autentificarea cu telefonul

1. Meniul din stânga: **Build → Authentication → Get started**.
2. Tabul **Sign-in method** → **Phone** → **Enable** → **Save**.
3. Tot acolo, la **Phone numbers for testing**, adaugă:
   - `+40 700 000 001` cu codul `123456`
   - `+40 700 000 002` cu codul `123456`

   Cu numerele acestea intri în aplicație fără SMS real (gratuit). Le poți folosi ca să joci rolul
   de client pe un telefon și de Priceput pe altul.
4. Tabul **Settings → SMS region policy** → **Allow** → bifează doar **Romania**. Așa nimeni nu poate trimite SMS-uri
   în alte țări pe banii proiectului.

> SMS-urile reale cer, la Firebase, planul **Blaze** (plătești doar ce folosești; primele SMS-uri costă câțiva bani
> fiecare). Pentru teste ajung numerele de mai sus, pe planul gratuit. Când trecem la Blaze, pune o alertă de buget.

## 3. Baza de date

1. **Build → Firestore Database → Create database**.
2. Locația: **eur3 (europe-west)** (datele rămân în UE).
3. Alege **Start in production mode** → **Create**.
4. Tabul **Rules**: șterge tot ce e acolo, lipește conținutul fișierului
   [`firestore.rules`](../firestore.rules) din repository și apasă **Publish**.

Fără pasul 4 aplicația rămâne la ecranul de încărcare, pentru că baza de date refuză orice citire.

## 4. Aplicația pe telefon (varianta simplă, fără Android Studio)

GitHub construiește singur aplicația la fiecare modificare. Trebuie doar să-i dai fișierul Firebase, o singură dată:

1. Deschide `google-services.json` cu Notepad și copiază tot textul.
2. Pe GitHub, în repository: **Settings → Secrets and variables → Actions → New repository secret**.
   - **Name:** `GOOGLE_SERVICES_JSON`
   - **Secret:** lipește textul copiat → **Add secret**.
3. Tabul **Actions** → workflow-ul **Android** → **Run workflow** (sau deschide ultima rulare și apasă **Re-run all jobs**).
4. Când rularea are bifă verde, deschide-o și descarcă **priceputii-debug-apk** de jos, din „Artifacts”.
   Primești un `.zip` cu fișierul `app-debug.apk` înăuntru.
5. Pune `app-debug.apk` pe telefon (cablu, Google Drive sau WhatsApp către tine) și deschide-l.
   Telefonul te întreabă dacă permiți instalarea din această sursă: apasă **Setări → Permite**, apoi **Instalează**.

La fiecare versiune nouă repeți pașii 3–5; aplicația se actualizează peste cea veche.

## 5. Varianta cu Android Studio (opțional)

Utilă dacă vrei să rulezi aplicația direct pe telefon prin cablu sau într-un emulator.

1. Instalează Android Studio de pe <https://developer.android.com/studio>.
2. **File → New → Project from Version Control** → URL: `https://github.com/ionutmenchenie-alt/Priceputii`.
3. Copiază `google-services.json` în folderul `app` al proiectului (lângă `build.gradle.kts`).
4. Pe telefon activează **Opțiuni pentru dezvoltatori → Depanare USB**, conectează-l prin cablu,
   alege telefonul sus în Android Studio și apasă ▶ (Run).

## Cum testezi

1. Pe telefonul A: „Am o lucrare” → numele tău → `0700 000 001` → codul `123456` → „Postează lucrare”.
2. Pe telefonul B (sau după „Ieși din cont” pe același): „Sunt Priceput” → `0700 000 002` → codul `123456`
   → bifează lucrările și zonele → în „Lucrări” deschizi lucrarea → „Sunt interesat de lucrarea dvs.”.
3. Pe telefonul A, în „Ale mele”, lucrarea arată Priceputul interesat, cu Sună, SMS și Mesaj.
