# Priceputii

Aplicație Android în care oamenii postează treburi prin gospodărie (cu preț în lei și zonă),
iar Priceputii din oraș le găsesc, se oferă și iau legătura cu clientul. Pornim cu Târgu Jiu.

**Cum o pui pe telefon și cum configurezi Firebase:** vezi [docs/GHID.md](docs/GHID.md).

## Ce face prima versiune

- Alegerea rolului: „Sunt Priceput” sau „Am o lucrare” (se poate schimba din Profil).
- Cont cu numărul de telefon și cod prin SMS (fără parolă).
- Profil de Priceput: zonele din Târgu Jiu (sau tot orașul), distanța, ce știe să facă (lista fixă de ~120 de lucrări).
- Postare lucrare: tipul din listă, zona, adresa (văzută doar de cine se oferă), descriere, când, cât oferi.
  Pentru „Transport marfă” se cer și plecarea și destinația.
- Lista de lucrări: cele mai noi sau după sumă, căutare după cuvinte („lemne”, „soba”, „pomi”), filtru „Potrivite pentru mine”.
- Butonul „Sunt interesat de lucrarea dvs.”: clientul primește numele și telefonul Priceputului, iar Priceputul vede adresa și telefonul clientului.
- Mesaje între client și Priceput, cu lista conversațiilor și mesajele necitite marcate.
- „Marchează finalizată” pentru client.
- Avatar ales la crearea contului (5 modele), schimbabil din Profil.
- Lista de prețuri a Priceputului (preț + unitate pentru fiecare lucrare) și tabul „Prețuri”, unde clientul compară de la cel mai ieftin.

## Urmează

Poze la lucrări, locație GPS și hartă, notificări „Lucrare nouă postată”, stele și ranguri după finalizare,
peretele Comunitate, raportare și blocare, ștergerea contului, Termeni în aplicație.

## Tehnic

Kotlin, Jetpack Compose (Material 3), Firebase Authentication (telefon) și Cloud Firestore.
Regulile de securitate sunt în [`firestore.rules`](firestore.rules). La fiecare modificare, GitHub Actions
rulează testele și construiește un APK de test (`.github/workflows/android.yml`).
